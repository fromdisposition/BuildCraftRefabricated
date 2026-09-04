package buildcraft.lib.fabric;

import buildcraft.lib.nbt.BcAuth;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IFakePlayerProvider;
import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

// No cache of its own: Fabric's FakePlayer cache is already per (level, profile), concurrent and weak-valued,
// so the player returned always belongs to the given level and setPos never has to cross dimensions.
public final class BCLibFakePlayerProvider implements IFakePlayerProvider {
   public static final GameProfile NULL_PROFILE = new GameProfile(FakePlayer.DEFAULT_UUID, "[BuildCraft]");
   private static final BCLibFakePlayerProvider INSTANCE = new BCLibFakePlayerProvider();

   @Override
   public ServerPlayer getBuildCraftPlayer(ServerLevel world) {
      return this.getFakePlayer(world, NULL_PROFILE);
   }

   @Override
   public ServerPlayer getFakePlayer(ServerLevel world, GameProfile profile) {
      return this.getFakePlayer(world, profile, BlockPos.ZERO);
   }

   @Override
   public ServerPlayer getFakePlayer(ServerLevel world, GameProfile profile, BlockPos pos) {
      FakePlayer player = FakePlayer.get(world, normalizeProfile(profile));
      player.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
      return player;
   }

   private static GameProfile normalizeProfile(@Nullable GameProfile profile) {
      if (profile == null || BcAuth.id(profile) == null) {
         return NULL_PROFILE;
      } else {
         return BcAuth.name(profile) != null && !BcAuth.name(profile).isEmpty() ? profile : new GameProfile(BcAuth.id(profile), "[BuildCraft]");
      }
   }

   public static void register() {
      BuildCraftAPI.fakePlayerProvider = INSTANCE;
   }
}
