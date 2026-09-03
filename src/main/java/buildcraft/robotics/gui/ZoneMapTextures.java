/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.gui;

import buildcraft.robotics.zone.ZonePlannerChunkKeys;
import buildcraft.robotics.zone.ZonePlannerMapColours;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

/**
 * The zone planner map as textures: one 256x256 texture per region of 16x16 chunks, a pixel per column, repainted
 * only for chunks whose data (or whose north/west neighbour, which drives the relief shading) changed.
 */
final class ZoneMapTextures implements AutoCloseable {
   static final int REGION_CHUNKS = 16;
   static final int REGION_BLOCKS = REGION_CHUNKS * 16;
   private static final int MAX_REGIONS = 48;
   private static final int EVICT_MARGIN = 2;
   private static int nextInstance;
   private final int instance = nextInstance++;
   private final Long2ObjectOpenHashMap<Region> regions = new Long2ObjectOpenHashMap<>();

   Identifier textureOf(int rx, int rz) {
      Region region = this.regions.get(ZonePlannerChunkKeys.chunkKey(rx, rz));
      return region == null ? null : region.id;
   }

   void update(ZonePlannerMapColours cache, int cx0, int cz0, int cx1, int cz1) {
      if (this.regions.size() > MAX_REGIONS) {
         int rx0 = (cx0 >> 4) - EVICT_MARGIN;
         int rx1 = (cx1 >> 4) + EVICT_MARGIN;
         int rz0 = (cz0 >> 4) - EVICT_MARGIN;
         int rz1 = (cz1 >> 4) + EVICT_MARGIN;
         this.regions.long2ObjectEntrySet().removeIf(entry -> {
            int rx = ChunkPos.getX(entry.getLongKey());
            int rz = ChunkPos.getZ(entry.getLongKey());
            if (rx < rx0 || rx > rx1 || rz < rz0 || rz > rz1) {
               Minecraft.getInstance().getTextureManager().release(entry.getValue().id);
               return true;
            }

            return false;
         });
      }

      for (int cx = cx0; cx <= cx1; cx++) {
         for (int cz = cz0; cz <= cz1; cz++) {
            long key = ZonePlannerChunkKeys.chunkKey(cx, cz);
            int version = cache.versionOf(key);
            if (version == 0) {
               continue;
            }

            int north = cache.versionOf(ZonePlannerChunkKeys.chunkKey(cx, cz - 1));
            int west = cache.versionOf(ZonePlannerChunkKeys.chunkKey(cx - 1, cz));
            Region region = this.regionFor(cx >> 4, cz >> 4);
            int slot = (cz & 15) * REGION_CHUNKS + (cx & 15);
            if (region.versions[slot] != version || region.northVersions[slot] != north || region.westVersions[slot] != west) {
               region.versions[slot] = version;
               region.northVersions[slot] = north;
               region.westVersions[slot] = west;
               region.paint(cache, cx, cz);
            }
         }
      }

      for (Region region : this.regions.values()) {
         if (region.dirty) {
            region.dirty = false;
            region.texture.upload();
         }
      }
   }

   private Region regionFor(int rx, int rz) {
      long key = ZonePlannerChunkKeys.chunkKey(rx, rz);
      Region region = this.regions.get(key);
      if (region == null) {
         region = new Region(Identifier.fromNamespaceAndPath("buildcraftrobotics", "zone_map/" + this.instance + "/" + rx + "_" + rz));
         this.regions.put(key, region);
      }

      return region;
   }

   @Override
   public void close() {
      for (Region region : this.regions.values()) {
         Minecraft.getInstance().getTextureManager().release(region.id);
      }

      this.regions.clear();
   }

   private static final class Region {
      final Identifier id;
      final NativeImage image;
      final DynamicTexture texture;
      final int[] versions = new int[REGION_CHUNKS * REGION_CHUNKS];
      final int[] northVersions = new int[REGION_CHUNKS * REGION_CHUNKS];
      final int[] westVersions = new int[REGION_CHUNKS * REGION_CHUNKS];
      boolean dirty;

      Region(Identifier id) {
         this.id = id;
         this.image = new NativeImage(REGION_BLOCKS, REGION_BLOCKS, true);
         //? if >= 1.21.10 {
         this.texture = new DynamicTexture(id::toString, this.image);
         //?} else {
         /*this.texture = new DynamicTexture(this.image);
         *///?}
         Minecraft.getInstance().getTextureManager().register(id, this.texture);
      }

      void paint(ZonePlannerMapColours cache, int cx, int cz) {
         int[] cols = cache.coloursOf(ZonePlannerChunkKeys.chunkKey(cx, cz));
         int[] hts = cache.heightsOf(ZonePlannerChunkKeys.chunkKey(cx, cz));
         if (cols == null || hts == null) {
            return;
         }

         int[] htsN = cache.heightsOf(ZonePlannerChunkKeys.chunkKey(cx, cz - 1));
         int[] htsW = cache.heightsOf(ZonePlannerChunkKeys.chunkKey(cx - 1, cz));
         int px0 = (cx & 15) * 16;
         int pz0 = (cz & 15) * 16;

         for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
               int i = lz * 16 + lx;
               int colour = cols[i];
               int argb = 0;
               if (colour != 0) {
                  int h = hts[i];
                  int hN = lz > 0 ? hts[i - 16] : (htsN != null ? htsN[240 + lx] : ZonePlannerMapColours.NO_HEIGHT);
                  int hW = lx > 0 ? hts[i - 1] : (htsW != null ? htsW[lz * 16 + 15] : ZonePlannerMapColours.NO_HEIGHT);
                  argb = shade(colour, h, hN, hW);
               }

               //? if >= 1.21.10 {
               this.image.setPixel(px0 + lx, pz0 + lz, argb);
               //?} else {
               /*this.image.setPixelRGBA(px0 + lx, pz0 + lz, argb & 0xFF00FF00 | (argb & 0xFF) << 16 | argb >> 16 & 0xFF);
               *///?}
            }
         }

         this.dirty = true;
      }

      /**
       * Cartographic relief lit from the north-west: the colour (already tinted by altitude on the server) gets
       * brighter the more the column rises above its north and west neighbours and darker the more it drops, in the
       * same range as vanilla map shading, and a contour line is drawn wherever the height crosses a 16-block level
       * between neighbours.
       */
      private static int shade(int colour, int h, int hN, int hW) {
         int slope = 0;
         if (hN != ZonePlannerMapColours.NO_HEIGHT) {
            slope += h - hN;
         }

         if (hW != ZonePlannerMapColours.NO_HEIGHT) {
            slope += h - hW;
         }

         float f = 0.86F + 0.025F * Mth.clamp(slope, -6, 6);
         if (hN != ZonePlannerMapColours.NO_HEIGHT && h >> 4 != hN >> 4 || hW != ZonePlannerMapColours.NO_HEIGHT && h >> 4 != hW >> 4) {
            f *= 0.78F;
         }

         int r = Math.min(255, (int)((colour >> 16 & 0xFF) * f));
         int g = Math.min(255, (int)((colour >> 8 & 0xFF) * f));
         int b = Math.min(255, (int)((colour & 0xFF) * f));
         return 0xFF000000 | r << 16 | g << 8 | b;
      }
   }
}
