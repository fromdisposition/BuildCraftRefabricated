package buildcraft.api.transport.pluggable;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.level.block.Block;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import buildcraft.lib.attachments.IAttachmentProvider;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;

public abstract class PipePluggable {
    public final PluggableDefinition definition;
    public final IPipeHolder holder;
    public final Direction side;

    public PipePluggable(PluggableDefinition definition, IPipeHolder holder, Direction side) {
        this.definition = definition;
        this.holder = holder;
        this.side = side;
    }

    public CompoundTag writeToNbt() {
        CompoundTag nbt = new CompoundTag();
        return nbt;
    }

    public boolean readFromNbt(CompoundTag nbt) {
        return false;
    }

    public CompoundTag writeClientUpdateData() {
        return new CompoundTag();
    }

    public void readClientUpdateData(CompoundTag nbt) {}

    public void writeCreationPayload(FriendlyByteBuf buffer) {

    }

    public void writePayload(FriendlyByteBuf buffer, Object side) {

    }

    public void readPayload(FriendlyByteBuf buffer, Object side, Object ctx) throws IOException {

    }

    public final void scheduleNetworkUpdate() {
        holder.scheduleNetworkUpdate(PipeMessageReceiver.PLUGGABLES[side.ordinal()]);
    }

    public void onTick() {}

    public abstract AABB getBoundingBox();

    public boolean isBlocking() {
        return false;
    }

    public <T> T getCapability(@Nonnull Object cap) {
        return null;
    }

    public <T> T getInternalCapability(@Nonnull Object cap) {
        return null;
    }

    public void onRemove() {}

    public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
        ItemStack stack = getPickStack();
        if (!stack.isEmpty()) {
            toDrop.add(stack);
        }
    }

    public ItemStack getPickStack() {
        return ItemStack.EMPTY;
    }

    public boolean onPluggableActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ) {
        return false;
    }

    @Nullable
    public PluggableModelKey getModelRenderKey(Object layer) {
        return null;
    }

    public boolean canBeConnected() {
        return false;
    }

    public boolean isSideSolid() {
        return false;
    }

    public float getExplosionResistance(@Nullable Entity exploder, Explosion explosion) {
        return 0;
    }

    public boolean canConnectToRedstone(@Nullable Direction to) {
        return false;
    }

    public Object getBlockFaceShape() {
        return null;
    }

    public void onPlacedBy(Player player) {

    }
}
