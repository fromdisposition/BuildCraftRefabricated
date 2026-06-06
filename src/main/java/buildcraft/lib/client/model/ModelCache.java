package buildcraft.lib.client.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.resources.model.geometry.BakedQuad;

public class ModelCache<K> implements IModelCache<K> {
   public static boolean cacheJoined = true;
   private final LoadingCache<K, List<BakedQuad>> modelCache;

   public ModelCache(ModelCache.IModelGenerator<K> generator) {
      this.modelCache = CacheBuilder.newBuilder().expireAfterAccess(1L, TimeUnit.MINUTES).build(CacheLoader.from(generator::generate));
   }

   @Override
   public List<BakedQuad> bake(K key) {
      return (List<BakedQuad>)this.modelCache.getUnchecked(key);
   }

   @Override
   public void clear() {
      this.modelCache.invalidateAll();
   }

   public interface IModelGenerator<T> {
      List<BakedQuad> generate(T var1);
   }
}
