/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.tile;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.core.BCCoreBlockEntities;
import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.MessageUtil;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import buildcraft.lib.nbt.BcValueIn;
import buildcraft.lib.nbt.BcValueOut;

public class TileEngineCreative extends TileEngineBase_BC8 {
   public static final long[] OUTPUTS = new long[]{1L, 2L, 4L, 8L, 16L, 32L, 64L, 128L, 256L};
   public int currentOutputIndex = 0;

   public TileEngineCreative(BlockPos pos, BlockState state) {
      super(BCCoreBlockEntities.ENGINE_CREATIVE, pos, state);
   }

   @Nonnull
   @Override
   protected IMjConnector createConnector() {
      return new EngineConnector(false);
   }

   @Override
   public boolean isBurning() {
      return this.isRedstonePowered;
   }

   @Override
   protected void engineUpdate() {
      if (this.isBurning()) {
         this.power = this.power + this.getCurrentOutput();
         long max = this.getMaxPower();
         if (this.power > max) {
            this.power = max;
         }
      } else {
         this.power = 0L;
      }
   }

   @Override
   public double getPistonSpeed() {
      double max = 0.08;
      double min = 0.01;
      double interp = (double)this.currentOutputIndex / (OUTPUTS.length - 1);
      return MathUtil.interp(interp, 0.01, 0.08);
   }

   @Override
   protected EnumPowerStage computePowerStage() {
      return EnumPowerStage.BLACK;
   }

   @Override
   public long getMaxPower() {
      return this.getCurrentOutput() * 10000L;
   }

   @Override
   public long minPowerReceived() {
      return 0L;
   }

   @Override
   public long maxPowerReceived() {
      return 2000L * MjAPI.MJ;
   }

   @Override
   public long maxPowerExtracted() {
      return 20L * this.getCurrentOutput();
   }

   @Override
   public float explosionRange() {
      return 0.0F;
   }

   @Override
   public long getCurrentOutput() {
      return OUTPUTS[MathUtil.clamp(this.currentOutputIndex, 0, OUTPUTS.length - 1)] * MjAPI.MJ;
   }

   public boolean onWrenchInteract(Player player) {
      if (this.level != null && !this.level.isClientSide()) {
         this.currentOutputIndex = (this.currentOutputIndex + 1) % OUTPUTS.length;
         MessageUtil.sendOverlayMessage(player, Component.translatable("chat.pipe.power.iron.mode", new Object[]{OUTPUTS[this.currentOutputIndex]}));
         this.setChanged();
         BlockState state = this.getBlockState();
         this.level.sendBlockUpdated(this.getBlockPos(), state, state, 3);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      super.getDebugInfo(left, right, side);
      left.add("Output = " + MjAPI.formatMj(this.getCurrentOutput()) + "/t");
   }

   @Override
   public void getClientDebugInfo(List<String> left, List<String> right, Direction side) {
      super.getClientDebugInfo(left, right, side);
   }

   @Override
   protected void writeData(BcValueOut output) {
      super.writeData(output);
      output.putInt("currentOutputIndex", this.currentOutputIndex);
   }

   @Override
   public void readData(BcValueIn input) {
      super.readData(input);
      this.currentOutputIndex = input.getIntOr("currentOutputIndex", 0);
      this.currentOutputIndex = MathUtil.clamp(this.currentOutputIndex, 0, OUTPUTS.length - 1);
   }
}
