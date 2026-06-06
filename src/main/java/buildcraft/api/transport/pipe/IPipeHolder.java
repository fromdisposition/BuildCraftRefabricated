package buildcraft.api.transport.pipe;

import buildcraft.api.statements.containers.IRedstoneStatementContainer;
import buildcraft.api.transport.IWireManager;
import buildcraft.api.transport.pluggable.PipePluggable;
import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IPipeHolder extends IRedstoneStatementContainer {
   Level getPipeWorld();

   BlockPos getPipePos();

   BlockEntity getPipeTile();

   IPipe getPipe();

   boolean canPlayerInteract(Player var1);

   @Nullable
   PipePluggable getPluggable(Direction var1);

   @Nullable
   BlockEntity getNeighbourTile(Direction var1);

   @Nullable
   IPipe getNeighbourPipe(Direction var1);

   IWireManager getWireManager();

   GameProfile getOwner();

   boolean fireEvent(PipeEvent var1);

   void scheduleRenderUpdate();

   void scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver... var1);

   void scheduleNetworkGuiUpdate(IPipeHolder.PipeMessageReceiver... var1);

   void sendMessage(IPipeHolder.PipeMessageReceiver var1, IPipeHolder.IWriter var2);

   void sendGuiMessage(IPipeHolder.PipeMessageReceiver var1, IPipeHolder.IWriter var2);

   void onPlayerOpen(Player var1);

   void onPlayerClose(Player var1);

   default void wakePipe() {
   }

   interface IWriter {
      void write(FriendlyByteBuf var1);
   }

   enum PipeMessageReceiver {
      BEHAVIOUR(null),
      FLOW(null),
      PLUGGABLE_DOWN(Direction.DOWN),
      PLUGGABLE_UP(Direction.UP),
      PLUGGABLE_NORTH(Direction.NORTH),
      PLUGGABLE_SOUTH(Direction.SOUTH),
      PLUGGABLE_WEST(Direction.WEST),
      PLUGGABLE_EAST(Direction.EAST);

      public static final IPipeHolder.PipeMessageReceiver[] VALUES = values();
      public static final IPipeHolder.PipeMessageReceiver[] PLUGGABLES = new IPipeHolder.PipeMessageReceiver[6];
      public final Direction face;

      PipeMessageReceiver(Direction face) {
         this.face = face;
      }

      static {
         for (IPipeHolder.PipeMessageReceiver type : VALUES) {
            if (type.face != null) {
               PLUGGABLES[type.face.ordinal()] = type;
            }
         }
      }
   }
}
