/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.statements;

import buildcraft.lib.nbt.BcNbt;
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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class TriggerParameterSignal implements IStatementParameter {
   public static final TriggerParameterSignal EMPTY = new TriggerParameterSignal(false, null);
   private static final Map<DyeColor, TriggerParameterSignal> SIGNALS_OFF = new EnumMap<>(DyeColor.class);
   private static final Map<DyeColor, TriggerParameterSignal> SIGNALS_ON = new EnumMap<>(DyeColor.class);
   public final boolean active;
   @Nullable
   public final DyeColor colour;

   public static TriggerParameterSignal get(boolean active, DyeColor colour) {
      return colour == null ? EMPTY : new TriggerParameterSignal(active, colour);
   }

   public static TriggerParameterSignal readFromNbt(CompoundTag nbt) {
      if (nbt.contains("color")) {
         DyeColor colour = DyeColor.byId(BcNbt.getByte(nbt, "color", (byte)0));
         boolean active = BcNbt.getBoolean(nbt, "active", false);
         return get(active, colour);
      } else {
         return EMPTY;
      }
   }

   @Override
   public void writeToNbt(CompoundTag nbt) {
      if (this.colour != null) {
         nbt.putByte("color", (byte)this.colour.getId());
         nbt.putBoolean("active", this.active);
      }
   }

   public static TriggerParameterSignal readFromBuf(FriendlyByteBuf buffer) {
      int colourId = buffer.readByte();
      if (colourId < 0) {
         return EMPTY;
      }

      DyeColor colour = DyeColor.byId(colourId);
      return get(buffer.readBoolean(), colour);
   }

   @Override
   public void writeToBuf(FriendlyByteBuf buffer) {
      if (this.colour == null) {
         buffer.writeByte(-1);
      } else {
         buffer.writeByte(this.colour.getId());
         buffer.writeBoolean(this.active);
      }
   }

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
      return this.colour == null ? null : BCTransportSprites.getPipeSignal(this.active, this.colour);
   }

   public TriggerParameterSignal onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
      return null;
   }

   @Override
   public String getDescription() {
      return this.colour == null
         ? null
         : String.format(LocaleUtil.localize("gate.trigger.pipe.wire." + (this.active ? "active" : "inactive")), ColourUtil.getTextFullTooltip(this.colour));
   }

   @Override
   public String getUniqueTag() {
      return "buildcraft:pipeWireTrigger";
   }

   @Override
   public IStatementParameter rotateLeft() {
      return this;
   }

   public TriggerParameterSignal[] getPossible(IStatementContainer source) {
      if (!(source instanceof IGate gate)) {
         return null;
      } else {
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

   static {
      for (DyeColor colour : ColourUtil.COLOURS) {
         SIGNALS_OFF.put(colour, new TriggerParameterSignal(false, colour));
         SIGNALS_ON.put(colour, new TriggerParameterSignal(true, colour));
      }
   }
}
