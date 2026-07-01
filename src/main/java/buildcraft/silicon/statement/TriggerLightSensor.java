/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.statement;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternalSided;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.silicon.BCSiliconSprites;
import buildcraft.silicon.BCSiliconStatements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TriggerLightSensor extends BCStatement implements ITriggerInternalSided {
   private final boolean bright;

   public TriggerLightSensor(boolean bright) {
      super("buildcraft:light_" + (bright ? "bright" : "dark"));
      this.bright = bright;
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize("gate.trigger.light." + (this.bright ? "bright" : "dark"));
   }

   @Override
   public boolean isTriggerActive(Direction side, IStatementContainer source, IStatementParameter[] parameters) {
      BlockEntity tile = source.getTile();
      if (tile != null && tile.getLevel() != null) {
         BlockPos pos = tile.getBlockPos().relative(side);
         int light = tile.getLevel().getMaxLocalRawBrightness(pos);
         return light < 8 ^ this.bright;
      } else {
         return false;
      }
   }

   @Override
   public IStatement[] getPossible() {
      return BCSiliconStatements.TRIGGER_LIGHT;
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return this.bright ? BCSiliconSprites.TRIGGER_LIGHT_HIGH : BCSiliconSprites.TRIGGER_LIGHT_LOW;
   }
}
