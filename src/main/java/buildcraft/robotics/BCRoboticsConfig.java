/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import buildcraft.lib.BCLibConfig;

/** Robot pacing knobs, loaded from the "robotics" section of the common config (see BCFabricConfig). All values
 * are read live at their use sites, so a config reload applies immediately; the loader clamps them to sane ranges
 * so a hand-edited file cannot stall or overload the AI. */
public final class BCRoboticsConfig {
   /** Flight speed in blocks per tick (vanilla-era default 0.15 = 3 blocks/s). Clamped to [0.02, 1.0]. */
   public static final BCLibConfig.DoubleValue flightSpeed = new BCLibConfig.DoubleValue(0.15);
   /** Block-state checks a scanning robot may spend per tick -- the real per-tick cost cap of a search. The cheap
    * iteration budget scales with it (x20). Clamped to [50, 2000]. */
   public static final BCLibConfig.IntValue scanBudgetPerTick = new BCLibConfig.IntValue(200);
   /** Seconds a robot rests docked before re-checking for work. Clamped to [1, 60]. */
   public static final BCLibConfig.IntValue sleepSeconds = new BCLibConfig.IntValue(3);
   /** Ticks between melee swings of a fighting robot (20 = one hit per second). Clamped to [5, 100]. */
   public static final BCLibConfig.IntValue attackPeriodTicks = new BCLibConfig.IntValue(20);
   /** Multiplier on block-breaking speed. The full energy price is still paid -- this only changes how much of it
    * a robot may spend per tick. Clamped to [0.25, 10.0]. */
   public static final BCLibConfig.DoubleValue workSpeedMultiplier = new BCLibConfig.DoubleValue(1.0);

   private BCRoboticsConfig() {
   }
}
