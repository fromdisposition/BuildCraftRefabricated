/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;

import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.lib.fabric.transfer.FluidStorageOps;
import buildcraft.lib.fabric.transfer.ItemFluidLookup;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.fluid.meta.FluidAttributes;
import buildcraft.lib.fabric.transfer.FluidVariants;
import buildcraft.lib.fluid.interaction.FluidInteractions;
import buildcraft.lib.fabric.transfer.NeighborTransfers;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

/**
 * Single entry point for BuildCraft fluid identity, display, transfer, and container helpers.
 * Fabric Transfer API conversion is in {@link buildcraft.lib.fabric.transfer.FluidVariants}.
 * BC custom fluid physics/rendering uses {@link buildcraft.fabric.fluid.BcFluidUtil}, not this class.
 */
public final class BcFluids {
   private BcFluids() {
   }

   public static void pushFluidToNeighbors(Level level, BlockPos pos, Storage<FluidVariant> tank) {
      NeighborTransfers.pushFluidToNeighbors(level, pos, tank);
   }

   public static List<FluidStack> mergeSameFluids(List<FluidStack> fluids) {
      List<FluidStack> stacks = new ArrayList<>();
      fluids.forEach(toAdd -> {
         boolean found = false;

         for (FluidStack stack : stacks) {
            if (FluidStack.isSameFluidSameComponents(stack, toAdd)) {
               stack.grow(toAdd.getAmount());
               found = true;
            }
         }

         if (!found) {
            stacks.add(toAdd.copy());
         }
      });
      return stacks;
   }

   public static boolean areFluidStackEqual(FluidStack a, FluidStack b) {
      if (a.isEmpty() && b.isEmpty()) {
         return true;
      } else {
         return !a.isEmpty() && !b.isEmpty() ? FluidStack.isSameFluidSameComponents(a, b) && a.getAmount() == b.getAmount() : false;
      }
   }

   public static boolean areFluidsEqual(Fluid a, Fluid b) {
      if (a == null || b == null) {
         return a == b;
      }

      if (a == b) {
         return true;
      }

      Identifier idA = BuiltInRegistries.FLUID.getKey(a);
      Identifier idB = BuiltInRegistries.FLUID.getKey(b);
      return idA.getNamespace().equals(idB.getNamespace()) && normalizeFluidPath(idA.getPath()).equals(normalizeFluidPath(idB.getPath()));
   }

   public static Fluid canonicalFluid(Fluid fluid) {
      if (fluid != null && !fluid.isSame(Fluids.EMPTY)) {
         Identifier fluidId = BuiltInRegistries.FLUID.getKey(fluid);
         if (fluidId == null) {
            return fluid;
         } else {
            String path = fluidId.getPath();
            if (path.startsWith("flowing_")) {
               Identifier stillId = Identifier.fromNamespaceAndPath(fluidId.getNamespace(), path.substring("flowing_".length()));
               Fluid still = BuiltInRegistries.FLUID.get(stillId).map(ref -> (Fluid)ref.value()).orElse(Fluids.EMPTY);
               return still.isSame(Fluids.EMPTY) ? fluid : still;
            } else if (path.endsWith("_flowing")) {
               Identifier stillId = Identifier.fromNamespaceAndPath(fluidId.getNamespace(), path.substring(0, path.length() - "_flowing".length()));
               Fluid still = BuiltInRegistries.FLUID.get(stillId).map(ref -> (Fluid)ref.value()).orElse(Fluids.EMPTY);
               return still.isSame(Fluids.EMPTY) ? fluid : still;
            } else {
               return fluid;
            }
         }
      } else {
         return Fluids.EMPTY;
      }
   }

   public static boolean areEquivalentFluidStacks(FluidStack a, FluidStack b) {
      if (!a.isEmpty() && !b.isEmpty()) {
         FluidStack ca = canonicalFluidStack(a);
         FluidStack cb = canonicalFluidStack(b);
         return FluidStack.isSameFluidSameComponents(ca, cb);
      } else {
         return a.isEmpty() && b.isEmpty();
      }
   }

