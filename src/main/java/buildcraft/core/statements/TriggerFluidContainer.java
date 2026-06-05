/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.core.statements;

import java.util.Locale;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;

import buildcraft.lib.attachments.Attachments;
import buildcraft.lib.attachments.AttachmentQueries;
import buildcraft.lib.transfer.access.ItemAccess;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.fluid.FluidResource;
import buildcraft.lib.transfer.transaction.Transaction;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;

public class TriggerFluidContainer extends BCStatement implements ITriggerExternal {
    public State state;

    public TriggerFluidContainer(State state) {
        super(
            "buildcraft:fluid." + state.name().toLowerCase(Locale.ROOT),
            "buildcraft.fluid." + state.name().toLowerCase(Locale.ROOT)
        );
        this.state = state;
    }

    @Override
    public SpriteHolder getSprite() {
        return BCCoreSprites.TRIGGER_FLUID.get(state);
    }

    @Override
    public int maxParameters() {
        return state == State.CONTAINS || state == State.SPACE ? 1 : 0;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.fluid." + state.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean isTriggerActive(BlockEntity tile, Direction side, IStatementContainer statementContainer, IStatementParameter[] parameters) {
        if (tile == null || tile.getLevel() == null) {
            return false;
        }

        ResourceHandler<FluidResource> handler = AttachmentQueries.getBlock(
            tile.getLevel(), Attachments.Fluid.BLOCK, tile.getBlockPos(), side != null ? side.getOpposite() : null
        );

        if (handler == null) {
            return false;
        }

        FluidResource searchedFluid = FluidResource.EMPTY;

        if (parameters != null && parameters.length >= 1 && parameters[0] != null && !parameters[0].getItemStack().isEmpty()) {
            net.minecraft.world.item.ItemStack stack = parameters[0].getItemStack();
            ResourceHandler<FluidResource> itemHandler = AttachmentQueries.getItem(stack, Attachments.Fluid.ITEM, ItemAccess.forStack(stack));
            if (itemHandler != null && itemHandler.size() > 0) {
                searchedFluid = itemHandler.getResource(0);
            }
        }

        int tanks = handler.size();
        if (tanks == 0) {
            return false;
        }

        switch (state) {
            case EMPTY: {
                for (int i = 0; i < tanks; i++) {
                    FluidResource fluid = handler.getResource(i);
                    if (!fluid.isEmpty() && handler.getAmountAsInt(i) > 0) {
                        return false;
                    }
                }
                return true;
            }
            case CONTAINS: {
                for (int i = 0; i < tanks; i++) {
                    FluidResource fluid = handler.getResource(i);
                    if (!fluid.isEmpty() && handler.getAmountAsInt(i) > 0
                        && (searchedFluid.isEmpty() || fluid.equals(searchedFluid))) {
                        return true;
                    }
                }
                return false;
            }
            case SPACE: {
                if (searchedFluid.isEmpty()) {
                    for (int i = 0; i < tanks; i++) {
                        FluidResource fluid = handler.getResource(i);
                        long cap = handler.getCapacityAsLong(i, fluid);
                        if (fluid.isEmpty() || handler.getAmountAsInt(i) < cap) {
                            return true;
                        }
                    }
                    return false;
                }
                try (Transaction tx = Transaction.openRoot()) {
                    for (int i = 0; i < tanks; i++) {
                        if (handler.insert(i, searchedFluid, 1, tx) > 0) {
                            return true;
                        }
                    }
                    return false;
                }
            }
            case FULL: {
                if (searchedFluid.isEmpty()) {
                    for (int i = 0; i < tanks; i++) {
                        FluidResource fluid = handler.getResource(i);
                        long cap = handler.getCapacityAsLong(i, fluid);
                        if (fluid.isEmpty() || handler.getAmountAsInt(i) < cap) {
                            return false;
                        }
                    }
                    return true;
                }
                try (Transaction tx = Transaction.openRoot()) {
                    for (int i = 0; i < tanks; i++) {
                        if (handler.insert(i, searchedFluid, 1, tx) > 0) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.TRIGGER_FLUID_ALL;
    }

    public enum State {
        EMPTY,
        CONTAINS,
        SPACE,
        FULL;

        public static final State[] VALUES = values();
    }
}
