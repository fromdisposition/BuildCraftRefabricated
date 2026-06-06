package buildcraft.transport.net;

import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.transport.client.PipeHolderClientExtensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public record MessagePipeLandingEffect(BlockPos pos, double x, double y, double z, int numberOfParticles) implements CustomPacketPayload {
   public static final Type<MessagePipeLandingEffect> TYPE = new Type(Identifier.parse("buildcraftrefabricated:pipe_landing_effect"));
   public static final StreamCodec<RegistryFriendlyByteBuf, MessagePipeLandingEffect> STREAM_CODEC = StreamCodec.composite(
      BlockPos.STREAM_CODEC,
      MessagePipeLandingEffect::pos,
      ByteBufCodecs.DOUBLE,
      MessagePipeLandingEffect::x,
      ByteBufCodecs.DOUBLE,
      MessagePipeLandingEffect::y,
      ByteBufCodecs.DOUBLE,
      MessagePipeLandingEffect::z,
      ByteBufCodecs.VAR_INT,
      MessagePipeLandingEffect::numberOfParticles,
      MessagePipeLandingEffect::new
   );

   public Type<MessagePipeLandingEffect> type() {
      return TYPE;
   }

   public static void handle(MessagePipeLandingEffect message, BCPayloadContext ctx) {
      Level level = ctx.player().level();
      if (level != null && level.isClientSide()) {
         PipeHolderClientExtensions.spawnLandingParticles(level, message.pos, message.x, message.y, message.z, message.numberOfParticles);
      }
   }
}
