package buildcraft.transport.container;

import java.util.EnumMap;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import buildcraft.api.core.BCLog;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.Widget_Neptune;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.transport.BCTransportMenuTypes;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;
import buildcraft.transport.tile.TilePipeHolder;

import buildcraft.fabric.network.BCPayloadContext;

@SuppressWarnings("this-escape")
public class ContainerEmzuliPipe_BC8 extends ContainerBC_Neptune {
    @javax.annotation.Nullable
    public final PipeBehaviourEmzuli behaviour;
    public final EnumMap<SlotIndex, PaintWidget> paintWidgets = new EnumMap<>(SlotIndex.class);
    @javax.annotation.Nullable
    private final IPipeHolder pipeHolder;

    public ContainerEmzuliPipe_BC8(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv, getBehaviour(playerInv, pos));
    }

    public ContainerEmzuliPipe_BC8(int containerId, Inventory playerInv, PipeBehaviourEmzuli behaviour) {
        super(BCTransportMenuTypes.EMZULI_PIPE, containerId, playerInv.player);
        this.behaviour = behaviour;
        if (behaviour == null) {
            this.pipeHolder = null;
            addFullPlayerInventory(8, 84);
            return;
        }
        this.pipeHolder = behaviour.pipe.getHolder();

        addSlot(new SlotPhantom(behaviour.invFilters, 0, 25, 21));
        addSlot(new SlotPhantom(behaviour.invFilters, 1, 25, 49));
        addSlot(new SlotPhantom(behaviour.invFilters, 2, 134, 21));
        addSlot(new SlotPhantom(behaviour.invFilters, 3, 134, 49));

        addFullPlayerInventory(8, 84);

        for (SlotIndex index : SlotIndex.VALUES) {
            PaintWidget widget = new PaintWidget(this, index);
            addWidget(widget);
            paintWidgets.put(index, widget);
        }
    }

    private static PipeBehaviourEmzuli getBehaviour(Inventory playerInv, BlockPos pos) {
        if (playerInv.player.level() != null) {
            var be = playerInv.player.level().getBlockEntity(pos);
            if (be instanceof TilePipeHolder holder && holder.getPipe() != null) {
                if (holder.getPipe().getBehaviour() instanceof PipeBehaviourEmzuli emzuli) {
                    return emzuli;
                }
            }
        }
        BCLog.logger.warn("[transport.gui] No emzuli pipe behaviour at {}", pos);
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return pipeHolder != null && pipeHolder.canPlayerInteract(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {

        return ItemStack.EMPTY;
    }

    public static class PaintWidget extends Widget_Neptune<ContainerEmzuliPipe_BC8> {
        public final SlotIndex index;

        public PaintWidget(ContainerEmzuliPipe_BC8 container, SlotIndex index) {
            super(container);
            this.index = index;
        }

        public void setColour(DyeColor colour) {
            sendWidgetData((buffer) -> {
                buffer.writeByte(colour == null ? -1 : colour.getId());
            });
        }

        @Override
        public void handleWidgetDataServer(BCPayloadContext ctx, PacketBufferBC buffer) {
            int c = buffer.readByte();
            DyeColor colour = (c >= 0 && c < 16) ? DyeColor.byId(c) : null;
            if (colour == null) {
                container.behaviour.slotColours.remove(index);
            } else {
                container.behaviour.slotColours.put(index, colour);
            }
            container.behaviour.pipe.getHolder().scheduleNetworkGuiUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }
}
