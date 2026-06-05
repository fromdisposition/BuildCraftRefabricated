package buildcraft.lib.block;

import net.minecraft.resources.Identifier;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ILocalBlockUpdateSubscriber {

    BlockPos getSubscriberPos();

    int getUpdateRange();

    void setLevelUpdated(Level world, BlockPos eventPos, BlockState oldState, BlockState newState, int flags);
}
