package buildcraft.energy.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.profiler.Profiler;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.lib.client.model.MutableQuad;

import buildcraft.energy.BCEnergyModels;
import buildcraft.energy.tile.TileDynamoMJ;

public class RenderDynamoMJ extends FastTESR<TileDynamoMJ> {
    public static final RenderDynamoMJ INSTANCE = new RenderDynamoMJ();

    @Override
    public void renderTileEntityFast(
        TileDynamoMJ engine, double x, double y, double z, float partialTicks, int destroyStage, float partial,
        BufferBuilder vb
    ) {
        Profiler profiler = Minecraft.getMinecraft().mcProfiler;
        profiler.startSection("bc");
        profiler.startSection("engine");

        profiler.startSection("compute");
        vb.setTranslation(x, y, z);
        MutableQuad[] quads = BCEnergyModels.getMjDynamoQuads(engine, partialTicks);
        profiler.endStartSection("render");
        MutableQuad copy = new MutableQuad(0, null);
        int lightc = engine.getWorld().getCombinedLight(engine.getPos(), 0);
        int light_block = (lightc >> 4) & 15;
        int light_sky = (lightc >> 20) & 15;
        for (MutableQuad q : quads) {
            copy.copyFrom(q);
            copy.maxLighti(light_block, light_sky);
            copy.multShade();
            copy.render(vb);
        }
        vb.setTranslation(0, 0, 0);

        profiler.endSection();
        profiler.endSection();
        profiler.endSection();
    }

}
