/* Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.core;

import net.minecraft.world.level.Level;

public class SafeTimeTracker {

    private long lastMark = Long.MIN_VALUE;
    private long duration = -1;
    private long randomRange = 0;
    private long lastRandomDelay = 0;
    private long internalDelay = 1;

    public SafeTimeTracker() {

    }

    public SafeTimeTracker(long delay) {
        internalDelay = delay;
    }

    public SafeTimeTracker(long delay, long random) {
        internalDelay = delay;
        randomRange = random;
    }

    public boolean markTimeIfDelay(Level world) {
        return markTimeIfDelay(world, internalDelay);
    }

    public boolean markTimeIfDelay(Level world, long delay) {
        if (world == null) {
            return false;
        }

        long currentTime = world.getGameTime();

        if (currentTime < lastMark) {
            lastMark = currentTime;
            return false;
        } else if (lastMark + delay + lastRandomDelay <= currentTime) {
            duration = currentTime - lastMark;
            lastMark = currentTime;
            lastRandomDelay = (int) (Math.random() * randomRange);

            return true;
        } else {
            return false;
        }
    }

    public long durationOfLastDelay() {
        return duration > 0 ? duration : 0;
    }

    public void markTime(Level world) {
        lastMark = world.getGameTime();
    }
}
