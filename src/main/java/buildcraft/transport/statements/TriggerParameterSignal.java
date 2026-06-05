/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.statements;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;

import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StackUtil;

import buildcraft.transport.BCTransportSprites;

public class TriggerParameterSignal implements IStatementParameter {

    public static final TriggerParameterSignal EMPTY;
    private static final Map<DyeColor, TriggerParameterSignal> SIGNALS_OFF, SIGNALS_ON;

    static {
        EMPTY = new TriggerParameterSignal(false, null);
        SIGNALS_OFF = new EnumMap<>(DyeColor.class);
        SIGNALS_ON = new EnumMap<>(DyeColor.class);
        for (DyeColor colour : ColourUtil.COLOURS) {
            SIGNALS_OFF.put(colour, new TriggerParameterSignal(false, colour));
            SIGNALS_ON.put(colour, new TriggerParameterSignal(true, colour));
        }
    }

    public static TriggerParameterSignal get(boolean active, DyeColor colour) {
        if (colour == null) {
            return EMPTY;
        }
        return new TriggerParameterSignal(active, colour);
    }

    public static TriggerParameterSignal readFromNbt(CompoundTag nbt) {
        if (nbt.contains("color")) {
            DyeColor colour = DyeColor.byId(nbt.getByteOr("color", (byte) 0));
            boolean active = nbt.getBooleanOr("active", false);
            return get(active, colour);
        } else {
            return EMPTY;
        }
    }

    @Override
    public void writeToNbt(CompoundTag nbt) {
        if (colour != null) {
            nbt.putByte("color", (byte) colour.getId());
            nbt.putBoolean("active", active);
        }
    }

    public static TriggerParameterSignal readFromBuf(FriendlyByteBuf buffer) {
        int colourId = buffer.readByte();
        if (colourId < 0) {
            return EMPTY;
        } else {
            DyeColor colour = DyeColor.byId(colourId);
            return get(buffer.readBoolean(), colour);
        }
    }

    @Override
    public void writeToBuf(FriendlyByteBuf buffer) {
        if (colour == null) {
            buffer.writeByte(-1);
        } else {
            buffer.writeByte(colour.getId());
            buffer.writeBoolean(active);
        }
    }

    public final boolean active;

    @Nullable
    public final DyeColor colour;

    private TriggerParameterSignal(boolean active, DyeColor colour) {
        this.active = active;
        this.colour = colour;
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return StackUtil.EMPTY;
    }

    @Override
    public ISprite getSprite() {
        if (colour == null) {
            return null;
        }
        return BCTransportSprites.getPipeSignal(active, colour);
    }

    @Override
    public TriggerParameterSignal onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
        StatementMouseClick mouse) {
        return null;
    }

    @Override
    public String getDescription() {
        if (colour == null) {
            return null;
        }
        return String.format(LocaleUtil.localize("gate.trigger.pipe.wire." + (active ? "active" : "inactive")),
            ColourUtil.getTextFullTooltip(colour));
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:pipeWireTrigger";
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public TriggerParameterSignal[] getPossible(IStatementContainer source) {
        if (!(source instanceof IGate)) {
            return null;
        }
        IGate gate = (IGate) source;
        List<TriggerParameterSignal> poss = new ArrayList<>(ColourUtil.COLOURS.length * 2 + 1);
        poss.add(EMPTY);
        for (DyeColor c : ColourUtil.COLOURS) {
            if (TriggerPipeSignal.doesGateHaveColour(gate, c)) {
                poss.add(get(true, c));
                poss.add(get(false, c));
            }
        }
        return poss.toArray(new TriggerParameterSignal[poss.size()]);
    }
}
