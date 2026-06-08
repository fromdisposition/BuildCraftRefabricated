/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.tile;

import buildcraft.core.tile.ITileOilSpring;
import buildcraft.energy.BCEnergyBlockEntities;
import buildcraft.lib.misc.AdvancementUtil;
import com.mojang.authlib.GameProfile;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileSpringOil extends BlockEntity implements ITileOilSpring {
   private static final Identifier ADVANCEMENT = Identifier.parse("buildcraftfactory:black_gold");
   private final Map<GameProfile, TileSpringOil.PlayerPumpInfo> pumpProgress = new ConcurrentHashMap<>();
   public int totalSources;

   public TileSpringOil(BlockPos pos, BlockState state) {
      super(BCEnergyBlockEntities.SPRING_OIL, pos, state);
   }

   @Override
   public void onPumpOil(GameProfile profile, BlockPos oilPos) {
      if (profile != null) {
         TileSpringOil.PlayerPumpInfo info = this.pumpProgress.computeIfAbsent(profile, TileSpringOil.PlayerPumpInfo::new);
         info.lastPumpTick = this.level.getGameTime();
         info.sourcesPumped++;
         if (info.sourcesPumped >= this.totalSources * 7 / 8 && oilPos.equals(this.getBlockPos().above()) && profile.id() != null) {
            AdvancementUtil.unlockAdvancement(profile.id(), this.level, ADVANCEMENT);
         }
      }
   }

   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.totalSources = input.getIntOr("totalSources", 0);
      int pumpCount = input.getIntOr("pumpCount", 0);

      for (int i = 0; i < pumpCount; i++) {
         String prefix = "pump_" + i + "_";
         String name = input.getStringOr(prefix + "name", "Unknown");
         String idStr = input.getStringOr(prefix + "id", "");
         UUID id = null;

         try {
            if (!idStr.isEmpty()) {
               id = UUID.fromString(idStr);
            }
         } catch (IllegalArgumentException var13) {
         }

         long tick = input.getLongOr(prefix + "tick", -1L);
         int pumped = input.getIntOr(prefix + "pumped", 0);
         GameProfile gp = new GameProfile(id, name);
         TileSpringOil.PlayerPumpInfo info = new TileSpringOil.PlayerPumpInfo(gp);
         info.lastPumpTick = tick;
         info.sourcesPumped = pumped;
         this.pumpProgress.put(gp, info);
      }
   }

   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putInt("totalSources", this.totalSources);
      output.putInt("pumpCount", this.pumpProgress.size());
      int i = 0;

      for (TileSpringOil.PlayerPumpInfo info : this.pumpProgress.values()) {
         String prefix = "pump_" + i + "_";
         if (info.profile.name() != null) {
            output.putString(prefix + "name", info.profile.name());
         }

         if (info.profile.id() != null) {
            output.putString(prefix + "id", info.profile.id().toString());
         }

         output.putLong(prefix + "tick", info.lastPumpTick);
         output.putInt(prefix + "pumped", info.sourcesPumped);
         i++;
      }
   }

   static class PlayerPumpInfo {
      final GameProfile profile;
      long lastPumpTick = -1L;
      int sourcesPumped = 0;

      public PlayerPumpInfo(GameProfile profile) {
         this.profile = profile;
      }
   }
}
