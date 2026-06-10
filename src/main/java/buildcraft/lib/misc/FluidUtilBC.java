/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.lib.client.fluid.BcFluidTintUtil;
import buildcraft.lib.fabric.transfer.FluidStorageOps;
import buildcraft.lib.fabric.transfer.ItemFluidLookup;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.fluids.FluidTypes;
import buildcraft.lib.transfer.fabric.TransferConvert;
import buildcraft.lib.transfer.fluid.FluidUtil;
import buildcraft.lib.transfer.neighbor.NeighborTransfers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
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

public class FluidUtilBC {
   private static final Map<Fluid, Integer> FABRIC_FLUID_TINTS = new ConcurrentHashMap<>();

   public static void registerFluidTint(Fluid fluid, int argb) {
      if (fluid != null) {
         FABRIC_FLUID_TINTS.put(fluid, argb);
      }
   }

   public static boolean shouldRenderTranslucent(Fluid fluid) {
      if (fluid != Fluids.WATER && fluid != Fluids.FLOWING_WATER) {
         Identifier fluidId = BuiltInRegistries.FLUID.getKey(fluid);
         return fluidId != null && "buildcraftenergy".equals(fluidId.getNamespace());
      } else {
         return true;
      }
   }

   public static boolean shouldRenderTranslucent(FluidStack stack) {
      return !stack.isEmpty() && shouldRenderTranslucent(stack.getFluid());
   }

   public static FluidUtilBC.FluidAppearance appearance(FluidStack stack, FluidUtilBC.FluidRenderContext context) {
      if (!stack.isEmpty() && stack.getFluid() != null) {
         BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(canonicalFluid(stack.getFluid()));
         if (context == FluidUtilBC.FluidRenderContext.PIPE) {
            return entry != null
               ? new FluidUtilBC.FluidAppearance(BcFluidTintUtil.bakedFlowSpriteId(entry.name()), -1, false)
               : new FluidUtilBC.FluidAppearance(getFluidFlowingTexture(stack), getFluidColor(stack), false);
         } else {
            return entry != null ? appearanceForBaked(entry) : new FluidUtilBC.FluidAppearance(getFluidTexture(stack), getFluidColor(stack), false);
         }
      } else {
         Identifier fallback = context == FluidUtilBC.FluidRenderContext.PIPE
            ? Identifier.withDefaultNamespace("block/water_flow")
            : Identifier.withDefaultNamespace("block/water_still");
         return new FluidUtilBC.FluidAppearance(fallback, -12618012, false);
      }
   }

   public static FluidUtilBC.FluidAppearance appearanceForBaked(BCEnergyFluidsFabric.FluidEntry entry) {
      return new FluidUtilBC.FluidAppearance(BcFluidTintUtil.bakedStillSpriteId(entry.name()), -1, false);
   }

   public static float[] vertexRgba(FluidStack stack) {
      int color = getFluidColor(stack);
      float a = (color >> 24 & 0xFF) / 255.0F;
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      if (a <= 0.0F) {
         a = 1.0F;
      }

      return new float[]{r, g, b, a};
   }

   public static Identifier getFluidTexture(FluidStack stack) {
      if (!stack.isEmpty() && stack.getFluid() != null) {
         Fluid fluid = canonicalFluid(stack.getFluid());
         if (fluid != Fluids.LAVA && fluid != Fluids.FLOWING_LAVA) {
            Identifier fluidId = BuiltInRegistries.FLUID.getKey(fluid);
            if (fluidId != null && fluidId.getNamespace().equals("buildcraftenergy")) {
               BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(fluid);
               if (entry != null) {
                  return BcFluidTintUtil.bakedStillSpriteId(entry.name());
               }

               int heat = BCEnergyFluidsFabric.getHeat(fluid);
               if (heat < 0) {
                  heat = 0;
               }

               return BcFluidTintUtil.heatStillWhiteSpriteId(heat);
            } else {
               return fluidId != null
                  ? Identifier.parse(fluidId.getNamespace() + ":block/" + fluidId.getPath() + "_still")
                  : Identifier.withDefaultNamespace("block/water_still");
            }
         } else {
            return Identifier.withDefaultNamespace("block/lava_still");
         }
      } else {
         return Identifier.withDefaultNamespace("block/water_still");
      }
   }

