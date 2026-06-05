package buildcraft.api.transport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;

public interface IItemPluggable {

    @Nullable
    PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, Direction side, Player player,
        InteractionHand hand);

    @Nonnull
    default AABB getPlacementBoundingBox(@Nonnull ItemStack stack, Direction side) {
        return DefaultPlacementBoxes.BOXES[side.get3DDataValue()];
    }

    final class DefaultPlacementBoxes {
        private DefaultPlacementBoxes() {}

        static final AABB[] BOXES = new AABB[6];
        static {
            double a = 5 / 16.0, b = 11 / 16.0;
            double near = 2 / 16.0, nearEnd = 4 / 16.0, far = 12 / 16.0, farEnd = 14 / 16.0;
            BOXES[Direction.DOWN.get3DDataValue()]  = new AABB(a, near, a, b, nearEnd, b);
            BOXES[Direction.UP.get3DDataValue()]    = new AABB(a, far, a, b, farEnd, b);
            BOXES[Direction.NORTH.get3DDataValue()] = new AABB(a, a, near, b, b, nearEnd);
            BOXES[Direction.SOUTH.get3DDataValue()] = new AABB(a, a, far, b, b, farEnd);
            BOXES[Direction.WEST.get3DDataValue()]  = new AABB(near, a, a, nearEnd, b, b);
            BOXES[Direction.EAST.get3DDataValue()]  = new AABB(far, a, a, farEnd, b, b);
        }
    }
}
