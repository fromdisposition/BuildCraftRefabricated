package buildcraft.lib.client.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.resources.model.geometry.BakedQuad;

public class ModelCacheMultipleSame<K, T> implements IModelCache<K> {
   private final IModelCache<K> mainCache = new ModelCache<>(this::load);
   private final ModelCacheMultipleSame.IModelKeyMultipleSameMapper<K, T> mapper;
   private final IModelCache<T> separateCache;

   public ModelCacheMultipleSame(ModelCacheMultipleSame.IModelKeyMultipleSameMapper<K, T> mapper, IModelCache<T> separateCache) {
      this.mapper = mapper;
      this.separateCache = separateCache;
   }

   private List<BakedQuad> load(K key) {
      List<BakedQuad> quads = new ArrayList<>();

      for (T to : this.mapper.map(key)) {
         quads.addAll(this.separateCache.bake(to));
      }

      return quads;
   }

   @Override
   public List<BakedQuad> bake(K key) {
      return ModelCache.cacheJoined ? this.mainCache.bake(key) : this.load(key);
   }

   @Override
   public void clear() {
      this.mainCache.clear();
      this.separateCache.clear();
   }

   public interface IModelKeyMultipleSameMapper<F, T> {
      Collection<T> map(F var1);
   }
}
