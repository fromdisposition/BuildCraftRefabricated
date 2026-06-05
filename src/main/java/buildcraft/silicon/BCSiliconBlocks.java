package buildcraft.silicon;

import buildcraft.fabric.registry.DeferredRegister;
import buildcraft.fabric.registry.DeferredBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import buildcraft.silicon.block.BlockLaser;
import buildcraft.silicon.block.BlockLaserTable;
import buildcraft.silicon.container.ContainerAssemblyTable;
import buildcraft.silicon.container.ContainerAdvancedCraftingTable;
import buildcraft.silicon.container.ContainerIntegrationTable;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileAdvancedCraftingTable;
import buildcraft.silicon.tile.TileIntegrationTable;

public class BCSiliconBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BCSilicon.MODID);

    public static final DeferredBlock<BlockLaser> LASER = BLOCKS.registerBlock(
            "laser",
            BlockLaser::new, () -> BlockBehaviour.Properties.of().strength(5.0f, 10.0f).noOcclusion().sound(SoundType.METAL).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockLaserTable> ASSEMBLY_TABLE = BLOCKS.registerBlock(
            "assembly_table",
            props -> new BlockLaserTable(props,
                () -> BCSiliconBlockEntities.ASSEMBLY_TABLE,
                (id, inv, tile) -> new ContainerAssemblyTable(id, inv.player, (TileAssemblyTable) tile)), () -> BlockBehaviour.Properties.of().strength(5.0f, 10.0f).noOcclusion().sound(SoundType.METAL).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockLaserTable> ADVANCED_CRAFTING_TABLE = BLOCKS.registerBlock(
            "advanced_crafting_table",
            props -> new BlockLaserTable(props,
                () -> BCSiliconBlockEntities.ADVANCED_CRAFTING_TABLE,
                (id, inv, tile) -> new ContainerAdvancedCraftingTable(id, inv.player, (TileAdvancedCraftingTable) tile)), () -> BlockBehaviour.Properties.of().strength(5.0f, 10.0f).noOcclusion().sound(SoundType.METAL).requiresCorrectToolForDrops());

    public static final DeferredBlock<BlockLaserTable> INTEGRATION_TABLE = BLOCKS.registerBlock(
            "integration_table",
            props -> new BlockLaserTable(props,
                    () -> BCSiliconBlockEntities.INTEGRATION_TABLE,
                    (id, inv, tile) -> new ContainerIntegrationTable(id, inv.player, (TileIntegrationTable) tile)),
            () -> BlockBehaviour.Properties.of().strength(5.0f, 10.0f).noOcclusion().sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());

    public static void register() {
        BLOCKS.register();
    }
}

