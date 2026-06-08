/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot.pattern.parameter;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.builders.BCBuildersSprites;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StackUtil;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public enum PatternParameterXZDir implements IStatementParameter {
   WEST(Direction.WEST),
   EAST(Direction.EAST),
   NORTH(Direction.NORTH),
   SOUTH(Direction.SOUTH);

   private static final PatternParameterXZDir[] POSSIBLE_ORDER = new PatternParameterXZDir[]{null, null, NORTH, null, EAST, null, SOUTH, null, WEST};
   private static final Map<Direction, PatternParameterXZDir> map = new EnumMap<>(Direction.class);
   public final Direction dir;

   PatternParameterXZDir(Direction dir) {
      this.dir = dir;
   }

   public static PatternParameterXZDir get(Direction face) {
      PatternParameterXZDir param = map.get(face);
      if (param == null) {
         throw new IllegalArgumentException("Can only accept horizontal Direction's (was given " + face + ")");
      } else {
         return param;
      }
   }

   public static PatternParameterXZDir readFromNbt(CompoundTag nbt) {
      Direction dir;
      if (nbt.contains("dir")) {
         int d = nbt.getByte("dir").orElse((byte)0) + 2;
         dir = Direction.from2DDataValue(d);
      } else {
         dir = Direction.from2DDataValue(nbt.getByte("d").orElse((byte)0));
      }

      PatternParameterXZDir param = map.get(dir);
      if (param == null) {
         throw new IllegalStateException("Map lookup failed for " + dir);
      } else {
         return param;
      }
   }

   @Override
   public void writeToNbt(CompoundTag nbt) {
      nbt.putByte("d", (byte)this.dir.get2DDataValue());
   }

   @Override
   public String getUniqueTag() {
      return "buildcraft:fillerParameterXZDir";
   }

   @Override
   public ISprite getSprite() {
      return BCBuildersSprites.PARAM_XZ_DIR.get(this.dir);
   }

   @Nonnull
   @Override
   public ItemStack getItemStack() {
      return StackUtil.EMPTY;
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize("direction." + this.dir.getSerializedName());
   }

   public PatternParameterXZDir onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
      return null;
   }

   @Override
   public IStatementParameter rotateLeft() {
      return get(this.dir.getClockWise());
   }

   @Override
   public IStatementParameter[] getPossible(IStatementContainer source) {
      return POSSIBLE_ORDER;
   }

   @Override
   public boolean isPossibleOrdered() {
      return true;
   }

   static {
      for (PatternParameterXZDir param : values()) {
         map.put(param.dir, param);
      }
   }
}
