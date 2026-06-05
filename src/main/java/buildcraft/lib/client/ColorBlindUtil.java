/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.client;

import net.minecraft.client.Minecraft;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.BCLibConfig.ColorBlindMode;

public final class ColorBlindUtil {

    private ColorBlindUtil() {}

    public static boolean isActive() {
        if (BCLibConfig.colorBlindMode == null) {
            return false;
        }
        ColorBlindMode mode = BCLibConfig.colorBlindMode.get();
        switch (mode) {
            case ON:
                return true;
            case OFF:
                return false;
            case AUTO:
            default:
                Minecraft mc = Minecraft.getInstance();
                if (mc == null || mc.options == null) {

                    return false;
                }
                return mc.options.highContrast().get();
        }
    }
}
