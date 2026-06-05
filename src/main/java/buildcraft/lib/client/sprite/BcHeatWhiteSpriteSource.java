/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.client.sprite;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.LazyLoadedImage;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/** Gray heat fluid template → white RGB + original alpha for direct vertex recolor. */
public record BcHeatWhiteSpriteSource(Identifier source) implements SpriteSource {

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath("buildcraftenergy", "heat_white");

    public static final MapCodec<BcHeatWhiteSpriteSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    Identifier.CODEC.fieldOf("source").forGetter(BcHeatWhiteSpriteSource::source)
            ).apply(i, BcHeatWhiteSpriteSource::new)
    );

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void run(ResourceManager rm, Output output) {
        Identifier sourceTexId = TEXTURE_ID_CONVERTER.idToFile(source);
        Optional<Resource> sourceRes = rm.getResource(sourceTexId);
        if (sourceRes.isEmpty()) {
            LOGGER.warn("BcHeatWhiteSpriteSource: source texture {} not found", sourceTexId);
            return;
        }
        Identifier outputId = source.withSuffix("_white");
        LazyLoadedImage baseImage = new LazyLoadedImage(sourceTexId, sourceRes.get(), 1);
        output.add(outputId, new Loader(baseImage, sourceRes.get(), outputId));
    }

    @Override
    public MapCodec<? extends SpriteSource> codec() {
        return MAP_CODEC;
    }

    private record Loader(LazyLoadedImage base, Resource sourceResource, Identifier outputId) implements DiscardableLoader {

        @Override
        public @Nullable SpriteContents get(SpriteResourceLoader loader) {
            NativeImage out = null;
            try {
                NativeImage baseImg = base.get();
                int frameW = baseImg.getWidth();
                int totalH = baseImg.getHeight();
                out = new NativeImage(frameW, totalH, false);
                for (int y = 0; y < totalH; y++) {
                    for (int x = 0; x < frameW; x++) {
                        int srcPixel = baseImg.getPixel(x, y);
                        int a = (srcPixel >>> 24) & 0xFF;
                        if (a == 0) {
                            out.setPixel(x, y, 0);
                        } else {
                            out.setPixel(x, y, (a << 24) | 0x00FFFFFF);
                        }
                    }
                }
                SpriteContents result = createWithSourceMetadata(outputId, out, sourceResource);
                out = null;
                return result;
            } catch (IOException e) {
                LOGGER.error("BcHeatWhiteSpriteSource: failed to generate {}", outputId, e);
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

    /** SpriteContents with animation metadata copied from the heat template .mcmeta. */
    public static SpriteContents createWithSourceMetadata(
            Identifier outputId, NativeImage image, Resource sourceResource) {
        Optional<AnimationMetadataSection> animationInfo = Optional.empty();
        Optional<TextureMetadataSection> textureInfo = Optional.empty();
        List<MetadataSectionType.WithValue<?>> additionalMetadata = List.of();
        try {
            ResourceMetadata metadata = sourceResource.metadata();
            animationInfo = metadata.getSection(AnimationMetadataSection.TYPE);
            textureInfo = metadata.getSection(TextureMetadataSection.TYPE);
            additionalMetadata = metadata.getTypedSections(Set.of());
        } catch (Exception e) {
            LOGGER.warn("BcHeatWhiteSpriteSource: failed to read metadata for {}", outputId, e);
        }

        FrameSize frameSize = animationInfo
                .map(animation -> animation.calculateFrameSize(image.getWidth(), image.getHeight()))
                .orElseGet(() -> new FrameSize(image.getWidth(), image.getHeight()));

        return new SpriteContents(outputId, frameSize, image, animationInfo, additionalMetadata, textureInfo);
    }
}
