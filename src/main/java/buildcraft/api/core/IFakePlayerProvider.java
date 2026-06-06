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
