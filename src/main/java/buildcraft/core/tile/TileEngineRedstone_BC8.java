/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.tile;

import buildcraft.lib.nbt.BcAuth;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.core.BCCoreBlockEntities;
import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.AdvancementUtil;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

public class TileEngineRedstone_BC8 extends TileEngineBase_BC8 {
   private static final Identifier ADVANCEMENT = Identifier.parse("buildcraftcore:free_power");
   private boolean givenAdvancement = false;

   public TileEngineRedstone_BC8(BlockPos pos, BlockState state) {
      super(BCCoreBlockEntities.ENGINE_REDSTONE, pos, state);
   }

   @Nonnull
   @Override
   protected IMjConnector createConnector() {
      return new EngineConnector(true);
   }

   @Override
   public boolean isBurning() {
      return this.isRedstonePowered;
   }

   @Override
   protected void engineUpdate() {
      if (this.isRedstonePowered) {
         this.power = this.getMaxPower();
         if (this.level != null && this.level.getGameTime() % 16L == 0L) {
            if (this.getHeatLevel() < 0.8F) {
               this.heat += 4.0F;
            }

            if (this.isPumping && !this.givenAdvancement && this.getOwner() != null) {
               this.givenAdvancement = AdvancementUtil.unlockAdvancement(BcAuth.id(this.getOwner()), this.level, ADVANCEMENT);
            }
         }
      } else {
         this.power = 0L;
      }
   }

   @Override
   public double getPistonSpeed() {
      return super.getPistonSpeed() / 2.0;
   }

   @Override
   public void updateHeatLevel() {
      if (this.heat > 20.0F) {
         this.heat -= 0.2F;
         if (this.heat < 20.0F) {
            this.heat = 20.0F;
         }
      }
   }

   @Override
   protected int getMaxChainLength() {
      return 0;
   }

   @Override
   public long getMaxPower() {
      return MjAPI.MJ;
   }

   @Override
   public long minPowerReceived() {
      return MjAPI.MJ / 10L;
   }

   @Override
   public long maxPowerReceived() {
      return 4L * MjAPI.MJ;
   }

   @Override
   public long maxPowerExtracted() {
      return 4L * MjAPI.MJ;
   }

   @Override
   public float explosionRange() {
      return 0.0F;
   }

   @Override
   public long getCurrentOutput() {
      return MjAPI.MJ / 20L;
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      super.getDebugInfo(left, right, side);
   }

   @Override
   public void getClientDebugInfo(List<String> left, List<String> right, Direction side) {
      super.getClientDebugInfo(left, right, side);
   }
}
