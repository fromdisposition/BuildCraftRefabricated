/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import buildcraft.lib.fluids.FluidStack;

import buildcraft.core.BCCore;
import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemFragileFluidContainer;
import buildcraft.lib.gui.GuiBC8;

@JeiPlugin
public class BCCoreJeiPlugin implements IModPlugin {
    private static final Identifier UID = Identifier.parse("buildcraftrefabricated:core_jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {

        registration.registerFromDataComponentTypes(
                BCCoreItems.PAINTBRUSH,
                BCCore.BRUSH_COLOR
        );

        registration.registerSubtypeInterpreter(
                BCCoreItems.FRAGILE_FLUID_CONTAINER,
                (stack, context) -> {
                    FluidStack fluid = ItemFragileFluidContainer.getFluid(stack);
                    if (fluid.isEmpty()) {
                        return null;
                    }
                    return BuiltInRegistries.FLUID.getKey(fluid.getFluid());
                }
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {

        registration.addGenericGuiContainerHandler(GuiBC8.class, new BCGuiContainerHandler());
    }
}
