package buildcraft.fabric;

import java.io.IOException;
import java.util.Optional;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.LazyLoadedImage;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import buildcraft.fabric.BCEnergyFluidsFabric.FluidEntry;
import buildcraft.lib.client.fluid.BcFluidTintUtil;
import buildcraft.lib.client.sprite.BcHeatWhiteSpriteSource;
import buildcraft.lib.client.sprite.DyeReplaceSpriteSource;

public final class BCSpriteSourcesFabric {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean registered;

    private BCSpriteSourcesFabric() {}

    public static void register() {
        try {
            var field = net.minecraft.client.renderer.texture.atlas.SpriteSources.class.getDeclaredField("ID_MAPPER");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends SpriteSource>> mapper =
                    (ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends SpriteSource>>) field.get(null);
            registerInto(mapper);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to register BuildCraft sprite source types", e);
        }
    }

    public static void registerInto(ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends SpriteSource>> mapper) {
        if (registered) {
            return;
        }
        try {
            mapper.put(DyeReplaceSpriteSource.ID, DyeReplaceSpriteSource.MAP_CODEC);
            mapper.put(BcHeatWhiteSpriteSource.ID, BcHeatWhiteSpriteSource.MAP_CODEC);
            mapper.put(BcFluidBakeSpriteSource.ID, BcFluidBakeSpriteSource.MAP_CODEC);
            registered = true;
        } catch (RuntimeException e) {
            LOGGER.error("Failed to register BuildCraft sprite source types", e);
        }
    }
}

/** BC 8.0 {@code AtlasSpriteFluid}: bake all energy fluid still/flow sprites at atlas stitch. */
final class BcFluidBakeSpriteSource implements SpriteSource {
    static final Identifier ID = Identifier.fromNamespaceAndPath("buildcraftenergy", "fluid_bake");
    static final MapCodec<BcFluidBakeSpriteSource> MAP_CODEC = MapCodec.unit(new BcFluidBakeSpriteSource());

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void run(ResourceManager rm, Output output) {
        for (FluidEntry entry : BCEnergyFluidsFabric.ALL) {
            emit(rm, output, entry, false);
            emit(rm, output, entry, true);
        }
    }

    private static void emit(ResourceManager rm, Output output, FluidEntry entry, boolean flow) {
        Identifier heatId = flow
                ? BcFluidTintUtil.heatFlowSpriteId(entry.heat())
                : BcFluidTintUtil.heatStillSpriteId(entry.heat());
        Identifier outId = flow
                ? BcFluidTintUtil.bakedFlowSpriteId(entry.name())
                : BcFluidTintUtil.bakedStillSpriteId(entry.name());
        Identifier texId = TEXTURE_ID_CONVERTER.idToFile(heatId);
        Optional<Resource> res = rm.getResource(texId);
        if (res.isEmpty()) {
            LOGGER.warn("BcFluidBakeSpriteSource: missing template {} for {}", texId, outId);
            return;
        }
        output.add(outId, new Loader(new LazyLoadedImage(texId, res.get(), 1), res.get(), outId, entry.texLight(), entry.texDark()));
    }

    @Override
    public MapCodec<? extends SpriteSource> codec() {
        return MAP_CODEC;
    }

    private record Loader(LazyLoadedImage base, Resource sourceResource, Identifier outputId, int texLight, int texDark)
            implements DiscardableLoader {

        @Override
        public @Nullable SpriteContents get(SpriteResourceLoader loader) {
            NativeImage out = null;
            try {
                NativeImage baseImg = base.get();
                int frameW = baseImg.getWidth();
                int totalH = baseImg.getHeight();
                out = new NativeImage(frameW, totalH, false);
                BcFluidTintUtil.bakeHeatImage(baseImg, out, texLight, texDark);
                SpriteContents result = BcHeatWhiteSpriteSource.createWithSourceMetadata(outputId, out, sourceResource);
                out = null;
                return result;
            } catch (IOException e) {
                LOGGER.error("BcFluidBakeSpriteSource: failed to bake {}", outputId, e);
                return null;
            } finally {
                if (out != null) {
                    out.close();
                }
                base.release();
            }
        }

        @Override
        public void discard() {
            base.release();
        }
    }
}
