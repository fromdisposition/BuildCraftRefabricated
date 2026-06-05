package buildcraft.silicon.plug;

import java.io.IOException;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import buildcraft.silicon.item.ItemGateCopier;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.network.chat.Component;

import buildcraft.api.transport.IWireEmitter;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.silicon.client.model.key.KeyPlugGate;

import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.data.ModelVariableData;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.client.model.key.KeyPlugGate;
import buildcraft.silicon.gate.GateLogic;
import buildcraft.silicon.gate.GateVariant;

@SuppressWarnings("this-escape")
public class PluggableGate extends PipePluggable implements IWireEmitter {

    private static final byte ID_UPDATE_PLUG = 1;

    private static final AABB[] BOXES = new AABB[6];

    private static final Identifier ADVANCEMENT_PLACE_GATE
        = Identifier.parse("buildcrafttransport:pipe_logic");

    private static final Identifier ADVANCEMENT_PLACE_ADV_GATE
        = Identifier.parse("buildcrafttransport:extended_logic");

    public final GateLogic logic;

    public final ModelVariableData clientModelData = new ModelVariableData();

    static {
        double ll = 2 / 16.0;
        double lu = 4 / 16.0;
        double ul = 12 / 16.0;
        double uu = 14 / 16.0;

        double min = 5 / 16.0;
        double max = 11 / 16.0;

        BOXES[Direction.DOWN.get3DDataValue()] = new AABB(min, ll, min, max, lu, max);
        BOXES[Direction.UP.get3DDataValue()] = new AABB(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.get3DDataValue()] = new AABB(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.get3DDataValue()] = new AABB(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.get3DDataValue()] = new AABB(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.get3DDataValue()] = new AABB(ul, min, min, uu, max, max);
    }

    public PluggableGate(PluggableDefinition def, IPipeHolder holder, Direction side, GateVariant variant) {
        super(def, holder, side);
        logic = new GateLogic(this, variant);
    }

    public PluggableGate(PluggableDefinition def, IPipeHolder holder, Direction side, CompoundTag nbt) {
        super(def, holder, side);
        logic = new GateLogic(this, nbt.getCompound("data").orElse(new CompoundTag()));
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        nbt.put("data", logic.writeToNbt());
        return nbt;
    }

    @Override
    public boolean readFromNbt(CompoundTag nbt) {
        CompoundTag data = nbt.getCompound("data").orElse(new CompoundTag());
        logic.readConfigData(data);
        return true;
    }

    @Override
    public CompoundTag writeClientUpdateData() {
        return logic.writeClientState();
    }

    @Override
    public void readClientUpdateData(CompoundTag nbt) {
        logic.readClientState(nbt);
    }

    public PluggableGate(PluggableDefinition def, IPipeHolder holder, Direction side, net.minecraft.network.FriendlyByteBuf buffer) {
        super(def, holder, side);
        PacketBufferBC packetBuffer = new PacketBufferBC(buffer);
        logic = new GateLogic(this, packetBuffer);
    }

    @Override
    public void writeCreationPayload(net.minecraft.network.FriendlyByteBuf buffer) {
        super.writeCreationPayload(buffer);
        PacketBufferBC packetBuffer = new PacketBufferBC(buffer);
        logic.writeCreationToBuf(packetBuffer);
    }

    public void sendMessage(IPayloadWriter writer) {
        PipeMessageReceiver to = PipeMessageReceiver.PLUGGABLES[side.ordinal()];
        holder.sendMessage(to, (buffer) -> {

            buffer.writeByte(ID_UPDATE_PLUG);
            writer.write(buffer);
        });
    }

    public void sendGuiMessage(IPayloadWriter writer) {
        PipeMessageReceiver to = PipeMessageReceiver.PLUGGABLES[side.ordinal()];
        holder.sendGuiMessage(to, (buffer) -> {

            buffer.writeByte(ID_UPDATE_PLUG);
            writer.write(buffer);
        });
    }

    @Override
    public void writePayload(FriendlyByteBuf buffer, Object side) {
        throw new Error("All messages must have an ID, and we can't just write a payload directly!");
    }

