package buildcraft.lib.platform.menu;

import buildcraft.core.marker.volume.EnumAddonSlot;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FillerPlannerMenuKey(UUID boxId, EnumAddonSlot slot) {
   public static final StreamCodec<RegistryFriendlyByteBuf, FillerPlannerMenuKey> STREAM_CODEC = StreamCodec.composite(
      UUIDUtil.STREAM_CODEC,
      FillerPlannerMenuKey::boxId,
      ByteBufCodecs.idMapper(i -> EnumAddonSlot.values()[i], Enum::ordinal),
      FillerPlannerMenuKey::slot,
      FillerPlannerMenuKey::new
   );
}
