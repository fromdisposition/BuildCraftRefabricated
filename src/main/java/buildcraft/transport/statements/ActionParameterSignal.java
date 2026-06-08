/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.statements;

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
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class ActionParameterSignal implements IStatementParameter {
   public static final ActionParameterSignal EMPTY = new ActionParameterSignal(null);
   private static final Map<DyeColor, ActionParameterSignal> SIGNALS = new EnumMap<>(DyeColor.class);
   @Nullable
   public final DyeColor colour;

   private ActionParameterSignal(DyeColor colour) {
      this.colour = colour;
   }

   public static ActionParameterSignal get(DyeColor colour) {
      return colour == null ? EMPTY : SIGNALS.get(colour);
   }

   public static ActionParameterSignal readFromNbt(CompoundTag nbt) {
      return nbt.contains("color") ? get(DyeColor.byId(nbt.getByteOr("color", (byte)0))) : EMPTY;
   }

   @Override
   public void writeToNbt(CompoundTag nbt) {
      DyeColor c = this.colour;
      if (c != null) {
         nbt.putByte("color", (byte)c.getId());
      }
   }

   @Nullable
   public DyeColor getColor() {
      return this.colour;
   }

   @Override
   public ISprite getSprite() {
      DyeColor c = this.colour;
      return c == null ? null : BCTransportSprites.getPipeSignal(true, c);
   }

   public ActionParameterSignal onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
      return null;
   }

   @Override
   public boolean equals(Object object) {
      return object instanceof ActionParameterSignal param ? param.getColor() == this.getColor() : false;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.getColor());
   }

   @Override
   public String getDescription() {
      DyeColor c = this.colour;
      if (c == null) {
         return null;
      }

      String format = LocaleUtil.localize("gate.action.pipe.wire");
      Object[] args = new Object[]{ColourUtil.getTextFullTooltip(c)};
      return String.format(format, args);
   }

   @Override
   public String getUniqueTag() {
      return "buildcraft:pipeWireAction";
   }

   @Override
   public IStatementParameter rotateLeft() {
      return this;
   }

   @Nonnull
   @Override
   public ItemStack getItemStack() {
      return StackUtil.EMPTY;
   }

   @Override
   public IStatementParameter[] getPossible(IStatementContainer source) {
      if (!(source instanceof IGate gate)) {
         return null;
      } else {
         List<IStatementParameter> poss = new ArrayList<>(1 + ColourUtil.COLOURS.length);
         poss.add(EMPTY);

         for (DyeColor c : ColourUtil.COLOURS) {
            if (TriggerPipeSignal.doesGateHaveColour(gate, c)) {
               poss.add(get(c));
            }
         }

         return poss.toArray(new IStatementParameter[poss.size()]);
      }
   }

   static {
      for (DyeColor colour : ColourUtil.COLOURS) {
         SIGNALS.put(colour, new ActionParameterSignal(colour));
      }
   }
}
