package buildcraft.transport.container;

import buildcraft.api.core.BCLog;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.lib.gui.BcMenu;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.transport.BCTransportMenuTypes;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDiamond;
import buildcraft.transport.tile.TilePipeHolder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ContainerDiamondPipe extends BcMenu {
   @Nullable
   private final IPipeHolder pipeHolder;
   @Nullable
   private final PipeBehaviourDiamond behaviour;

   public ContainerDiamondPipe(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, getBehaviour(playerInv, pos));
   }

   public ContainerDiamondPipe(int containerId, Inventory playerInv, PipeBehaviourDiamond behaviour) {
      super(BCTransportMenuTypes.DIAMOND_PIPE, containerId, playerInv.player);
      this.behaviour = behaviour;
      if (behaviour == null) {
         this.pipeHolder = null;
         this.addFullPlayerInventory(8, 140);
      } else {
         this.pipeHolder = behaviour.pipe.getHolder();

         for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 9; x++) {
               this.addSlot(new SlotPhantom(behaviour.filters, x + y * 9, 8 + x * 18, 18 + y * 18));
            }
         }

         this.addFullPlayerInventory(8, 140);
         this.pipeHolder.onPlayerOpen(playerInv.player);
      }
   }

   public void removed(Player player) {
      super.removed(player);
      if (this.pipeHolder != null) {
         this.pipeHolder.onPlayerClose(player);
      }
   }

   private static PipeBehaviourDiamond getBehaviour(Inventory playerInv, BlockPos pos) {
      if (playerInv.player.level() != null
         && playerInv.player.level().getBlockEntity(pos) instanceof TilePipeHolder holder
         && holder.getPipe() != null
         && holder.getPipe().getBehaviour() instanceof PipeBehaviourDiamond diamond) {
         return diamond;
      }

      BCLog.logger.warn("[transport.gui] No diamond pipe behaviour at {}", pos);
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
}
