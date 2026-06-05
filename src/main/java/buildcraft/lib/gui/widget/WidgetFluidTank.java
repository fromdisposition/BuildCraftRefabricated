/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.widget;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import buildcraft.lib.attachments.Attachments;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.fluid.FluidResource;
import buildcraft.lib.transfer.transaction.Transaction;
import buildcraft.fabric.network.BCPayloadContext;

import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.fuels.ISolidCoolant;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.Widget_Neptune;
import buildcraft.lib.net.PacketBufferBC;

public class WidgetFluidTank extends Widget_Neptune<ContainerBC_Neptune> {
    private static final byte NET_CLICK = 0;

    private final ResourceHandler<FluidResource> tank;

    public WidgetFluidTank(ContainerBC_Neptune container, ResourceHandler<FluidResource> tank) {
        super(container);
        this.tank = tank;
    }

    private boolean canAccessTank() {
        return tank != null && tank.size() > 0;
    }

    @Override
    public void handleWidgetDataServer(BCPayloadContext ctx, PacketBufferBC buffer) {
        byte id = buffer.readByte();
        if (id == NET_CLICK) {
            onGuiClicked();
        }
    }

    public void sendClick() {
        sendWidgetData(buf -> buf.writeByte(NET_CLICK));
    }

    private boolean drainTankIntoInventoryBucket(Player player) {
        if (!canAccessTank()) {
            return false;
        }
        FluidResource tankFluid = tank.size() > 0 ? tank.getResource(0) : FluidResource.EMPTY;
        if (tankFluid.isEmpty() || tank.getAmountAsLong(0) <= 0) {
            return false;
        }
        net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        int size = inv.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack invStack = inv.getItem(i);
            if (invStack.isEmpty()) continue;
            buildcraft.lib.transfer.access.ItemAccess slotAccess =
                    buildcraft.lib.transfer.access.ItemAccess.forPlayerSlot(player, i).oneByOne();
            ResourceHandler<FluidResource> slotCap = slotAccess.getCapability(Attachments.Fluid.ITEM);
            if (slotCap == null || slotCap.size() == 0) continue;

            if (!slotCap.getResource(0).isEmpty()) continue;
            try (Transaction tx = Transaction.openRoot()) {
                int moved = buildcraft.lib.transfer.ResourceHandlerUtil.move(
                        tank, slotCap, r -> true, Integer.MAX_VALUE, tx);
                if (moved > 0) {
                    tx.commit();
                    return true;
                }
            }
        }
        return false;
    }

    private void onGuiClicked() {
        if (!canAccessTank()) {
            return;
        }
        Player player = container.player;
        ItemStack held = player.containerMenu.getCarried();
        if (held.isEmpty()) {
            return;
        }

        transferStackToTank(player);
        if (player instanceof ServerPlayer sp) {
            sp.containerMenu.broadcastChanges();
        }
    }

    private void transferStackToTank(Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        if (!canAccessTank()) {
            return;
        }
        ItemStack carried = player.containerMenu.getCarried();
        boolean isCreative = player.getAbilities().instabuild;

        if (isCreative) {

            ItemStack bucketCopy = carried.copy();
            buildcraft.lib.transfer.access.ItemAccess copyAccess =
                buildcraft.lib.transfer.access.ItemAccess.forStack(bucketCopy);
            ResourceHandler<FluidResource> bucketCap = copyAccess.getCapability(Attachments.Fluid.ITEM);

            if (bucketCap != null && bucketCap.size() > 0) {
                FluidResource bucketFluid = bucketCap.getResource(0);
                long bucketAmount = bucketCap.getAmountAsLong(0);
                if (!bucketFluid.isEmpty() && bucketAmount > 0) {
                    try (Transaction tx = Transaction.openRoot()) {
                        int filled = tank.insert(0, bucketFluid, (int) bucketAmount, tx);
                        if (filled > 0) {
                            tx.commit();
                        }
                    }
                    return;
                }

                if (!tank.getResource(0).isEmpty() && tank.getAmountAsLong(0) > 0) {
                    try (Transaction tx = Transaction.openRoot()) {
                        int drained = tank.extract(0,
                            tank.getResource(0),
                            (int) Math.min(1000L, tank.getAmountAsLong(0)),
                            tx);
                        if (drained > 0) {
                            tx.commit();
                        }
                    }
                }
                return;
            }

        } else {
            ItemStack original = carried.copy();
            buildcraft.lib.transfer.access.ItemAccess access = buildcraft.lib.transfer.access.ItemAccess.forPlayerCursor(player, player.containerMenu).oneByOne();
            ResourceHandler<FluidResource> itemHandlerIn = access.getCapability(Attachments.Fluid.ITEM);

            if (itemHandlerIn != null) {

                try (Transaction tx = Transaction.openRoot()) {
                    int moved = buildcraft.lib.transfer.ResourceHandlerUtil.move(
                            itemHandlerIn, tank, r -> true, Integer.MAX_VALUE, tx
                    );
                    if (moved > 0) {
                        tx.commit();
                        return;
                    }
                }

                try (Transaction tx = Transaction.openRoot()) {
                    int moved = buildcraft.lib.transfer.ResourceHandlerUtil.move(
                            tank, itemHandlerIn, r -> true, Integer.MAX_VALUE, tx
                    );
                    if (moved > 0) {
                        tx.commit();
                        return;
                    }
                }

                if (drainTankIntoInventoryBucket(player)) {
                    return;
                }
            }
        }

        if (BuildcraftFuelRegistry.coolant != null) {
            ItemStack stack = player.containerMenu.getCarried();
            ItemStack singleCopyCoolant = stack.copyWithCount(1);
            ISolidCoolant solidCoolant = BuildcraftFuelRegistry.coolant.getSolidCoolant(singleCopyCoolant);
            if (solidCoolant != null) {
                FluidStack fluidCoolant = solidCoolant.getFluidFromSolidCoolant(singleCopyCoolant);
                if (fluidCoolant != null && !fluidCoolant.isEmpty()) {
                    try (Transaction tx = Transaction.openRoot()) {
                        int filled = tank.insert(0, FluidResource.of(fluidCoolant), fluidCoolant.getAmount(), tx);
                        if (filled == fluidCoolant.getAmount()) {
                            tx.commit();

                            buildcraft.lib.misc.AdvancementUtil.unlockAdvancement(
                                player, net.minecraft.resources.Identifier.parse("buildcraftenergy:ice_cool"));
                            if (!isCreative) {
                                stack.shrink(1);
                            }
                            return;
                        }
                    }
                }
            }
        }
    }
}
