package buildcraft.transport.container;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.transport.BCTransportMenuTypes;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;

public class ContainerDiamondWoodPipe extends AbstractPipeFilterContainer<PipeBehaviourWoodDiamond> {
   private static final int NET_FILTER_MODE = 1;

   public ContainerDiamondWoodPipe(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, resolveBehaviour(playerInv, pos, PipeBehaviourWoodDiamond.class, "wood-diamond pipe"));
   }

   public ContainerDiamondWoodPipe(int containerId, Inventory playerInv, PipeBehaviourWoodDiamond behaviour) {
      super(BCTransportMenuTypes.DIAMOND_WOOD_PIPE, containerId, playerInv, behaviour, behaviour == null ? null : behaviour.pipe.getHolder());
      if (behaviour == null) {
         this.addFullPlayerInventory(8, 79);
      } else {
         for (int i = 0; i < 9; i++) {
            this.addSlot(new SlotPhantom(behaviour.filters, i, 8 + i * 18, 18));
         }

         this.addFullPlayerInventory(8, 79);
      }
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
}
