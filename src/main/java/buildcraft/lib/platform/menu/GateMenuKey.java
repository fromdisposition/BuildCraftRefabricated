package buildcraft.lib.platform.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record GateMenuKey(BlockPos pos, Direction side) {
   public static final StreamCodec<RegistryFriendlyByteBuf, GateMenuKey> STREAM_CODEC = StreamCodec.composite(
      BlockPos.STREAM_CODEC, GateMenuKey::pos, ByteBufCodecs.idMapper(i -> Direction.values()[i], Enum::ordinal), GateMenuKey::side, GateMenuKey::new
   );
}
