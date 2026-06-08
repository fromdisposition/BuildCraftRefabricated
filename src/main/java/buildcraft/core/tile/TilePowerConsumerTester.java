/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.tile;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.core.BCCoreBlockEntities;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.tile.BcBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TilePowerConsumerTester extends BcBlockEntity implements IMjReceiver, IDebuggable {
   private long lastReceived;
   private long nextTickReceived;
   private long lastTickReceived;
   private long totalReceived;

   public TilePowerConsumerTester(BlockPos pos, BlockState state) {
      super(BCCoreBlockEntities.POWER_TESTER, pos, state);
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.lastReceived = input.getLongOr("last", 0L);
      this.nextTickReceived = input.getLongOr("nt", 0L);
      this.lastTickReceived = input.getLongOr("lt", 0L);
      this.totalReceived = input.getLongOr("total", 0L);
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putLong("last", this.lastReceived);
      output.putLong("nt", this.nextTickReceived);
      output.putLong("lt", this.lastTickReceived);
      output.putLong("total", this.totalReceived);
   }

   public void serverTick() {
      this.lastTickReceived = this.nextTickReceived;
      this.nextTickReceived = 0L;
   }

   @Override
   public boolean canConnect(IMjConnector other) {
      return true;
   }

   @Override
   public long getPowerRequested() {
      return 100000L * MjAPI.MJ;
   }

   @Override
   public long receivePower(long microJoules, boolean simulate) {
      if (!simulate) {
         this.lastReceived = microJoules;
         this.nextTickReceived += microJoules;
         this.totalReceived += microJoules;
      }

      return 0L;
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      left.add("");
      left.add("Last received = " + LocaleUtil.localizeMj(this.lastReceived));
      left.add("Tick received = " + LocaleUtil.localizeMj(this.lastTickReceived));
      left.add("Total received = " + LocaleUtil.localizeMj(this.totalReceived));
   }
}