   public static FluidStack canonicalFluidStack(FluidStack stack) {
      if (stack.isEmpty()) {
         return stack;
      }

      Fluid canonical = canonicalFluid(stack.getFluid());
      return canonical.isSame(stack.getFluid()) ? stack : new FluidStack(canonical, stack.getAmount(), stack.getComponentsPatch());
   }

   @Nullable
   public static FluidStack move(Storage<FluidVariant> from, Storage<FluidVariant> to) {
      return move(from, to, Integer.MAX_VALUE);
   }

   @Nullable
   public static FluidStack move(Storage<FluidVariant> from, Storage<FluidVariant> to, int maxMb) {
      if (from != null && to != null) {
         FluidVariant firstVariant = FluidVariant.blank();

         for (StorageView<FluidVariant> view : from) {
            if (!view.isResourceBlank() && view.getAmount() > 0L) {
               firstVariant = (FluidVariant)view.getResource();
               break;
            }
         }

         if (firstVariant.isBlank()) {
            return null;
         }

         long maxDroplets = FluidVariants.mbToDroplets(maxMb);
         try (Transaction transaction = Transaction.openOuter()) {
            long moved = FluidStorageOps.move(from, to, maxDroplets, transaction);
            if (moved <= 0L) {
               return null;
            }

            transaction.commit();
            return FluidVariants.toStack(firstVariant, moved);
         }
      } else {
         return null;
      }
   }

   public static String getDebugString(FluidStack stack) {
      return stack != null && !stack.isEmpty() ? stack.getAmount() + " mB " + BuiltInRegistries.FLUID.getKey(stack.getFluid()) : "empty";
   }

   public static Component getFluidDisplayName(FluidStack stack) {
      if (stack == null || stack.isEmpty()) {
         return Component.empty();
      }

      Component fromClient = clientFluidDisplayName(stack);
      if (fromClient != null) {
         return fromClient;
      }

      Fluid fluid = canonicalFluid(stack.getFluid());
      Component fromAttributes = FluidAttributes.displayName(fluid);
      if (!isUntranslated(fromAttributes, stack.getDescriptionId())) {
         return fromAttributes;
      }
      Item bucket = fluid.getBucket();
      if (bucket != null && bucket != Items.AIR) {
         ItemStack bucketStack = new ItemStack(bucket);
         Component bucketName = bucketStack.getHoverName();
         Identifier bucketId = BuiltInRegistries.ITEM.getKey(bucket);
         if (bucketId != null) {
            String bucketKey = bucketId.toLanguageKey("item");
            if (!isUntranslated(bucketName, bucketKey)) {
               return bucketName;
            }
         }

         if (bucketId != null) {
            String path = bucketId.getPath();
            if (path.endsWith("_bucket")) {
               String blockPath = normalizeFluidPath(path.substring(0, path.length() - "_bucket".length()));
               String blockKey = Identifier.fromNamespaceAndPath(bucketId.getNamespace(), blockPath).toLanguageKey("block");
               Component fromBlock = Component.translatable(blockKey);
               if (!isUntranslated(fromBlock, blockKey)) {
                  return fromBlock;
               }
            }
         }
      }

      Identifier fluidId = BuiltInRegistries.FLUID.getKey(fluid);
      if (fluidId != null) {
         String path = normalizeFluidPath(fluidId.getPath());
         String blockKey = Identifier.fromNamespaceAndPath(fluidId.getNamespace(), path).toLanguageKey("block");
         Component fromBlock = Component.translatable(blockKey);
         if (!isUntranslated(fromBlock, blockKey)) {
            return fromBlock;
         }
      }

      return fromAttributes;
   }

   @Environment(EnvType.CLIENT)
   private static @Nullable Component clientFluidDisplayName(FluidStack stack) {
      if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
         return null;
      }

      List<Component> tooltip = FluidVariantRendering.getTooltip(FluidVariants.toVariant(stack));
      if (!tooltip.isEmpty()) {
         Component name = tooltip.getFirst();
         if (!isUntranslated(name, stack.getDescriptionId())) {
            return name;
         }
      }

