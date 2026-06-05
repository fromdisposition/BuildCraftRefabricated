package buildcraft.robotics;

import buildcraft.fabric.registry.DeferredRegister;
import buildcraft.fabric.registry.DeferredBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import buildcraft.lib.BCLib;
import buildcraft.robotics.block.BlockZonePlanner;

public class BCRoboticsBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BCRobotics.MODID);

    public static final DeferredBlock<BlockZonePlanner> ZONE_PLANNER;

    static {
        ZONE_PLANNER = BCLib.DEV
                ? BLOCKS.registerBlock("zone_planner", BlockZonePlanner::new,
                        () -> BlockBehaviour.Properties.of().strength(5.0f, 10.0f).sound(SoundType.METAL).requiresCorrectToolForDrops())
                : null;
    }

    public static void register() {
        BLOCKS.register();
    }
}

