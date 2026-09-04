/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import buildcraft.lib.BCLibConfig;

// Read live at every use site, so a reload applies at once; the loader clamps each value to the range noted.
public final class BCRoboticsConfig {
   // Blocks per tick; [0.02, 1.0].
   public static final BCLibConfig.DoubleValue flightSpeed = new BCLibConfig.DoubleValue(0.15);
   // Block-state checks per tick; the cheap iteration budget is 20x this. [50, 2000].
   public static final BCLibConfig.IntValue scanBudgetPerTick = new BCLibConfig.IntValue(200);
   // Seconds; [1, 60].
   public static final BCLibConfig.IntValue sleepSeconds = new BCLibConfig.IntValue(3);
   // Ticks between melee swings; [5, 100].
   public static final BCLibConfig.IntValue attackPeriodTicks = new BCLibConfig.IntValue(20);
   // Caps per-tick energy spend only; the block still costs its full price. [0.25, 10.0].
   public static final BCLibConfig.DoubleValue workSpeedMultiplier = new BCLibConfig.DoubleValue(1.0);

   private BCRoboticsConfig() {
   }
}
