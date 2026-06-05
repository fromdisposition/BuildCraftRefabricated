package buildcraft.transport.net;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.transport.client.PipeHolderClientExtensions;

public record MessagePipeLandingEffect(
        BlockPos pos,
        double x, double y, double z,
        int numberOfParticles
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MessagePipeLandingEffect> TYPE =
            new CustomPacketPayload.Type<>(Identifier.parse("buildcraftrefabricated:pipe_landing_effect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MessagePipeLandingEffect> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, MessagePipeLandingEffect::pos,
                    net.minecraft.network.codec.ByteBufCodecs.DOUBLE, MessagePipeLandingEffect::x,
                    net.minecraft.network.codec.ByteBufCodecs.DOUBLE, MessagePipeLandingEffect::y,
                    net.minecraft.network.codec.ByteBufCodecs.DOUBLE, MessagePipeLandingEffect::z,
                    net.minecraft.network.codec.ByteBufCodecs.VAR_INT, MessagePipeLandingEffect::numberOfParticles,
                    MessagePipeLandingEffect::new
            );

    @Override
    public Type<MessagePipeLandingEffect> type() {
        return TYPE;
    }

    public static void handle(MessagePipeLandingEffect message, BCPayloadContext ctx) {
        Level level = ctx.player().level();
        if (level != null && level.isClientSide()) {
            PipeHolderClientExtensions.spawnLandingParticles(level, message.pos, message.x, message.y, message.z,
                    message.numberOfParticles);
        }
    }
}
