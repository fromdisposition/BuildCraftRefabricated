/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.core.BCCoreSprites;
import buildcraft.lib.misc.LocaleUtil;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public enum StatementParamGateSideOnly implements IStatementParameter {
   ANY(false),
   SPECIFIC(true);

   public final boolean isSpecific;
   private static final StatementParamGateSideOnly[] POSSIBLE_ANY = new StatementParamGateSideOnly[]{ANY, SPECIFIC};
   private static final StatementParamGateSideOnly[] POSSIBLE_SPECIFIC = new StatementParamGateSideOnly[]{SPECIFIC, ANY};

   StatementParamGateSideOnly(boolean isSpecific) {
      this.isSpecific = isSpecific;
   }

   public static StatementParamGateSideOnly readFromNbt(CompoundTag nbt) {
      return BcNbt.getBoolean(nbt, "isOn", false) ? SPECIFIC : ANY;
   }

   @Override
   public void writeToNbt(CompoundTag compound) {
      compound.putBoolean("isOn", this.isSpecific);
   }

   @Nonnull
   @Override
   public ItemStack getItemStack() {
      return ItemStack.EMPTY;
   }

   @Override
   public ISprite getSprite() {
      return this.isSpecific ? BCCoreSprites.PARAM_GATE_SIDE_ONLY : null;
   }

   @Override
   public IStatementParameter.DrawType getDrawType() {
      return IStatementParameter.DrawType.SPRITE_ONLY;
   }

   public StatementParamGateSideOnly onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
      return null;
   }

   @Override
   public String getDescription() {
      return this.isSpecific ? LocaleUtil.localize("gate.parameter.redstone.gateSideOnly") : "";
   }

   @Override
   public String getUniqueTag() {
      return "buildcraft:redstoneGateSideOnly";
   }

   @Override
   public IStatementParameter rotateLeft() {
      return this;
   }

   @Override
   public IStatementParameter[] getPossible(IStatementContainer source) {
      return this.isSpecific ? POSSIBLE_SPECIFIC : POSSIBLE_ANY;
   }
}
