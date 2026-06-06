package buildcraft.transport.container;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.api.core.BCLog;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.gui.BcMenu;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.transport.BCTransportMenuTypes;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond;
import buildcraft.transport.tile.TilePipeHolder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ContainerDiamondWoodPipe extends BcMenu {
   private static final int NET_FILTER_MODE = 1;
   @Nullable
   private final IPipeHolder pipeHolder;
   @Nullable
   public final PipeBehaviourWoodDiamond behaviour;

   public ContainerDiamondWoodPipe(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, getBehaviour(playerInv, pos));
   }

   public ContainerDiamondWoodPipe(int containerId, Inventory playerInv, PipeBehaviourWoodDiamond behaviour) {
      super(BCTransportMenuTypes.DIAMOND_WOOD_PIPE, containerId, playerInv.player);
      this.behaviour = behaviour;
      if (behaviour == null) {
         this.pipeHolder = null;
         this.addFullPlayerInventory(8, 79);
      } else {
         this.pipeHolder = behaviour.pipe.getHolder();

         for (int i = 0; i < 9; i++) {
            this.addSlot(new SlotPhantom(behaviour.filters, i, 8 + i * 18, 18));
         }

         this.addFullPlayerInventory(8, 79);
         this.pipeHolder.onPlayerOpen(playerInv.player);
      }
   }

   public void removed(Player player) {
      super.removed(player);
      if (this.pipeHolder != null) {
         this.pipeHolder.onPlayerClose(player);
      }
   }

   private static PipeBehaviourWoodDiamond getBehaviour(Inventory playerInv, BlockPos pos) {
      if (playerInv.player.level() != null
         && playerInv.player.level().getBlockEntity(pos) instanceof TilePipeHolder holder
         && holder.getPipe() != null
         && holder.getPipe().getBehaviour() instanceof PipeBehaviourWoodDiamond wd) {
         return wd;
      }

      BCLog.logger.warn("[transport.gui] No wood-diamond pipe behaviour at {}", pos);
      return null;
   }

   public void sendNewFilterMode(PipeBehaviourWoodDiamond.FilterMode newFilterMode) {
      this.sendMessage(1, buffer -> buffer.writeEnum(newFilterMode));
   }

   @Override
   public void readMessage(int id, FriendlyByteBuf buffer, boolean isClient, BCPayloadContext ctx) {
      super.readMessage(id, buffer, isClient, ctx);
      if (id == 1 && !isClient && this.behaviour != null) {
         this.behaviour.filterMode = (PipeBehaviourWoodDiamond.FilterMode)buffer.readEnum(PipeBehaviourWoodDiamond.FilterMode.class);
         this.behaviour.pipe.getHolder().scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.BEHAVIOUR);
      }
   }

   @Override
   public boolean stillValid(Player player) {
      return this.pipeHolder != null && this.pipeHolder.canPlayerInteract(player);
   }

   @Override
   public ItemStack quickMoveStack(Player player, int slotIndex) {
      return ItemStack.EMPTY;
   }
}
