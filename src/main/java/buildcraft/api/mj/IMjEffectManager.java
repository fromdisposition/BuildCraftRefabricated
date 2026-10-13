/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.mj;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface IMjEffectManager {
   void createPowerLossEffect(Level var1, Vec3 var2, long var3);

   void createPowerLossEffect(Level var1, Vec3 var2, Direction var3, long var4);

   void createPowerLossEffect(Level var1, Vec3 var2, Vec3 var3, long var4);
}
