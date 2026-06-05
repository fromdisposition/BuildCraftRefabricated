/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.client.sprite;

import java.io.IOException;
import java.util.Optional;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.LazyLoadedImage;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.DyeColor;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import buildcraft.lib.misc.ColourUtil;

public record DyeReplaceSpriteSource(Identifier source, Identifier mask) implements SpriteSource {

    public static final Identifier ID =
        Identifier.fromNamespaceAndPath("buildcraftlib", "dye_replace");

    public static final MapCodec<DyeReplaceSpriteSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(
            Identifier.CODEC.fieldOf("source").forGetter(DyeReplaceSpriteSource::source),
            Identifier.CODEC.fieldOf("mask").forGetter(DyeReplaceSpriteSource::mask)
        ).apply(i, DyeReplaceSpriteSource::new)
    );

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int WATERPROOFING_R = 0x24;
    private static final int WATERPROOFING_G = 0x45;
    private static final int WATERPROOFING_B = 0x1B;
    private static final int WATERPROOFING_TOLERANCE = 3;

    @Override
    public void run(ResourceManager rm, Output output) {
        Identifier sourceTexId = TEXTURE_ID_CONVERTER.idToFile(source);
        Identifier maskTexId   = TEXTURE_ID_CONVERTER.idToFile(mask);

        Optional<Resource> sourceRes = rm.getResource(sourceTexId);
        if (sourceRes.isEmpty()) {
            LOGGER.warn("DyeReplaceSpriteSource: source texture {} not found", sourceTexId);
            return;
        }
        Optional<Resource> maskRes = rm.getResource(maskTexId);
        if (maskRes.isEmpty()) {
            LOGGER.warn("DyeReplaceSpriteSource: mask texture {} not found", maskTexId);
            return;
        }

        DyeColor[] dyes = DyeColor.values();

        LazyLoadedImage baseImage = new LazyLoadedImage(sourceTexId, sourceRes.get(), dyes.length);
        LazyLoadedImage maskImage = new LazyLoadedImage(maskTexId,   maskRes.get(),   dyes.length);

        for (DyeColor dye : dyes) {
            Identifier outputId = source.withSuffix("_dyed_" + dye.getName());
            int dyeRgb = ColourUtil.getLightHex(dye);
            output.add(outputId, new Loader(baseImage, maskImage, dyeRgb, outputId));
        }
    }

    @Override
    public MapCodec<? extends SpriteSource> codec() {
        return MAP_CODEC;
    }

    private record Loader(LazyLoadedImage base, LazyLoadedImage mask, int dyeRgb, Identifier outputId)

            implements DiscardableLoader {

        @Override

        public @Nullable SpriteContents get(SpriteResourceLoader loader) {

            NativeImage out = null;
            try {
                NativeImage baseImg = base.get();
                NativeImage maskImg = mask.get();

                int w = baseImg.getWidth();
                int h = baseImg.getHeight();
                if (maskImg.getWidth() != w || maskImg.getHeight() != h) {
                    LOGGER.warn(
                        "DyeReplaceSpriteSource: mask {}x{} differs from source {}x{} for {}",
                        maskImg.getWidth(), maskImg.getHeight(), w, h, outputId);
                    return null;
                }

                int dyeR = (dyeRgb >> 16) & 0xFF;
                int dyeG = (dyeRgb >> 8)  & 0xFF;
                int dyeB =  dyeRgb        & 0xFF;

                out = new NativeImage(w, h, false);
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int srcPixel = baseImg.getPixel(x, y);
                        int srcA = (srcPixel >>> 24) & 0xFF;
                        if (srcA == 0) {
                            out.setPixel(x, y, 0);
                            continue;
                        }
                        int srcR = (srcPixel >> 16) & 0xFF;
                        int srcG = (srcPixel >> 8)  & 0xFF;
                        int srcB =  srcPixel        & 0xFF;

                        int maskA = (maskImg.getPixel(x, y) >>> 24) & 0xFF;
                        if (maskA > 0 && isWaterproofing(srcR, srcG, srcB)) {

                            out.setPixel(x, y,
                                (srcA << 24) | (dyeR << 16) | (dyeG << 8) | dyeB);
                        } else {
                            out.setPixel(x, y, srcPixel);
                        }
                    }
                }

                SpriteContents result = new SpriteContents(outputId, new FrameSize(w, h), out);
                out = null;
                return result;
            } catch (IOException e) {
                LOGGER.error("DyeReplaceSpriteSource: failed to generate {}", outputId, e);
                return null;
            } finally {
                if (out != null) out.close();
                base.release();
                mask.release();
            }
        }

        @Override
        public void discard() {
            base.release();
            mask.release();
        }

        private static boolean isWaterproofing(int r, int g, int b) {
            return Math.abs(r - WATERPROOFING_R) <= WATERPROOFING_TOLERANCE
                && Math.abs(g - WATERPROOFING_G) <= WATERPROOFING_TOLERANCE
                && Math.abs(b - WATERPROOFING_B) <= WATERPROOFING_TOLERANCE;
        }
    }
}
