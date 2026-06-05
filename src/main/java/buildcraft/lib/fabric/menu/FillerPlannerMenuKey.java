package buildcraft.lib.fabric.menu;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import buildcraft.core.marker.volume.EnumAddonSlot;

public record FillerPlannerMenuKey(UUID boxId, EnumAddonSlot slot) {
    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, FillerPlannerMenuKey> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    FillerPlannerMenuKey::boxId,
                    ByteBufCodecs.idMapper(i -> EnumAddonSlot.values()[i], EnumAddonSlot::ordinal),
                    FillerPlannerMenuKey::slot,
                    FillerPlannerMenuKey::new);
}
