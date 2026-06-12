/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid.display;

import buildcraft.lib.fluid.identity.FluidIdentity;
import buildcraft.lib.fluid.meta.FluidAttributes;
import buildcraft.lib.fluid.stack.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public final class FluidDisplayNames {
   @FunctionalInterface
   public interface ClientResolver {
      @Nullable
      Component resolve(FluidStack stack);
   }

   private static @Nullable ClientResolver clientResolver;

   private FluidDisplayNames() {
   }

   public static void setClientResolver(@Nullable ClientResolver resolver) {
      clientResolver = resolver;
   }

   public static String debugString(FluidStack stack) {
      return stack != null && !stack.isEmpty() ? stack.getAmount() + " mB " + BuiltInRegistries.FLUID.getKey(stack.getFluid()) : "empty";
   }

   public static Component forStack(FluidStack stack) {
      if (stack == null || stack.isEmpty()) {
         return Component.empty();
      }

      if (clientResolver != null) {
         Component fromClient = clientResolver.resolve(stack);
         if (fromClient != null && !fromClient.getString().isEmpty()) {
            return fromClient;
         }
      }

      Fluid fluid = FluidIdentity.canonicalFluid(stack.getFluid());
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
               String blockPath = FluidIdentity.normalizeFluidPath(path.substring(0, path.length() - "_bucket".length()));
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
         String path = FluidIdentity.normalizeFluidPath(fluidId.getPath());
         String blockKey = Identifier.fromNamespaceAndPath(fluidId.getNamespace(), path).toLanguageKey("block");
         Component fromBlock = Component.translatable(blockKey);
         if (!isUntranslated(fromBlock, blockKey)) {
            return fromBlock;
         }
      }

      return fromAttributes;
   }

   private static boolean isUntranslated(Component component, String translationKey) {
      String text = component.getString();
      return text.isEmpty() || text.equals(translationKey);
   }
}
