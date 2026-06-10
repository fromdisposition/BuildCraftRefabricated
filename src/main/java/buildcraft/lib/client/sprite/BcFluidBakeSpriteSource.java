/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.sprite;

import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.lib.client.fluid.BcFluidTintUtil;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import java.io.IOException;
import java.util.Optional;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSource.DiscardableLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource.Output;
import net.minecraft.client.renderer.texture.atlas.sources.LazyLoadedImage;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public record BcFluidBakeSpriteSource() implements SpriteSource {
   public static final Identifier ID = Identifier.fromNamespaceAndPath("buildcraftenergy", "fluid_bake");
   public static final MapCodec<BcFluidBakeSpriteSource> MAP_CODEC = MapCodec.unit(new BcFluidBakeSpriteSource());
   private static final Logger LOGGER = LogUtils.getLogger();

   public void run(ResourceManager rm, Output output) {
      for (BCEnergyFluidsFabric.FluidEntry entry : BCEnergyFluidsFabric.ALL) {
         emit(rm, output, entry, false);
         emit(rm, output, entry, true);
      }
   }

   private static void emit(ResourceManager rm, Output output, BCEnergyFluidsFabric.FluidEntry entry, boolean flow) {
      Identifier heatId = flow ? BcFluidTintUtil.heatFlowSpriteId(entry.heat()) : BcFluidTintUtil.heatStillSpriteId(entry.heat());
      Identifier outId = flow ? BcFluidTintUtil.bakedFlowSpriteId(entry.name()) : BcFluidTintUtil.bakedStillSpriteId(entry.name());
      Identifier texId = TEXTURE_ID_CONVERTER.idToFile(heatId);
      Optional<Resource> res = rm.getResource(texId);
      if (res.isEmpty()) {
         LOGGER.warn("BcFluidBakeSpriteSource: missing template {} for {}", texId, outId);
      } else {
         output.add(outId, new BcFluidBakeSpriteSource.Loader(new LazyLoadedImage(texId, res.get(), 1), res.get(), outId, entry.texLight(), entry.texDark()));
      }
   }

   public MapCodec<? extends SpriteSource> codec() {
      return MAP_CODEC;
   }

   private record Loader(LazyLoadedImage base, Resource sourceResource, Identifier outputId, int texLight, int texDark) implements DiscardableLoader {
      public @Nullable SpriteContents get(SpriteResourceLoader loader) {
         NativeImage out = null;

         try {
            NativeImage baseImg = this.base.get();
            int frameW = baseImg.getWidth();
            int totalH = baseImg.getHeight();
            out = new NativeImage(frameW, totalH, false);
            BcFluidTintUtil.bakeHeatImage(baseImg, out, this.texLight, this.texDark);
            SpriteContents result = BcHeatWhiteSpriteSource.createWithSourceMetadata(this.outputId, out, this.sourceResource);
            out = null;
            return result;
         } catch (IOException e) {
            BcFluidBakeSpriteSource.LOGGER.error("BcFluidBakeSpriteSource: failed to bake {}", this.outputId, e);
            return null;
         } finally {
            if (out != null) {
               out.close();
            }

            this.base.release();
         }
      }

      public void discard() {
         this.base.release();
      }
   }
}
