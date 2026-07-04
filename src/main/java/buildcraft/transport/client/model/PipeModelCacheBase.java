/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import buildcraft.api.transport.pipe.EnumPipeColourType;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pipe.PipeFaceTex;
import buildcraft.lib.client.model.IModelCache;
import buildcraft.lib.client.model.ModelCache;
import buildcraft.transport.client.model.key.PipeModelKey;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.DyeColor;

public class PipeModelCacheBase {
   public static IPipeBaseModelGen generator = PipeBaseModelGenStandard.INSTANCE;
   static final IModelCache<PipeModelCacheBase.PipeBaseCutoutKey> cacheCutout = new ModelCache<>(PipeModelCacheBase::generateCutout);
   static final IModelCache<PipeModelCacheBase.PipeBaseTranslucentKey> cacheTranslucent = new ModelCache<>(PipeModelCacheBase::generateTranslucent);

   private static List<BakedQuad> generateCutout(PipeModelCacheBase.PipeBaseCutoutKey key) {
      return generator.generateCutout(key);
   }

   private static List<BakedQuad> generateTranslucent(PipeModelCacheBase.PipeBaseTranslucentKey key) {
      return generator.generateTranslucent(key);
   }

   public static final class PipeBaseCutoutKey {
      public final PipeDefinition definition;
      public final PipeFaceTex centerSprite;
      public final PipeFaceTex[] sideSprites;
      public final float[] connections;
      public final DyeColor colour;
      public final EnumPipeColourType colourType;
      /** Bitmask by face ordinal: sides connected to a TILE — those tube ends are open (see generator). */
      public final int tileConnections;
      private final int hashCode;

      public PipeBaseCutoutKey(PipeModelKey key) {
         this.definition = key.definition;
         this.centerSprite = key.center;
         this.sideSprites = key.sides;
         this.connections = key.connected;
         this.tileConnections = key.tileConnections;
         EnumPipeColourType defColourType = key.getColourType();
         if (key.colour != null && canBakeCutoutColour(defColourType)) {
            this.colour = key.colour;
            this.colourType = defColourType;
         } else {
            this.colour = null;
            this.colourType = null;
         }

         this.hashCode = Objects.hash(
            System.identityHashCode(this.definition),
            this.centerSprite,
            Arrays.hashCode(this.sideSprites),
            Arrays.hashCode(this.connections),
            this.colour,
            this.colourType,
            this.tileConnections
         );
      }

      private static boolean canBakeCutoutColour(EnumPipeColourType type) {
         return type == EnumPipeColourType.BORDER_OUTER || type == EnumPipeColourType.BORDER_INNER || type == EnumPipeColourType.TRANSLUCENT;
      }

      @Override
      public int hashCode() {
         return this.hashCode;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj == null) {
            return false;
         } else if (this.getClass() != obj.getClass()) {
            return false;
         } else {
            PipeModelCacheBase.PipeBaseCutoutKey other = (PipeModelCacheBase.PipeBaseCutoutKey)obj;
            if (this.definition != other.definition) {
               return false;
            } else if (this.centerSprite != other.centerSprite) {
               return false;
            } else if (this.colour != other.colour) {
               return false;
            } else if (this.colourType != other.colourType) {
               return false;
            } else if (this.tileConnections != other.tileConnections) {
               return false;
            } else {
               return !Arrays.equals(this.connections, other.connections) ? false : Arrays.equals(this.sideSprites, other.sideSprites);
            }
         }
      }

      @Override
      public String toString() {
         return "PipeBaseCutoutKey [center="
            + this.centerSprite
            + ", sides="
            + Arrays.toString(this.sideSprites)
            + ", connections="
            + Arrays.toString(this.connections)
            + "]";
      }
   }

   public static final class PipeBaseTranslucentKey {
      public final DyeColor colour;
      public final float[] connections;
      public final PipeModelCacheBase.PipeBaseCutoutKey cutoutKey;
      private final int hashCode;

      public PipeBaseTranslucentKey(PipeModelKey key) {
         if (key.getColourType() == EnumPipeColourType.TRANSLUCENT) {
            this.colour = key.colour;
            this.cutoutKey = new PipeModelCacheBase.PipeBaseCutoutKey(key);
         } else {
            this.colour = null;
            this.cutoutKey = null;
         }

         if (this.colour == null) {
            this.connections = null;
            this.hashCode = 0;
         } else {
            this.connections = key.connected;
            this.hashCode = Objects.hash(this.colour, Arrays.hashCode(this.connections), this.cutoutKey);
         }
      }

      public boolean shouldRender() {
         return this.colour != null;
      }

      @Override
      public int hashCode() {
         return this.hashCode;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj == null) {
            return false;
         } else if (!(obj instanceof PipeModelCacheBase.PipeBaseTranslucentKey other)) {
            return false;
         } else if (!this.shouldRender() && !other.shouldRender()) {
            return true;
         } else if (!Arrays.equals(this.connections, other.connections)) {
            return false;
         } else {
            return this.colour != other.colour ? false : Objects.equals(this.cutoutKey, other.cutoutKey);
         }
      }

      @Override
      public String toString() {
         return "PipeBaseTranslucentKey [colour=" + this.colour + ", connections=" + Arrays.toString(this.connections) + "]";
      }
   }
}
