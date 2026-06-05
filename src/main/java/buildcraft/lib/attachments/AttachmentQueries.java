package buildcraft.lib.attachments;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class AttachmentQueries {
    private AttachmentQueries() {}

    public static <T, C> @Nullable T getBlock(Level level, BlockAttachment<T, C> capability, BlockPos pos, C context) {
        return capability.getCapability(level, pos, null, null, context);
    }

    public static <T, C> @Nullable T getBlock(
            Level level,
            BlockAttachment<T, C> capability,
            BlockPos pos,
            net.minecraft.world.level.block.state.BlockState state,
            net.minecraft.world.level.block.entity.BlockEntity blockEntity,
            C context) {
        return capability.getCapability(level, pos, state, blockEntity, context);
    }

    public static <T, C> @Nullable T getItem(ItemStack stack, ItemAttachment<T, C> capability, C context) {
        return capability.getCapability(stack, context);
    }

    public static <T, C> @Nullable T getEntity(Entity entity, EntityAttachment<T, C> capability, C context) {
        return capability.getCapability(entity, context);
    }

    public static void invalidate(Level level, BlockPos pos) {
        if (level instanceof CapabilityInvalidationLevel invalidation) {
            invalidation.buildcraft$invalidateCapabilities(pos);
        }
    }

    public interface CapabilityInvalidationLevel {
        void buildcraft$invalidateCapabilities(BlockPos pos);
    }

    public interface CapabilityListenerLevel {
        void buildcraft$registerCapabilityListener(BlockPos pos, IAttachmentInvalidationListener invalidationListener);
    }
}
