/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.core;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface IFakePlayerProvider {
   ServerPlayer getBuildCraftPlayer(ServerLevel var1);

   ServerPlayer getFakePlayer(ServerLevel var1, GameProfile var2);

   ServerPlayer getFakePlayer(ServerLevel var1, GameProfile var2, BlockPos var3);
}
