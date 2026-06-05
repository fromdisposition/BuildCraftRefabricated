package buildcraft.lib.fabric;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.lib.attachments.BlockAttachment;

public interface AttachmentLevelAccess {
    static AttachmentLevelAccess of(Level level) {
        return (AttachmentLevelAccess) (Object) level;
    }

    @Nullable
    <T> T getCapability(BlockAttachment<T, @Nullable Direction> capability, BlockPos pos, @Nullable Direction side);

    @Nullable
    <T> T getCapability(
            BlockAttachment<T, @Nullable Direction> capability,
            BlockPos pos,
            @Nullable BlockState state,
            @Nullable BlockEntity blockEntity,
            @Nullable Direction side);

    void invalidateCapabilities(BlockPos pos);
}
