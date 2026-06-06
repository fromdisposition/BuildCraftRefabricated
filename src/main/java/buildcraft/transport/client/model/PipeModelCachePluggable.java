package buildcraft.transport.client.model;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.lib.client.model.IModelCache;
import buildcraft.lib.client.model.ModelCache;
import buildcraft.lib.client.model.ModelCacheMultipleSame;
import buildcraft.transport.client.PipeRegistryClient;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.List;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;

public class PipeModelCachePluggable {
   public static final ModelCache<PluggableModelKey> cacheCutoutSingle = new ModelCache<>(PipeModelCachePluggable::generate);
   public static final ModelCache<PluggableModelKey> cacheTranslucentSingle = new ModelCache<>(PipeModelCachePluggable::generate);
   public static final IModelCache<PipeModelCachePluggable.PluggableKey> cacheCutoutAll = new ModelCacheMultipleSame<>(
      PipeModelCachePluggable.PluggableKey::getKeys, PipeModelCachePluggable.cacheCutoutSingle
   );
   public static final IModelCache<PipeModelCachePluggable.PluggableKey> cacheTranslucentAll = new ModelCacheMultipleSame<>(
      PipeModelCachePluggable.PluggableKey::getKeys, PipeModelCachePluggable.cacheTranslucentSingle
   );

   private static <K extends PluggableModelKey> List<BakedQuad> generate(K key) {
      if (key == null) {
         return ImmutableList.of();
      }

      IPluggableStaticBaker<K> baker = PipeRegistryClient.getPlugBaker(key);
      return (List<BakedQuad>)(baker == null ? ImmutableList.of() : baker.bake(key));
   }

   public static class PluggableKey {
      private final ImmutableSet<PluggableModelKey> pluggables;
      private final int hash;

      public PluggableKey(boolean isCutout, IPipeHolder holder) {
         Builder<PluggableModelKey> builder = ImmutableSet.builder();

         for (Direction side : Direction.values()) {
            PipePluggable pluggable = holder.getPluggable(side);
            if (pluggable != null) {
               PluggableModelKey key = pluggable.getModelRenderKey(isCutout ? "cutout" : "translucent");
               if (key != null) {
                  builder.add(key);
               }
            }
         }

         this.pluggables = builder.build();
         this.hash = this.pluggables.hashCode();
      }

      public ImmutableSet<PluggableModelKey> getKeys() {
         return this.pluggables;
      }

      @Override
      public int hashCode() {
         return this.hash;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         }

         if (obj == null) {
            return false;
         }

         if (this.getClass() != obj.getClass()) {
            return false;
         }

         PipeModelCachePluggable.PluggableKey other = (PipeModelCachePluggable.PluggableKey)obj;
         return this.pluggables.equals(other.pluggables);
      }
   }
}
