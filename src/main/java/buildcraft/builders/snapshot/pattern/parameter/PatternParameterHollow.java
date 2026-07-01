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

public enum PatternParameterHollow implements IStatementParameter {
   FILLED_INNER(true, false),
   FILLED_OUTER(true, true),
   HOLLOW(false, false);

   public final boolean filled;
   public final boolean outerFilled;

   PatternParameterHollow(boolean filled, boolean outerFilled) {
      this.filled = filled;
      this.outerFilled = outerFilled;
   }

   public static PatternParameterHollow readFromNbt(CompoundTag nbt) {
      if (BcNbt.getBoolean(nbt, "filled", false)) {
         return BcNbt.getBoolean(nbt, "outer", false) ? FILLED_OUTER : FILLED_INNER;
      } else {
         return HOLLOW;
      }
   }

   @Override
   public void writeToNbt(CompoundTag compound) {
      compound.putBoolean("filled", this.filled);
      if (this.filled) {
         compound.putBoolean("outer", this.outerFilled);
      }
   }

   @Override
   public String getUniqueTag() {
      return "buildcraft:fillerParameterHollow";
   }

   @Override
   public ISprite getSprite() {
      if (this.filled) {
         return this.outerFilled ? BCBuildersSprites.PARAM_FILLED_OUTER : BCBuildersSprites.PARAM_FILLED_INNER;
      } else {
         return BCBuildersSprites.PARAM_HOLLOW;
      }
   }

   @Nonnull
   @Override
   public ItemStack getItemStack() {
      return StackUtil.EMPTY;
   }

   @Override
   public String getDescription() {
      String after = this.filled ? (this.outerFilled ? "filled_outer" : "filled") : "hollow";
      return LocaleUtil.localize("fillerpattern.parameter." + after);
   }

   public PatternParameterHollow onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
      return null;
   }

   @Override
   public IStatementParameter rotateLeft() {
      return this;
   }

   public PatternParameterHollow[] getPossible(IStatementContainer source) {
      return values();
   }
}
