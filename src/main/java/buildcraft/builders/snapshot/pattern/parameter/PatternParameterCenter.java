/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot.pattern.parameter;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.builders.BCBuildersSprites;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StackUtil;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public enum PatternParameterCenter implements IStatementParameter {
   NORTH_WEST(-1, -1),
   NORTH(0, -1),
   NORTH_EAST(1, -1),
   WEST(-1, 0),
   CENTER(0, 0),
   EAST(1, 0),
   SOUTH_WEST(-1, 1),
   SOUTH(0, 1),
   SOUTH_EAST(1, 1);

   public static final PatternParameterCenter[] POSSIBLE_ORDER = new PatternParameterCenter[]{
      CENTER, NORTH_WEST, NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST
   };
   public final int offsetX;
   public final int offsetZ;

   PatternParameterCenter(int x, int z) {
      this.offsetX = x;
      this.offsetZ = z;
   }

   public static PatternParameterCenter readFromNbt(CompoundTag nbt) {
      int ord = BcNbt.getByte(nbt, "dir", (byte)0);
      return ord >= 0 && ord < values().length ? values()[ord] : CENTER;
   }

   @Override
   public void writeToNbt(CompoundTag nbt) {
      nbt.putByte("dir", (byte)this.ordinal());
   }

   @Override
   public String getUniqueTag() {
      return "buildcraft:fillerParameterCenter";
   }

   @Override
   public ISprite getSprite() {
      return BCBuildersSprites.PARAM_CENTER.get(this);
   }

   @Nonnull
   @Override
   public ItemStack getItemStack() {
      return StackUtil.EMPTY;
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize("direction.center." + this.ordinal());
   }

   public PatternParameterCenter onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
      return null;
   }

   @Override
   public IStatementParameter rotateLeft() {
      return this;
   }

   @Override
   public IStatementParameter[] getPossible(IStatementContainer source) {
      return POSSIBLE_ORDER;
   }

   @Override
   public boolean isPossibleOrdered() {
      return true;
   }
}
