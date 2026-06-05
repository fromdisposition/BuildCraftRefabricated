package buildcraft.lib.fabric.mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import buildcraft.lib.tile.IBlockEntityLoadHook;
import buildcraft.lib.tile.TileMarker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {
    @Inject(method = "setLevel", at = @At("TAIL"))
    private void buildcraft$markerAttached(Level level, CallbackInfo ci) {
        BlockEntity self = (BlockEntity) (Object) this;
        if (self instanceof TileMarker<?> marker) {
            marker.buildcraft$onAttachedToLevel();
        }
    }

    @Inject(method = "clearRemoved", at = @At("TAIL"))
    private void buildcraft$onLoad(CallbackInfo ci) {
        if (this instanceof IBlockEntityLoadHook loadHook
                && ((BlockEntity) (Object) this).getLevel() != null) {
            loadHook.onLoad();
        }
    }

    @Inject(method = "setRemoved", at = @At("HEAD"))
    private void buildcraft$markerChunkUnloadHook(CallbackInfo ci) {
        BlockEntity self = (BlockEntity) (Object) this;
        if (!(self instanceof TileMarker<?> marker)) {
            return;
        }
        Level level = self.getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        if (level.hasChunkAt(self.getBlockPos()) && level.getBlockState(self.getBlockPos()).hasBlockEntity()) {
            marker.buildcraft$onChunkUnloading();
        }
    }
}
