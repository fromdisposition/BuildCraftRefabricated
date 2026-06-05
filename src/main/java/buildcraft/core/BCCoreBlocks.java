package buildcraft.core;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import buildcraft.api.enums.EnumSpring;
import buildcraft.core.block.BlockEngineCreative;
import buildcraft.core.block.BlockEngineRedstone_BC8;
import buildcraft.core.block.BlockMarkerPath;
import buildcraft.core.block.BlockMarkerVolume;
import buildcraft.core.block.BlockPowerConsumerTester;
import buildcraft.core.block.BlockSpring;
import buildcraft.fabric.BCRegistries;
import buildcraft.lib.BCLib;

public class BCCoreBlocks {
    public static BlockSpring SPRING_WATER;
    public static BlockSpring SPRING_OIL;
    public static Block DECORATED_LASER;

    public static Block DECORATED_DESTROY;
    public static Block DECORATED_BLUEPRINT;
    public static Block DECORATED_TEMPLATE;
    public static Block DECORATED_PAPER;
    public static Block DECORATED_LEATHER;

    public static BlockMarkerVolume MARKER_VOLUME;
    public static BlockMarkerPath MARKER_PATH;

    public static BlockEngineRedstone_BC8 ENGINE_REDSTONE;
    public static BlockEngineCreative ENGINE_CREATIVE;

    public static BlockPowerConsumerTester POWER_TESTER;

    private BCCoreBlocks() {}

    public static void register() {
        SPRING_WATER = BCRegistries.registerBlock(BCCore.MODID, "spring_water",
                props -> new BlockSpring(EnumSpring.WATER, props),
                p -> p.sound(SoundType.STONE));
        SPRING_OIL = BCRegistries.registerBlock(BCCore.MODID, "spring_oil",
                props -> new BlockSpring(EnumSpring.OIL, props),
                p -> p.sound(SoundType.STONE));

        DECORATED_LASER = BCRegistries.registerBlock(BCCore.MODID, "decorated_laser",
                Block::new,
                p -> p.strength(3.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());

        MARKER_VOLUME = BCRegistries.registerBlock(BCCore.MODID, "marker_volume",
                BlockMarkerVolume::new,
                p -> p.sound(SoundType.METAL));
        MARKER_PATH = BCRegistries.registerBlock(BCCore.MODID, "marker_path",
                BlockMarkerPath::new,
                p -> p.sound(SoundType.METAL));

        ENGINE_REDSTONE = BCRegistries.registerBlock(BCCore.MODID, "engine_redstone",
                BlockEngineRedstone_BC8::new,
                p -> p.strength(3.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());
        ENGINE_CREATIVE = BCRegistries.registerBlock(BCCore.MODID, "engine_creative",
                BlockEngineCreative::new,
                p -> p.strength(3.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());

        if (BCLib.DEV) {
            POWER_TESTER = BCRegistries.registerBlock(BCCore.MODID, "power_tester",
                    BlockPowerConsumerTester::new,
                    p -> p.strength(3.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());
            DECORATED_DESTROY = BCRegistries.registerBlock(BCCore.MODID, "decorated_destroy",
                    Block::new,
                    p -> p.strength(3.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noLootTable());
            DECORATED_BLUEPRINT = BCRegistries.registerBlock(BCCore.MODID, "decorated_blueprint",
                    Block::new,
                    p -> p.strength(3.0f).lightLevel(s -> 10).sound(SoundType.METAL).requiresCorrectToolForDrops().noLootTable());
            DECORATED_TEMPLATE = BCRegistries.registerBlock(BCCore.MODID, "decorated_template",
                    Block::new,
                    p -> p.strength(3.0f).lightLevel(s -> 10).sound(SoundType.METAL).requiresCorrectToolForDrops().noLootTable());
            DECORATED_PAPER = BCRegistries.registerBlock(BCCore.MODID, "decorated_paper",
                    Block::new,
                    p -> p.strength(3.0f).lightLevel(s -> 10).sound(SoundType.METAL).requiresCorrectToolForDrops().noLootTable());
            DECORATED_LEATHER = BCRegistries.registerBlock(BCCore.MODID, "decorated_leather",
                    Block::new,
                    p -> p.strength(3.0f).lightLevel(s -> 10).sound(SoundType.METAL).requiresCorrectToolForDrops().noLootTable());
        }
    }
}
