/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import buildcraft.api.statements.StatementManager;
import buildcraft.silicon.statement.ActionPowerPulsar;
import buildcraft.silicon.statement.TriggerLightSensor;
import buildcraft.silicon.statement.TriggerTimer;

public class BCSiliconStatements {
   public static final TriggerLightSensor TRIGGER_LIGHT_LOW = new TriggerLightSensor(false);
   public static final TriggerLightSensor TRIGGER_LIGHT_HIGH = new TriggerLightSensor(true);
   public static final TriggerLightSensor[] TRIGGER_LIGHT = new TriggerLightSensor[]{TRIGGER_LIGHT_LOW, TRIGGER_LIGHT_HIGH};
   public static final TriggerTimer TRIGGER_TIMER_SHORT = new TriggerTimer(TriggerTimer.Duration.SHORT);
   public static final TriggerTimer TRIGGER_TIMER_MEDIUM = new TriggerTimer(TriggerTimer.Duration.MEDIUM);
   public static final TriggerTimer TRIGGER_TIMER_LONG = new TriggerTimer(TriggerTimer.Duration.LONG);
   public static final TriggerTimer[] TRIGGER_TIMER = new TriggerTimer[]{TRIGGER_TIMER_SHORT, TRIGGER_TIMER_MEDIUM, TRIGGER_TIMER_LONG};
   public static final ActionPowerPulsar ACTION_PULSAR_CONSTANT = new ActionPowerPulsar(true);
   public static final ActionPowerPulsar ACTION_PULSAR_SINGLE = new ActionPowerPulsar(false);
   public static final ActionPowerPulsar[] ACTION_PULSAR = new ActionPowerPulsar[]{ACTION_PULSAR_CONSTANT, ACTION_PULSAR_SINGLE};

   public static void preInit() {
      StatementManager.registerStatement(TRIGGER_LIGHT_LOW);
      StatementManager.registerStatement(TRIGGER_LIGHT_HIGH);
      StatementManager.registerStatement(TRIGGER_TIMER_SHORT);
      StatementManager.registerStatement(TRIGGER_TIMER_MEDIUM);
      StatementManager.registerStatement(TRIGGER_TIMER_LONG);
      StatementManager.registerStatement(ACTION_PULSAR_CONSTANT);
      StatementManager.registerStatement(ACTION_PULSAR_SINGLE);
   }
}
