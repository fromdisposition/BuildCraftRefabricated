/*
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p>
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.core;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import net.minecraft.server.level.ServerPlayer;

public interface IFakePlayerProvider {

    ServerPlayer getBuildCraftPlayer(ServerLevel world);

    ServerPlayer getFakePlayer(ServerLevel world, GameProfile profile);

    ServerPlayer getFakePlayer(ServerLevel world, GameProfile profile, BlockPos pos);
}
