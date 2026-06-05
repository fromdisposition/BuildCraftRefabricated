/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders;

import buildcraft.lib.config.ConfigSpec;

public class BCBuildersConfig {

    public static ConfigSpec.IntValue quarryFrameMinHeight;

    public static ConfigSpec.IntValue quarryMaxTasksPerTick;

    public static ConfigSpec.IntValue quarryTaskPowerDivisor;

    public static ConfigSpec.DoubleValue quarryMaxFrameMoveSpeed;

    public static ConfigSpec.DoubleValue quarryMaxBlockMineRate;

    public static void ensureLoaded() {
        if (quarryFrameMinHeight != null) {
            return;
        }
        buildGeneral(new ConfigSpec.Builder());
    }

    public static void buildGeneral(ConfigSpec.Builder builder) {
        builder.push("builders");

        builder.push("quarry");

        quarryFrameMinHeight = builder
                .comment("The minimum height that all quarry frames must be.",
                         "A value of 1 will look strange when it drills the uppermost layer.")
                .defineInRange("quarryFrameMinHeight", 4, 1, 256);

        quarryMaxTasksPerTick = builder
                .comment("The maximum number of tasks that the quarry will do per tick.",
                         "(Where a task is either breaking a block, or moving the frame)")
                .defineInRange("quarryMaxTasksPerTick", 4, 1, 20);

        quarryTaskPowerDivisor = builder
                .comment("1 divided by this value is added to the power cost for each additional task done per tick.",
                         "A value of 0 disables this behaviour.")
                .defineInRange("quarryTaskPowerDivisor", 2, 0, 100);

        quarryMaxFrameMoveSpeed = builder
                .comment("The maximum number of blocks that a quarry is allowed to move, per second.",
                         "A value of 0 means no limit.")
                .defineInRange("quarryMaxFrameMoveSpeed", 0.0, 0.0, 5120.0);

        quarryMaxBlockMineRate = builder
                .comment("The maximum number of blocks that the quarry is allowed to mine each second.",
                         "A value of 0 means no limit, and a value of 0.5 will mine up to half a block per second.")
                .defineInRange("quarryMaxBlockMineRate", 0.0, 0.0, 1000.0);

        builder.pop();
        builder.pop();
    }
}
