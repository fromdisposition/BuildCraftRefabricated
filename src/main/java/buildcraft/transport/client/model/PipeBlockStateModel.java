package buildcraft.transport.client.model;

import java.util.List;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;

public class PipeBlockStateModel implements BlockStateModel {
    private final BlockStateModel vanillaDelegate;

    public PipeBlockStateModel(BlockStateModel vanillaDelegate) {
        this.vanillaDelegate = vanillaDelegate;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        vanillaDelegate.collectParts(random, parts);
    }

    @Override
    public Material.Baked particleMaterial() {
        return vanillaDelegate.particleMaterial();
    }

    @Override
    public int materialFlags() {
        return vanillaDelegate.materialFlags();
    }
}
