/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.integration.jei;

import buildcraft.core.BCCore;
import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemFragileFluidContainer;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.gui.BcScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

@JeiPlugin
public class BCCoreJeiPlugin implements IModPlugin {
   private static final Identifier UID = Identifier.parse("buildcraftrefabricated:core_jei_plugin");

   @Override
   public Identifier getPluginUid() {
      return UID;
   }

   @Override
   public void registerItemSubtypes(ISubtypeRegistration registration) {
      //? if >= 1.21.10 {
      registration.registerFromDataComponentTypes(BCCoreItems.PAINTBRUSH, BCCore.BRUSH_COLOR);
      registration.registerSubtypeInterpreter(BCCoreItems.FRAGILE_FLUID_CONTAINER, (stack, context) -> {
         FluidStack fluid = ItemFragileFluidContainer.getFluid(stack);
         return fluid.isEmpty() ? null : BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString();
      });
      //?} else {
      /*// JEI 19 has no registerFromDataComponentTypes helper, and its single-method IIngredientSubtypeInterpreter
      // is deprecated-for-removal; use the current ISubtypeInterpreter overload (getSubtypeData + legacy string).
      registration.registerSubtypeInterpreter(BCCoreItems.PAINTBRUSH, new mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter<net.minecraft.world.item.ItemStack>() {
         public Object getSubtypeData(net.minecraft.world.item.ItemStack stack, mezz.jei.api.ingredients.subtypes.UidContext context) {
            return stack.get(BCCore.BRUSH_COLOR);
         }

         @Deprecated
         public String getLegacyStringSubtypeInfo(net.minecraft.world.item.ItemStack stack, mezz.jei.api.ingredients.subtypes.UidContext context) {
            return String.valueOf(stack.get(BCCore.BRUSH_COLOR));
         }
      });
      registration.registerSubtypeInterpreter(BCCoreItems.FRAGILE_FLUID_CONTAINER, new mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter<net.minecraft.world.item.ItemStack>() {
         public Object getSubtypeData(net.minecraft.world.item.ItemStack stack, mezz.jei.api.ingredients.subtypes.UidContext context) {
            FluidStack fluid = ItemFragileFluidContainer.getFluid(stack);
            return fluid.isEmpty() ? null : BuiltInRegistries.FLUID.getKey(fluid.getFluid());
         }

         @Deprecated
         public String getLegacyStringSubtypeInfo(net.minecraft.world.item.ItemStack stack, mezz.jei.api.ingredients.subtypes.UidContext context) {
            FluidStack fluid = ItemFragileFluidContainer.getFluid(stack);
            return fluid.isEmpty() ? "" : BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString();
         }
      });
      *///?}
   }

   @Override
   public void registerGuiHandlers(IGuiHandlerRegistration registration) {
      registration.addGenericGuiContainerHandler(BcScreen.class, new BCGuiContainerHandler());
   }
}