    @Override
    public void readPayload(FriendlyByteBuf b, Object side, Object ctx) throws IOException {
        PacketBufferBC packetBuffer = new PacketBufferBC(b);
        byte id = packetBuffer.readByte();

        if (id == ID_UPDATE_PLUG) {
            logic.readPayload(packetBuffer, ((Boolean) ctx).booleanValue());
        }
    }

    @Override
    public AABB getBoundingBox() {
        return BOXES[side.get3DDataValue()];
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public ItemStack getPickStack() {
        return buildcraft.silicon.BCSiliconItems.PLUG_GATE.get().getStack(logic.variant);
    }

    @Override
    public PluggableModelKey getModelRenderKey(Object layer) {
        if ("cutout".equals(layer)) {
            return new KeyPlugGate(side, logic.variant, logic.isOn);
        }
        return null;
    }

    @Override
    public void onPlacedBy(Player player) {
        super.onPlacedBy(player);
        if (!holder.getPipeWorld().isClientSide()) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PLACE_GATE);
            if (logic.variant.numActionArgs >= 1) {
                AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PLACE_ADV_GATE);
            }
        }
        buildcraft.transport.BCTransportAttachments.recordPluggablePlacement(
            player, buildcraft.transport.BCTransportAttachments.PluggablesPlaced.Kind.GATE);
    }

    @Override
    public boolean onPluggableActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ) {
        if (!player.level().isClientSide()) {
            if (interactWithCopier(player, player.getMainHandItem())) {
                return true;
            }
            if (interactWithCopier(player, player.getOffhandItem())) {
                return true;
            }

            BlockPos pos = holder.getPipePos();
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                buildcraft.lib.fabric.menu.GateMenuKey key =
                        new buildcraft.lib.fabric.menu.GateMenuKey(pos, side);
                serverPlayer.openMenu(new net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<>() {
                    @Override
                    public buildcraft.lib.fabric.menu.GateMenuKey getScreenOpeningData(
                            net.minecraft.server.level.ServerPlayer sp) {
                        return key;
                    }

                    @Override
                    public Component getDisplayName() {
                        return logic.variant.getLocalizedName();
                    }

                    @Override
                    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                            int id, net.minecraft.world.entity.player.Inventory inv, Player p) {
                        return new buildcraft.silicon.container.ContainerGate(id, inv, PluggableGate.this);
                    }
                });
            }
        }
        return true;
    }

    private boolean interactWithCopier(Player player, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemGateCopier)) {
            return false;
        }

        CompoundTag stored = ItemGateCopier.getCopiedGateData(stack);
        if (stored != null) {

            logic.readConfigData(stored);
            if (holder instanceof net.minecraft.world.level.block.entity.BlockEntity be) {
                be.setChanged();
            }
            holder.scheduleRenderUpdate();

            logic.sendResolveData();
            buildcraft.lib.misc.MessageUtil.sendOverlayMessage(player,Component.translatable("chat.gateCopier.gatePasted"));
        } else {

            if (!logic.hasConfiguration()) {
                buildcraft.lib.misc.MessageUtil.sendOverlayMessage(player,Component.translatable("chat.gateCopier.noInformation"));
                return false;
            }
            CompoundTag data = logic.writeToNbt();

            data.remove("wireBroadcasts");
            ItemGateCopier.setCopiedGateData(stack, data);
            buildcraft.lib.misc.MessageUtil.sendOverlayMessage(player,Component.translatable("chat.gateCopier.gateCopied"));
        }
        return true;
    }

    @Override
    public boolean isEmitting(DyeColor colour) {
        return logic.isEmitting(colour);
    }

    @Override
    public void emitWire(DyeColor colour) {
        logic.emitWire(colour);
    }

    @Override
    public void onTick() {
        logic.onTick();
        if (holder.getPipeWorld().isClientSide()) {
            clientModelData.tick();
        }
    }

    @Override
    public boolean canConnectToRedstone(@Nullable Direction to) {
        return true;
    }
}
