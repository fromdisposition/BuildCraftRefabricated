/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import buildcraft.lib.client.fluid.BcFluidTintUtil;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.attachments.Attachments;
import buildcraft.lib.attachments.AttachmentQueries;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.fluid.FluidResource;
import buildcraft.lib.transfer.fluid.FluidStacksResourceHandler;
import buildcraft.lib.transfer.transaction.Transaction;

import net.minecraft.world.level.material.Fluids;

import buildcraft.api.core.IFluidFilter;

public class FluidUtilBC {

    /** How a fluid is drawn: baked atlas (world/BER/GUI) vs runtime white+template (pipes). */
    public enum FluidRenderContext {
        WORLD, PIPE, BAKED_MESH, GUI
    }

    public record FluidAppearance(Identifier texture, int tintArgb, boolean vertexRecolor) {}

    private static final java.util.Map<Fluid, Integer> FABRIC_FLUID_TINTS = new java.util.concurrent.ConcurrentHashMap<>();

    public static void registerFluidTint(Fluid fluid, int argb) {
        if (fluid != null) {
            FABRIC_FLUID_TINTS.put(fluid, argb);
        }
    }

    public static boolean shouldRenderTranslucent(Fluid fluid) {
        if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
            return true;
        }
        Identifier fluidId = BuiltInRegistries.FLUID.getKey(fluid);
        return fluidId != null && "buildcraftenergy".equals(fluidId.getNamespace());
    }

    public static boolean shouldRenderTranslucent(FluidStack stack) {
        return !stack.isEmpty() && shouldRenderTranslucent(stack.getFluid());
    }

    public static FluidAppearance appearance(FluidStack stack, FluidRenderContext context) {
        if (stack.isEmpty() || stack.getFluid() == null) {
            return new FluidAppearance(
                    Identifier.withDefaultNamespace("block/water_still"),
                    0xFF3F76E4,
                    false);
        }
        buildcraft.fabric.BCEnergyFluidsFabric.FluidEntry entry =
                buildcraft.fabric.BCEnergyFluidsFabric.findEntry(canonicalFluid(stack.getFluid()));
        if (entry != null) {
            if (context == FluidRenderContext.PIPE) {
                return appearanceForPipe(entry);
            }
            return appearanceForBaked(entry);
        }
        return new FluidAppearance(getFluidTexture(stack), getFluidColor(stack), false);
    }

    public static FluidAppearance appearanceForPipe(buildcraft.fabric.BCEnergyFluidsFabric.FluidEntry entry) {
        return new FluidAppearance(
                BcFluidTintUtil.heatStillWhiteSpriteId(entry.heat()),
                BcFluidTintUtil.RENDER_TINT_WHITE,
                true);
    }

    public static FluidAppearance appearanceForBaked(buildcraft.fabric.BCEnergyFluidsFabric.FluidEntry entry) {
        return new FluidAppearance(
                BcFluidTintUtil.bakedStillSpriteId(entry.name()),
                BcFluidTintUtil.RENDER_TINT_WHITE,
                false);
    }

    /** RGBA vertex multipliers for baked-sprite mesh/GUI (white = use texture color as-is). */
    public static float[] vertexRgba(FluidStack stack) {
        int color = getFluidColor(stack);
        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        if (a <= 0f) {
            a = 1.0f;
        }
        return new float[] { r, g, b, a };
    }

    public static net.minecraft.resources.Identifier getFluidTexture(FluidStack stack) {
        if (stack.isEmpty() || stack.getFluid() == null) return net.minecraft.resources.Identifier.withDefaultNamespace("block/water_still");
        Fluid fluid = canonicalFluid(stack.getFluid());
        if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
            return net.minecraft.resources.Identifier.withDefaultNamespace("block/lava_still");
        }

        net.minecraft.resources.Identifier fluidId = net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(fluid);
        if (fluidId != null && fluidId.getNamespace().equals("buildcraftenergy")) {
            buildcraft.fabric.BCEnergyFluidsFabric.FluidEntry entry =
                    buildcraft.fabric.BCEnergyFluidsFabric.findEntry(fluid);
            if (entry != null) {
                return buildcraft.lib.client.fluid.BcFluidTintUtil.bakedStillSpriteId(entry.name());
            }
            int heat = buildcraft.fabric.BCEnergyFluidsFabric.getHeat(fluid);
            if (heat < 0) {
                heat = 0;
            }
            return buildcraft.lib.client.fluid.BcFluidTintUtil.heatStillWhiteSpriteId(heat);
        }
        if (fluidId != null) {
            return net.minecraft.resources.Identifier.parse(fluidId.getNamespace() + ":block/" + fluidId.getPath() + "_still");
        }

        return net.minecraft.resources.Identifier.withDefaultNamespace("block/water_still");
    }

    public static int getFluidColor(FluidStack stack) {
        if (stack.isEmpty() || stack.getFluid() == null) return 0xFFFFFFFF;
        Fluid fluid = canonicalFluid(stack.getFluid());

        if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
            return 0xFFFFFFFF;
        }
        if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
            return 0xFF3F76E4;
        }

        buildcraft.fabric.BCEnergyFluidsFabric.FluidEntry bcEntry =
                buildcraft.fabric.BCEnergyFluidsFabric.findEntry(fluid);
        if (bcEntry != null) {
            return BcFluidTintUtil.RENDER_TINT_WHITE;
        }

        Integer tint = FABRIC_FLUID_TINTS.get(fluid);
        if (tint != null) {
            return tint;
        }

        net.minecraft.resources.Identifier fluidId = net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(fluid);
        if (fluidId != null && fluidId.getNamespace().equals("buildcraftenergy")) {
            return 0xFF505050;
        }

        return 0xFFFFFFFF;
    }

    public static void pushFluidToNeighbors(Level level, BlockPos pos, FluidStacksResourceHandler tank) {
        if (tank.getAmountAsLong(0) <= 0) return;
        for (Direction dir : Direction.values()) {
            if (tank.getAmountAsLong(0) <= 0) break;
            BlockPos neighborPos = pos.relative(dir);
            ResourceHandler<FluidResource> neighbor = AttachmentQueries.getBlock(
                    level, Attachments.Fluid.BLOCK, neighborPos, dir.getOpposite());
            if (neighbor == null) continue;

            FluidResource resource = tank.getResource(0);
            if (resource.isEmpty()) break;

            int amountToTry = (int) Math.min(tank.getAmountAsLong(0), 1000);

            try (Transaction tx = Transaction.openRoot()) {
                int accepted = buildcraft.lib.transfer.ResourceHandlerUtil.move(
                        tank, neighbor, r -> true, amountToTry, tx);
                if (accepted > 0) {
                    tx.commit();
                }
            }
        }
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
        if (a.isEmpty() && b.isEmpty()) return true;
        if (a.isEmpty() || b.isEmpty()) return false;
        return FluidStack.isSameFluidSameComponents(a, b) && a.getAmount() == b.getAmount();
    }

    public static boolean areFluidsEqual(Fluid a, Fluid b) {
        if (a == null || b == null) {
            return a == b;
        }

        if (a == b) return true;
        net.minecraft.resources.Identifier idA = net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(a);
        net.minecraft.resources.Identifier idB = net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(b);
        return idA.getNamespace().equals(idB.getNamespace())
                && normalizeFluidPath(idA.getPath()).equals(normalizeFluidPath(idB.getPath()));
    }

    public static Fluid canonicalFluid(Fluid fluid) {
        if (fluid == null || fluid.isSame(Fluids.EMPTY)) {
            return Fluids.EMPTY;
        }
        Identifier fluidId = BuiltInRegistries.FLUID.getKey(fluid);
        if (fluidId == null) {
            return fluid;
        }
        String path = fluidId.getPath();
        if (path.startsWith("flowing_")) {
            Identifier stillId = Identifier.fromNamespaceAndPath(fluidId.getNamespace(), path.substring("flowing_".length()));
            Fluid still = BuiltInRegistries.FLUID.get(stillId).map(ref -> ref.value()).orElse(Fluids.EMPTY);
            return still.isSame(Fluids.EMPTY) ? fluid : still;
        }
        if (path.endsWith("_flowing")) {
            Identifier stillId = Identifier.fromNamespaceAndPath(
                    fluidId.getNamespace(), path.substring(0, path.length() - "_flowing".length()));
            Fluid still = BuiltInRegistries.FLUID.get(stillId).map(ref -> ref.value()).orElse(Fluids.EMPTY);
            return still.isSame(Fluids.EMPTY) ? fluid : still;
        }
        return fluid;
    }

    public static boolean areEquivalentFluidResources(FluidResource a, FluidResource b) {
        if (a.isEmpty() || b.isEmpty()) {
            return a.isEmpty() && b.isEmpty();
        }
        return areFluidsEqual(a.getFluid(), b.getFluid()) && a.getComponentsPatch().equals(b.getComponentsPatch());
    }

    public static FluidResource canonicalFluidResource(FluidResource resource) {
        if (resource.isEmpty()) {
            return resource;
        }
        Fluid canonical = canonicalFluid(resource.getFluid());
        if (canonical.isSame(resource.getFluid())) {
            return resource;
        }
        return FluidResource.of(canonical, resource.getComponentsPatch());
    }

    @Nullable
    public static FluidStack move(ResourceHandler<FluidResource> from, ResourceHandler<FluidResource> to) {
        return move(from, to, Integer.MAX_VALUE);
    }

    @Nullable
    public static FluidStack move(ResourceHandler<FluidResource> from, ResourceHandler<FluidResource> to, int max) {
        if (from == null || to == null) {
            return null;
        }

        try (Transaction tx = Transaction.openRoot()) {

            FluidResource firstAvailable = FluidResource.EMPTY;
            for (int i = 0; i < from.size(); i++) {
                if (!from.getResource(i).isEmpty()) {
                    firstAvailable = from.getResource(i);
                    break;
                }
            }

            if (firstAvailable.isEmpty()) return null;

            int moved = buildcraft.lib.transfer.ResourceHandlerUtil.move(from, to, r -> true, max, tx);
            if (moved > 0) {
                tx.commit();
                return firstAvailable.toStack(moved);
            }
        }
        return null;
    }

    public static String getDebugString(FluidStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "empty";
        }
        return stack.getAmount() + " mB " + net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(stack.getFluid());
    }

    public static String getDebugString(FluidStacksResourceHandler tank) {
        FluidResource f = tank.getResource(0);
        String name = f.isEmpty() ? "n/a" : net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(f.getFluid()).toString();
        return (f.isEmpty() ? 0 : tank.getAmountAsLong(0)) + " / " + tank.getCapacityAsLong(0, FluidResource.EMPTY) + " mB of " + name;
    }

    public static boolean isGaseous(FluidStack fluid) {
        return !fluid.isEmpty() && fluid.getFluidType().isLighterThanAir();
    }

    public static boolean isGaseous(Fluid fluid) {
        return fluid != null && !fluid.isSame(net.minecraft.world.level.material.Fluids.EMPTY)
                && buildcraft.lib.fluids.FluidTypes.of(fluid).isLighterThanAir();
    }

    private static String normalizeFluidPath(String path) {
        if (path.startsWith("flowing_")) {
            return path.substring("flowing_".length());
        }
        if (path.endsWith("_flowing")) {
            return path.substring(0, path.length() - "_flowing".length());
        }
        return path;
    }

    public static boolean isFluidContainerItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return AttachmentQueries.getItem(
                stack, Attachments.Fluid.ITEM, buildcraft.lib.transfer.access.ItemAccess.forStack(stack)) != null;
    }

    public static boolean isFluidContainerInHand(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            return false;
        }
        buildcraft.lib.transfer.access.ItemAccess access =
                buildcraft.lib.transfer.access.ItemAccess.forPlayerInteraction(player, hand).oneByOne();
        return access.getCapability(Attachments.Fluid.ITEM) != null;
    }

    public static boolean onTankActivated(Player player, BlockPos pos, InteractionHand hand,
        ResourceHandler<FluidResource> fluidHandler) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) return false;
        if (!isFluidContainerInHand(player, hand)) return false;
        Level world = player.level();
        if (world.isClientSide()) return true;
        return buildcraft.lib.transfer.fluid.FluidUtil.interactWithFluidHandler(player, hand, pos, fluidHandler);
    }

    public static ItemStack getFilledBucket(FluidStack fluidStack) {
        if (fluidStack == null || fluidStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (fluidStack.getComponents().isEmpty()) {
            if (fluidStack.is(Fluids.WATER)) {
                return new ItemStack(net.minecraft.world.item.Items.WATER_BUCKET);
            } else if (fluidStack.is(Fluids.LAVA)) {
                return new ItemStack(net.minecraft.world.item.Items.LAVA_BUCKET);
            }
        }
        return ItemStack.EMPTY;
    }
}
