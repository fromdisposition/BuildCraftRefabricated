package buildcraft.lib.fabric;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IFakePlayerProvider;
import com.mojang.authlib.GameProfile;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopping;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class BCLibFakePlayerProvider implements IFakePlayerProvider {
   public static final GameProfile NULL_PROFILE = new GameProfile(FakePlayer.DEFAULT_UUID, "[BuildCraft]");
   private static final BCLibFakePlayerProvider INSTANCE = new BCLibFakePlayerProvider();
   private final Map<GameProfile, FakePlayer> players = new HashMap<>();

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
      profile = normalizeProfile(profile);
      FakePlayer player = this.players.computeIfAbsent(profile, p -> FakePlayer.get(world, p));
      reposition(player, world, pos);
      return player;
   }

   private static void reposition(FakePlayer player, ServerLevel world, BlockPos pos) {
      double x = pos.getX() + 0.5;
      double y = pos.getY() + 0.5;
      double z = pos.getZ() + 0.5;
      if (player.level() != world) {
         player.teleportTo(world, x, y, z, Set.of(), player.getYRot(), player.getXRot(), false);
      } else {
         player.setPos(x, y, z);
      }
   }

   private static GameProfile normalizeProfile(@Nullable GameProfile profile) {
      if (profile == null || profile.id() == null) {
         return NULL_PROFILE;
      } else {
         return profile.name() != null && !profile.name().isEmpty() ? profile : new GameProfile(profile.id(), "[BuildCraft]");
      }
   }

   public void unloadWorld(ServerLevel world) {
      this.players.values().removeIf(player -> player.level() == world);
   }

   private void unloadAll() {
      this.players.clear();
   }

   public static void register() {
      BuildCraftAPI.fakePlayerProvider = INSTANCE;
      ServerLifecycleEvents.SERVER_STOPPING.register((ServerStopping)server -> INSTANCE.unloadAll());
   }
}
