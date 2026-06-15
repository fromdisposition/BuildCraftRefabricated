/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.IMjReadable;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.api.mj.MjAPI;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TriggerPower extends BCStatement implements ITriggerInternal, ITriggerExternal {
   private final boolean high;

   public TriggerPower(boolean high) {
      super("buildcraft:energyStored" + (high ? "high" : "low"));
      this.high = high;
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return this.high ? BCCoreSprites.TRIGGER_POWER_HIGH : BCCoreSprites.TRIGGER_POWER_LOW;
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize("gate.trigger.machine.energyStored." + (this.high ? "high" : "low"));
   }

   public boolean isTriggeredMjConnector(IMjReadable readable) {
      if (readable == null) {
         return false;
      } else {
         long stored = readable.getStored();
         long max = readable.getCapacity();
         if (max > 0L) {
            double level = (double)stored / max;
            return this.high ? level > 0.95 : level < 0.05;
         } else {
            return false;
         }
      }
   }

   public static boolean isTriggeringTile(BlockEntity tile) {
      return isTriggeringTile(tile, null);
   }

   public static boolean isTriggeringTile(BlockEntity tile, Direction face) {
      if (tile instanceof IMjReadable) {
         return true;
      }

      Level level = tile.getLevel();
      if (level != null) {
         IMjReceiver receiver = MjAPI.CAP_RECEIVER.find(level, tile.getBlockPos(), null, tile, face);
         if (receiver instanceof IMjReadable) {
            return true;
         }
      }

      return false;
   }

   private static IMjReadable resolveReadable(Object tile, EnumPipePart side) {
      if (tile instanceof IMjReadable readable) {
         return readable;
      } else {
         return tile instanceof BlockEntity be
               && be.getLevel() != null
               && MjAPI.CAP_RECEIVER.find(be.getLevel(), be.getBlockPos(), null, be, side.face) instanceof IMjReadable readable
            ? readable
            : null;
      }
   }

   protected boolean isActive(Object tile, EnumPipePart side) {
      return this.isTriggeredMjConnector(resolveReadable(tile, side));
   }

   @Override
   public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
      return this.isActive(source.getTile(), EnumPipePart.CENTER);
   }

   @Override
   public boolean isTriggerActive(BlockEntity target, Direction side, IStatementContainer source, IStatementParameter[] parameters) {
      return this.isActive(target, EnumPipePart.fromFacing(side.getOpposite()));
   }

   @Override
   public IStatement[] getPossible() {
      return BCCoreStatements.TRIGGER_POWER;
   }
}
