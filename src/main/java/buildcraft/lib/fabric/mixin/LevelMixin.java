package buildcraft.lib.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import buildcraft.lib.block.LocalBlockUpdateNotifier;
import buildcraft.lib.attachments.BlockAttachment;
import buildcraft.lib.attachments.AttachmentQueries;
import buildcraft.lib.fabric.AttachmentLevelAccess;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin implements AttachmentLevelAccess {
    @Nullable
    public <T> T getCapability(BlockAttachment<T, @Nullable Direction> capability, BlockPos pos, @Nullable Direction side) {
        return capability.getCapability((Level) (Object) this, pos, null, null, side);
    }

    @Nullable
    public <T> T getCapability(
            BlockAttachment<T, @Nullable Direction> capability,
            BlockPos pos,
            @Nullable BlockState state,
            @Nullable BlockEntity blockEntity,
            @Nullable Direction side) {
        return capability.getCapability((Level) (Object) this, pos, state, blockEntity, side);
    }

    public void invalidateCapabilities(BlockPos pos) {
        AttachmentQueries.invalidate((Level) (Object) this, pos);
    }

    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("RETURN"),
            require = 0)
    private void buildcraft$onSetBlockReturn(BlockPos pos, BlockState newState, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        buildcraft$finishSetBlock(pos, newState, flags, cir.getReturnValue());
    }

    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
            at = @At("RETURN"),
            require = 0)
    private void buildcraft$onSetBlockReturnCompat(BlockPos pos, BlockState newState, int flags, CallbackInfoReturnable<Boolean> cir) {
        buildcraft$finishSetBlock(pos, newState, flags, cir.getReturnValue());
    }

    private void buildcraft$finishSetBlock(BlockPos pos, BlockState newState, int flags, boolean success) {
        if (!success) {
            return;
        }
        Level level = (Level) (Object) this;
        AttachmentQueries.invalidate(level, pos);

        LocalBlockUpdateNotifier.onLevelBlockStateChanged(level, pos, newState, newState, flags);
    }
}
