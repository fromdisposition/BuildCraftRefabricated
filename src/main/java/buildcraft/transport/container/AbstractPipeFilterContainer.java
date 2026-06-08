package buildcraft.transport.container;

import buildcraft.api.core.BCLog;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.lib.gui.BcMenu;
import buildcraft.transport.tile.TilePipeHolder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractPipeFilterContainer<B> extends BcMenu {
   @Nullable
   protected final IPipeHolder pipeHolder;
   @Nullable
   public final B behaviour;

   protected AbstractPipeFilterContainer(MenuType<?> menuType, int containerId, Inventory playerInv, @Nullable B behaviour, @Nullable IPipeHolder pipeHolder) {
      super(menuType, containerId, playerInv.player);
      this.behaviour = behaviour;
      this.pipeHolder = pipeHolder;
      if (this.pipeHolder != null) {
         this.pipeHolder.onPlayerOpen(playerInv.player);
      }
   }

   @Nullable
   protected static <B> B resolveBehaviour(Inventory playerInv, BlockPos pos, Class<B> behaviourClass, String warnLabel) {
      if (playerInv.player.level() != null
         && playerInv.player.level().getBlockEntity(pos) instanceof TilePipeHolder holder
         && holder.getPipe() != null
         && behaviourClass.isInstance(holder.getPipe().getBehaviour())) {
         return behaviourClass.cast(holder.getPipe().getBehaviour());
      }

      BCLog.logger.warn("[transport.gui] No {} behaviour at {}", warnLabel, pos);
      return null;
   }

   @Override
   public void removed(Player player) {
      super.removed(player);
      if (this.pipeHolder != null) {
         this.pipeHolder.onPlayerClose(player);
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