   public static Identifier getFluidFlowingTexture(FluidStack stack) {
      if (!stack.isEmpty() && stack.getFluid() != null) {
         Fluid fluid = canonicalFluid(stack.getFluid());
         if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
            return Identifier.withDefaultNamespace("block/water_flow");
         }

         if (fluid != Fluids.LAVA && fluid != Fluids.FLOWING_LAVA) {
            Identifier fluidId = BuiltInRegistries.FLUID.getKey(fluid);
            if (fluidId != null && fluidId.getNamespace().equals("buildcraftenergy")) {
               BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(fluid);
               if (entry != null) {
                  return BcFluidTintUtil.bakedFlowSpriteId(entry.name());
               }

               int heat = BCEnergyFluidsFabric.getHeat(fluid);
               if (heat < 0) {
                  heat = 0;
               }

               return BcFluidTintUtil.heatFlowSpriteId(heat);
            } else if (fluidId != null) {
               String path = fluidId.getPath();
               return path.endsWith("_flowing")
                  ? Identifier.fromNamespaceAndPath(fluidId.getNamespace(), "block/" + path)
                  : Identifier.parse(fluidId.getNamespace() + ":block/" + path + "_flowing");
            } else {
               return Identifier.withDefaultNamespace("block/water_flow");
            }
         } else {
            return Identifier.withDefaultNamespace("block/lava_flow");
         }
      } else {
         return Identifier.withDefaultNamespace("block/water_flow");
      }
   }

   public static int getFluidColor(FluidStack stack) {
      if (!stack.isEmpty() && stack.getFluid() != null) {
         Fluid fluid = canonicalFluid(stack.getFluid());
         if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
            return -1;
         }

         if (fluid != Fluids.WATER && fluid != Fluids.FLOWING_WATER) {
            BCEnergyFluidsFabric.FluidEntry bcEntry = BCEnergyFluidsFabric.findEntry(fluid);
            if (bcEntry != null) {
               return -1;
            }

            Integer tint = FABRIC_FLUID_TINTS.get(fluid);
            if (tint != null) {
               return tint;
            }

            Identifier fluidId = BuiltInRegistries.FLUID.getKey(fluid);
            return fluidId != null && fluidId.getNamespace().equals("buildcraftenergy") ? -11513776 : -1;
         } else {
            return -12618012;
         }
      } else {
         return -1;
      }
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

         long maxDroplets = TransferConvert.mbToDroplets(maxMb);
         try (Transaction transaction = Transaction.openOuter()) {
            long moved = FluidStorageOps.move(from, to, maxDroplets, transaction);
            if (moved <= 0L) {
               return null;
            }

            transaction.commit();
            return TransferConvert.toFluidStack(firstVariant, moved);
         }
      } else {
         return null;
      }
   }

   public static String getDebugString(FluidStack stack) {
      return stack != null && !stack.isEmpty() ? stack.getAmount() + " mB " + BuiltInRegistries.FLUID.getKey(stack.getFluid()) : "empty";
   }

   public static Component getFluidDisplayName(FluidStack stack) {
      if (stack != null && !stack.isEmpty()) {
         Component display = stack.getHoverName();
         String descriptionId = stack.getDescriptionId();
         if (!display.getString().equals(descriptionId)) {
            return display;
         }

         Fluid fluid = canonicalFluid(stack.getFluid());
         Item bucket = fluid.getBucket();
         if (bucket != Items.AIR) {
            Identifier bucketId = BuiltInRegistries.ITEM.getKey(bucket);
            if (bucketId != null) {
               String path = bucketId.getPath();
               if (path.endsWith("_bucket")) {
                  String blockPath = normalizeFluidPath(path.substring(0, path.length() - "_bucket".length()));
                  return Component.translatable(Identifier.fromNamespaceAndPath(bucketId.getNamespace(), blockPath).toLanguageKey("block"));
               }

               return new ItemStack(bucket).getHoverName();
            }
         }

         Identifier fluidId = BuiltInRegistries.FLUID.getKey(fluid);
         if (fluidId != null) {
            String path = normalizeFluidPath(fluidId.getPath());
            return Component.translatable(Identifier.fromNamespaceAndPath(fluidId.getNamespace(), path).toLanguageKey("block"));
         }

         return display;
      } else {
         return Component.empty();
      }
   }

   public static boolean isGaseous(FluidStack fluid) {
      return !fluid.isEmpty() && fluid.getFluidType().isLighterThanAir();
   }

   public static boolean isGaseous(Fluid fluid) {
      return fluid != null && !fluid.isSame(Fluids.EMPTY) && FluidTypes.of(fluid).isLighterThanAir();
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
      return world.isClientSide() ? true : FluidUtil.interactWithFluidStorage(player, hand, pos, storage);
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

            Item bucket = FluidTypes.of(fluid).getBucket(fluidStack);
            if (bucket != null) {
               return new ItemStack(bucket);
            }
         }

         return ItemStack.EMPTY;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public record FluidAppearance(Identifier texture, int tintArgb, boolean vertexRecolor) {
   }

   public enum FluidRenderContext {
      WORLD,
      PIPE,
      BAKED_MESH,
      GUI;
   }
}
