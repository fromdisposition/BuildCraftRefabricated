/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.enums;

import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public enum EnumSpring implements StringRepresentable {
   WATER(5, -1, Blocks.WATER.defaultBlockState()),
   OIL(6000, 8, null);

   public static final EnumSpring[] VALUES = values();
   public final int tickRate;
   public final int chance;
   public BlockState liquidBlock;
   public boolean canGen = true;
   public Supplier<BlockEntity> tileConstructor;
   private final String lowerCaseName = this.name().toLowerCase(Locale.ROOT);

   EnumSpring(int tickRate, int chance, BlockState liquidBlock) {
      this.tickRate = tickRate;
      this.chance = chance;
      this.liquidBlock = liquidBlock;
   }

   public String getSerializedName() {
      return this.lowerCaseName;
   }
}
