package buildcraft.lib.fabric;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IFakePlayerProvider;
import buildcraft.lib.common.util.FakePlayer;

public final class BCLibFakePlayerProvider implements IFakePlayerProvider {
    private static final GameProfile BC_PROFILE = new GameProfile(
            java.util.UUID.fromString("6B8C778A-6E1F-4F6A-8F96-000000000BCA"),
            "[BuildCraft]");

    @Override
    public ServerPlayer getBuildCraftPlayer(ServerLevel world) {
        return getFakePlayer(world, BC_PROFILE);
    }

    @Override
    public ServerPlayer getFakePlayer(ServerLevel world, GameProfile profile) {
        return new FakePlayer(world, profile);
    }

    @Override
    public ServerPlayer getFakePlayer(ServerLevel world, GameProfile profile, BlockPos pos) {
        FakePlayer player = new FakePlayer(world, profile);
        player.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        return player;
    }

    public static void register() {
        BuildCraftAPI.fakePlayerProvider = new BCLibFakePlayerProvider();
    }
}
