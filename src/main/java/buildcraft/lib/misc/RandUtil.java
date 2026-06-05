/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.util.Random;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class RandUtil {

    public static Random createRandomForChunk(Level level, int chunkX, int chunkZ, long magicNumber) {
        long worldSeed = level instanceof ServerLevel sl ? sl.getSeed() : 0L;
        return createRandomForChunk(worldSeed, chunkX, chunkZ, magicNumber);
    }

    public static Random createRandomForChunk(long worldSeed, int chunkX, int chunkZ, long magicNumber) {

        Random worldRandom = new Random(worldSeed);
        long xSeed = worldRandom.nextLong() >> 2 + 1L;
        long zSeed = worldRandom.nextLong() >> 2 + 1L;
        long chunkSeed = (xSeed * chunkX + zSeed * chunkZ) ^ worldSeed;

        chunkSeed ^= magicNumber;
        return new Random(chunkSeed);
    }
}
