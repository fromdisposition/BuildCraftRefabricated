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

public enum PatternParameterYDir implements IStatementParameter {
   UP(true),
   DOWN(false);

   private static final PatternParameterYDir[] POSSIBLE_ORDER = new PatternParameterYDir[]{null, null, UP, null, null, null, DOWN};
   public final boolean up;

   PatternParameterYDir(boolean up) {
      this.up = up;
   }

   public static PatternParameterYDir readFromNbt(CompoundTag nbt) {
      return BcNbt.getBoolean(nbt, "up", false) ? UP : DOWN;
   }

   @Override
   public void writeToNbt(CompoundTag nbt) {
      nbt.putBoolean("up", this.up);
   }

   @Override
   public String getUniqueTag() {
      return "buildcraft:fillerParameterYDir";
   }

   @Nonnull
   @Override
   public ItemStack getItemStack() {
      return StackUtil.EMPTY;
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize("direction." + (this.up ? "up" : "down"));
   }

   public PatternParameterYDir onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
      return null;
   }

   @Override
   public IStatementParameter[] getPossible(IStatementContainer source) {
      return POSSIBLE_ORDER;
   }

   @Override
   public boolean isPossibleOrdered() {
      return true;
   }

   @Override
   public IStatementParameter rotateLeft() {
      return this;
   }

   @Override
   public ISprite getSprite() {
      return this.up ? BCBuildersSprites.PARAM_STAIRS_UP : BCBuildersSprites.PARAM_STAIRS_DOWN;
   }
}