      return null;
   }

   private static boolean isUntranslated(Component component, String translationKey) {
      String text = component.getString();
      return text.isEmpty() || text.equals(translationKey);
   }

   public static boolean isGaseous(FluidStack fluid) {
      return !fluid.isEmpty() && fluid.getFluidAttributes().isLighterThanAir();
   }

   public static boolean isGaseous(Fluid fluid) {
      return fluid != null && !fluid.isSame(Fluids.EMPTY) && FluidAttributes.of(fluid).isLighterThanAir();
   }

   private static String normalizeFluidPath(String path) {
      if (path.startsWith("flowing_")) {
         return path.substring("flowing_".length());
      } else {
         return path.endsWith("_flowing") ? path.substring(0, path.length() - "_flowing".length()) : path;
      }
   }

   public static boolean isFluidContainerItem(ItemStack stack) {
      return stack.isEmpty() ? false : ItemFluidLookup.storage(stack) != null;
   }

   public static boolean isFluidContainerInHand(Player player, InteractionHand hand) {
      ContainerItemContext context = ContainerItemContext.forPlayerInteraction(player, hand);
      if (context.getItemVariant().isBlank()) {
         return false;
      }

      ItemStack held = context.getItemVariant().toStack((int)Math.min(context.getAmount(), 2147483647L));
      return held.isEmpty() ? false : ItemFluidLookup.storage(held, context) != null;
   }

   public static boolean onTankActivated(Player player, BlockPos pos, InteractionHand hand, Storage<FluidVariant> storage) {
      ItemStack held = player.getItemInHand(hand);
      if (held.isEmpty()) {
         return false;
      }

      if (!isFluidContainerInHand(player, hand)) {
         return false;
      }

      Level world = player.level();
      return world.isClientSide() ? true : FluidInteractions.interactWithFluidStorage(player, hand, pos, storage);
   }

   public static boolean interactWithFluidHandler(Player player, InteractionHand hand, Level level, BlockPos pos, @Nullable Direction side) {
      return FluidInteractions.interactWithFluidHandler(player, hand, level, pos, side);
   }

   public static boolean interactWithFluidStorage(Player player, InteractionHand hand, @Nullable BlockPos pos, Storage<FluidVariant> tank) {
      return FluidInteractions.interactWithFluidStorage(player, hand, pos, tank);
   }

   public static FluidStack tryPickupFluid(
      @Nullable Storage<FluidVariant> destination, @Nullable Player player, Level level, BlockPos pos, @Nullable Direction side
   ) {
      return FluidInteractions.tryPickupFluid(destination, player, level, pos, side);
   }

   public static FluidStack tryPlaceFluid(@Nullable Storage<FluidVariant> source, @Nullable Player player, Level level, InteractionHand hand, BlockPos pos) {
      return FluidInteractions.tryPlaceFluid(source, player, level, hand, pos);
   }

   public static boolean tryPlaceFluid(FluidStack resource, @Nullable Player player, Level level, InteractionHand hand, BlockPos pos) {
      return FluidInteractions.tryPlaceFluid(resource, player, level, hand, pos);
   }

   public static void triggerSoundAndGameEvent(FluidStack stack, Level level, Vec3 position, @Nullable Player player, boolean pickup) {
      FluidInteractions.triggerSoundAndGameEvent(stack, level, position, player, pickup);
   }

   public static ItemStack getFilledBucket(FluidStack fluidStack) {
      if (fluidStack != null && !fluidStack.isEmpty()) {
         if (fluidStack.getComponents().isEmpty()) {
            if (fluidStack.is(Fluids.WATER)) {
               return new ItemStack(Items.WATER_BUCKET);
            }

            if (fluidStack.is(Fluids.LAVA)) {
               return new ItemStack(Items.LAVA_BUCKET);
            }

            Fluid fluid = canonicalFluid(fluidStack.getFluid());
            BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(fluid);
            if (entry != null && entry.bucket() != null) {
               return new ItemStack(entry.bucket());
            }

            Item bucket = FluidAttributes.of(fluid).getBucket(fluidStack);
            if (bucket != null) {
               return new ItemStack(bucket);
            }
         }

         return ItemStack.EMPTY;
      } else {
         return ItemStack.EMPTY;
      }
   }

}
