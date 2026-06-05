package buildcraft.energy.tile;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.core.tile.ITileOilSpring;
import buildcraft.energy.BCEnergyBlockEntities;
import buildcraft.lib.misc.AdvancementUtil;

public class TileSpringOil extends BlockEntity implements ITileOilSpring {

    private static final Identifier ADVANCEMENT = Identifier.parse("buildcraftfactory:black_gold");

    private final Map<GameProfile, PlayerPumpInfo> pumpProgress = new ConcurrentHashMap<>();

    public int totalSources;

    public TileSpringOil(BlockPos pos, BlockState state) {
        super(BCEnergyBlockEntities.SPRING_OIL, pos, state);
    }

    @Override
    public void onPumpOil(GameProfile profile, BlockPos oilPos) {
        if (profile == null) {
            return;
        }
        PlayerPumpInfo info = pumpProgress.computeIfAbsent(profile, PlayerPumpInfo::new);
        info.lastPumpTick = level.getGameTime();
        info.sourcesPumped++;

        if (info.sourcesPumped >= totalSources * 7 / 8) {
            if (oilPos.equals(getBlockPos().above())) {
                if (profile.id() != null) {
                    AdvancementUtil.unlockAdvancement(profile.id(), level, ADVANCEMENT);
                }
            }
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        totalSources = input.getIntOr("totalSources", 0);
        int pumpCount = input.getIntOr("pumpCount", 0);
        for (int i = 0; i < pumpCount; i++) {
            String prefix = "pump_" + i + "_";
            String name = input.getStringOr(prefix + "name", "Unknown");
            String idStr = input.getStringOr(prefix + "id", "");
            UUID id = null;
            try {
                if (!idStr.isEmpty()) id = UUID.fromString(idStr);
            } catch (IllegalArgumentException ignored) {}
            long tick = input.getLongOr(prefix + "tick", -1);
            int pumped = input.getIntOr(prefix + "pumped", 0);
            GameProfile gp = new GameProfile(id, name);
            PlayerPumpInfo info = new PlayerPumpInfo(gp);
            info.lastPumpTick = tick;
            info.sourcesPumped = pumped;
            pumpProgress.put(gp, info);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("totalSources", totalSources);
        output.putInt("pumpCount", pumpProgress.size());
        int i = 0;
        for (PlayerPumpInfo info : pumpProgress.values()) {
            String prefix = "pump_" + i + "_";
            if (info.profile.name() != null) {
                output.putString(prefix + "name", info.profile.name());
            }
            if (info.profile.id() != null) {
                output.putString(prefix + "id", info.profile.id().toString());
            }
            output.putLong(prefix + "tick", info.lastPumpTick);
            output.putInt(prefix + "pumped", info.sourcesPumped);
            i++;
        }
    }

    static class PlayerPumpInfo {
        final GameProfile profile;
        long lastPumpTick = -1;
        int sourcesPumped = 0;

        public PlayerPumpInfo(GameProfile profile) {
            this.profile = profile;
        }
    }
}
