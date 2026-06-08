/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.core.BCCoreSprites;
import buildcraft.lib.misc.LocaleUtil;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class StatementParameterRedstoneLevel implements IStatementParameter {
   public final int level;
   private final int minLevel;
   private final int maxLevel;

   public StatementParameterRedstoneLevel() {
      this(0, 0, 15);
   }

   public StatementParameterRedstoneLevel(int min, int max) {
      this(0, min, max);
   }

   public StatementParameterRedstoneLevel(int def, int min, int max) {
      this.level = def;
      this.minLevel = min;
      this.maxLevel = max;
   }

   public StatementParameterRedstoneLevel(CompoundTag nbt) {
      this.level = nbt.getByte("l").orElse((byte)0);
      this.minLevel = nbt.getByte("mi").orElse((byte)0);
      this.maxLevel = nbt.getByte("ma").orElse((byte)15);
   }

   @Override
   public void writeToNbt(CompoundTag nbt) {
      nbt.putByte("l", (byte)this.level);
      nbt.putByte("mi", (byte)this.minLevel);
      nbt.putByte("ma", (byte)this.maxLevel);
   }

   @Nonnull
   @Override
   public ItemStack getItemStack() {
      return ItemStack.EMPTY;
   }

   @Override
   public ISprite getSprite() {
      return BCCoreSprites.PARAM_REDSTONE_LEVEL[this.level & 15];
   }

   @Override
   public IStatementParameter onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
      int l = this.level;
      if (mouse.getButton() == 0) {
         l = l + 1 & 15;

         while (l < this.minLevel || l > this.maxLevel) {
            l = l + 1 & 15;
         }
      } else {
         l = l - 1 & 15;

         while (l < this.minLevel || l > this.maxLevel) {
            l = l - 1 & 15;
         }
      }

      return new StatementParameterRedstoneLevel(l, this.minLevel, this.maxLevel);
   }

   @Override
   public boolean equals(Object object) {
      return object instanceof StatementParameterRedstoneLevel param ? param.level == this.level : false;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.level);
   }

   @Override
   public String getDescription() {
      return String.format(LocaleUtil.localize("gate.trigger.redstone.input.level"), this.level);
   }

   @Override
   public String getUniqueTag() {
      return "buildcraft:redstoneLevel";
   }

   @Override
   public IStatementParameter rotateLeft() {
      return this;
   }

   @Override
   public IStatementParameter[] getPossible(IStatementContainer source) {
      IStatementParameter[] possible = new IStatementParameter[this.maxLevel - this.minLevel];

      for (int i = 0; i < this.maxLevel - this.minLevel; i++) {
         int l = this.minLevel + i;
         if (this.level == l) {
            possible[i] = this;
         } else {
            possible[i] = new StatementParameterRedstoneLevel(l, this.minLevel, this.maxLevel);
         }
      }

      return possible;
   }
}
