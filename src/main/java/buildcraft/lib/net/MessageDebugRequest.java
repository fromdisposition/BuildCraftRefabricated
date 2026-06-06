package buildcraft.lib.net;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.item.ItemDebugger;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record MessageDebugRequest(BlockPos pos, Direction side) implements CustomPacketPayload {
   public static final Type<MessageDebugRequest> TYPE = new Type(Identifier.parse("buildcraftrefabricated:debug_request"));
   public static final StreamCodec<RegistryFriendlyByteBuf, MessageDebugRequest> STREAM_CODEC = StreamCodec.of(
      MessageDebugRequest::encode, MessageDebugRequest::decode
   );

   private static void encode(RegistryFriendlyByteBuf buf, MessageDebugRequest msg) {
      buf.writeBlockPos(msg.pos);
      buf.writeEnum(msg.side);
   }

   private static MessageDebugRequest decode(RegistryFriendlyByteBuf buf) {
      BlockPos pos = buf.readBlockPos();
      Direction side = (Direction)buf.readEnum(Direction.class);
      return new MessageDebugRequest(pos, side);
   }

   public Type<MessageDebugRequest> type() {
      return TYPE;
   }

   public static void handle(MessageDebugRequest message, BCPayloadContext ctx) {
      if (ctx.player() instanceof ServerPlayer player) {
         if (!ItemDebugger.isShowDebugInfo(player)) {
            BcPacketDistributor.sendToPlayer(player, new MessageDebugResponse(List.of(), List.of()));
         } else if (!(player.distanceToSqr(message.pos.getX() + 0.5, message.pos.getY() + 0.5, message.pos.getZ() + 0.5) > 64.0)) {
            if (player.level().getBlockEntity(message.pos) instanceof IDebuggable debuggable) {
               List<String> left = new ArrayList<>();
               List<String> right = new ArrayList<>();
               debuggable.getDebugInfo(left, right, message.side);
               BcPacketDistributor.sendToPlayer(player, new MessageDebugResponse(left, right));
            }
         }
      }
   }
}
