package buildcraft.transport.container;

import buildcraft.api.core.BCLog;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.gui.BcMenu;
import buildcraft.lib.gui.Widget_Neptune;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.transport.BCTransportMenuTypes;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli;
import buildcraft.transport.tile.TilePipeHolder;
import java.util.EnumMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class ContainerEmzuliPipe_BC8 extends BcMenu {
   @Nullable
   public final PipeBehaviourEmzuli behaviour;
   public final EnumMap<PipeBehaviourEmzuli.SlotIndex, ContainerEmzuliPipe_BC8.PaintWidget> paintWidgets = new EnumMap<>(PipeBehaviourEmzuli.SlotIndex.class);
   @Nullable
   private final IPipeHolder pipeHolder;

   public ContainerEmzuliPipe_BC8(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, getBehaviour(playerInv, pos));
   }

   public ContainerEmzuliPipe_BC8(int containerId, Inventory playerInv, PipeBehaviourEmzuli behaviour) {
      super(BCTransportMenuTypes.EMZULI_PIPE, containerId, playerInv.player);
      this.behaviour = behaviour;
      if (behaviour == null) {
         this.pipeHolder = null;
         this.addFullPlayerInventory(8, 84);
      } else {
         this.pipeHolder = behaviour.pipe.getHolder();
         this.addSlot(new SlotPhantom(behaviour.invFilters, 0, 25, 21));
         this.addSlot(new SlotPhantom(behaviour.invFilters, 1, 25, 49));
         this.addSlot(new SlotPhantom(behaviour.invFilters, 2, 134, 21));
         this.addSlot(new SlotPhantom(behaviour.invFilters, 3, 134, 49));
         this.addFullPlayerInventory(8, 84);

         for (PipeBehaviourEmzuli.SlotIndex index : PipeBehaviourEmzuli.SlotIndex.VALUES) {
            ContainerEmzuliPipe_BC8.PaintWidget widget = new ContainerEmzuliPipe_BC8.PaintWidget(this, index);
            this.addWidget(widget);
            this.paintWidgets.put(index, widget);
         }

         this.pipeHolder.onPlayerOpen(playerInv.player);
      }
   }

   public void removed(Player player) {
      super.removed(player);
      if (this.pipeHolder != null) {
         this.pipeHolder.onPlayerClose(player);
      }
   }

   private static PipeBehaviourEmzuli getBehaviour(Inventory playerInv, BlockPos pos) {
      if (playerInv.player.level() != null
         && playerInv.player.level().getBlockEntity(pos) instanceof TilePipeHolder holder
         && holder.getPipe() != null
         && holder.getPipe().getBehaviour() instanceof PipeBehaviourEmzuli emzuli) {
         return emzuli;
      }

      BCLog.logger.warn("[transport.gui] No emzuli pipe behaviour at {}", pos);
      return null;
   }

   @Override
   public boolean stillValid(Player player) {
      return this.pipeHolder != null && this.pipeHolder.canPlayerInteract(player);
   }

   @Override
   public ItemStack quickMoveStack(Player player, int slotIndex) {
      return ItemStack.EMPTY;
   }

   public static class PaintWidget extends Widget_Neptune<ContainerEmzuliPipe_BC8> {
      public final PipeBehaviourEmzuli.SlotIndex index;

      public PaintWidget(ContainerEmzuliPipe_BC8 container, PipeBehaviourEmzuli.SlotIndex index) {
         super(container);
         this.index = index;
      }

      public void setColour(DyeColor colour) {
         this.sendWidgetData(buffer -> buffer.writeByte(colour == null ? -1 : colour.getId()));
      }

      @Override
      public void handleWidgetDataServer(BCPayloadContext ctx, PacketBufferBC buffer) {
         int c = buffer.readByte();
         DyeColor colour = c >= 0 && c < 16 ? DyeColor.byId(c) : null;
         if (colour == null) {
            this.container.behaviour.slotColours.remove(this.index);
         } else {
            this.container.behaviour.slotColours.put(this.index, colour);
         }

         this.container.behaviour.pipe.getHolder().scheduleNetworkGuiUpdate(IPipeHolder.PipeMessageReceiver.BEHAVIOUR);
      }
   }
}
