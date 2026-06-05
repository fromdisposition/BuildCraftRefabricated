/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.core.statements;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.lib.misc.LocaleUtil;

@Deprecated
public class StatementParameterDirection implements IStatementParameter {

    @Nullable
    private Direction direction = null;

    public StatementParameterDirection() {
    }

    public StatementParameterDirection(Direction face) {
        this.direction = face;
    }

    @Nullable
    public Direction getDirection() {
        return direction;
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public ISprite getSprite() {

        return null;
    }

    @Override
    public IStatementParameter onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        return null;
    }

    @Override
    public void writeToNbt(CompoundTag nbt) {
        if (direction != null) {
            nbt.putByte("direction", (byte) direction.ordinal());
        }
    }

    public static StatementParameterDirection readFromNbt(CompoundTag nbt) {
        StatementParameterDirection param = new StatementParameterDirection();
        if (nbt.contains("direction")) {
            param.direction = Direction.values()[nbt.getByte("direction").orElse((byte) 0)];
        }
        return param;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof StatementParameterDirection param) {
            return param.getDirection() == this.getDirection();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDirection());
    }

    @Override
    public String getDescription() {
        Direction dir = getDirection();
        if (dir == null) {
            return "";
        } else {
            return LocaleUtil.localize("direction." + dir.name().toLowerCase());
        }
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:pipeActionDirection";
    }

    @Override
    public IStatementParameter rotateLeft() {
        StatementParameterDirection d = new StatementParameterDirection();
        Direction dir = d.getDirection();
        if (dir != null && dir.getAxis() != Axis.Y) {
            d.direction = dir.getClockWise();
        }
        return d;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source) {
        IStatementParameter[] possible = new IStatementParameter[7];
        for (EnumPipePart part : EnumPipePart.VALUES) {
            if (part.face == direction) {
                possible[part.getIndex()] = this;
            } else {
                possible[part.getIndex()] = new StatementParameterDirection(part.face);
            }
        }
        return possible;
    }
}
