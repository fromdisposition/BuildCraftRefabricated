/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.statement;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.silicon.BCSiliconSprites;
import buildcraft.silicon.BCSiliconStatements;
import java.util.Locale;
import net.minecraft.world.level.Level;

public class TriggerTimer extends BCStatement implements ITriggerInternal {
   private final TriggerTimer.Duration duration;

   public TriggerTimer(TriggerTimer.Duration duration) {
      super("buildcraft:timer_" + duration.name().toLowerCase(Locale.ROOT));
      this.duration = duration;
   }

   @Override
   public String getDescription() {
      return String.format(LocaleUtil.localize("gate.trigger.timer"), this.duration.duration);
   }

   @Override
   public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
      if (source.getTile() != null && source.getTile().getLevel() != null) {
         Level level = source.getTile().getLevel();
         return level.getGameTime() % (20L * this.duration.duration) == 0L;
      } else {
         return false;
      }
   }

   @Override
   public IStatement[] getPossible() {
      return BCSiliconStatements.TRIGGER_TIMER;
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return switch (this.duration) {
         case SHORT -> BCSiliconSprites.TRIGGER_TIMER_SHORT;
         case MEDIUM -> BCSiliconSprites.TRIGGER_TIMER_MEDIUM;
         case LONG -> BCSiliconSprites.TRIGGER_TIMER_LONG;
      };
   }

   public enum Duration {
      SHORT(5),
      MEDIUM(10),
      LONG(15);

      public final int duration;

      Duration(int duration) {
         this.duration = duration;
      }
   }
}
