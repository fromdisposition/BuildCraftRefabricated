/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;

import buildcraft.lib.attachments.Attachments;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.access.ItemAccess;
import buildcraft.lib.transfer.fluid.FluidResource;

import buildcraft.api.lists.ListMatchHandler;

public class ListMatchHandlerFluid extends ListMatchHandler {

    @Nullable
    private static ResourceHandler<FluidResource> handlerOf(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) return null;
        return buildcraft.lib.attachments.AttachmentQueries.getItem(stack, Attachments.Fluid.ITEM, ItemAccess.forStack(stack));
    }

    @Nullable
    private static FluidResource firstResource(@Nonnull ItemStack stack) {
        ResourceHandler<FluidResource> h = handlerOf(stack);
        if (h == null || h.size() == 0) return null;
        return h.getResource(0);
    }

    @Override
    public boolean isValidSource(Type type, @Nonnull ItemStack stack) {
        switch (type) {
            case TYPE:
                return handlerOf(stack) != null;
            case MATERIAL: {
                FluidResource r = firstResource(stack);
                return r != null && !r.isEmpty();
            }
            default:
                return false;
        }
    }

    @Nonnull
    @Override
    public java.util.List<String> describeMatch(Type type, @Nonnull ItemStack stack) {
        switch (type) {
            case TYPE:
                if (handlerOf(stack) != null) {
                    return java.util.List.of("any fluid container");
                }
                return java.util.List.of();
            case MATERIAL: {
                FluidResource r = firstResource(stack);
                if (r == null || r.isEmpty()) return java.util.List.of();
                net.minecraft.resources.Identifier id =
                        net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(r.getFluid());
                return java.util.List.of("fluid: " + (id != null ? id.toString() : r.getFluid().toString()));
            }
            default:
                return java.util.List.of();
        }
    }

    @Override
    public boolean matches(Type type, @Nonnull ItemStack source, @Nonnull ItemStack target, boolean precise) {
        switch (type) {
            case TYPE:
                return handlerOf(source) != null && handlerOf(target) != null;
            case MATERIAL: {
                FluidResource a = firstResource(source);
                FluidResource b = firstResource(target);
                if (a == null || b == null || a.isEmpty() || b.isEmpty()) return false;
                return a.equals(b);
            }
            default:
                return false;
        }
    }
}
