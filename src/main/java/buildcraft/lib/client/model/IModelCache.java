package buildcraft.lib.client.model;

import java.util.List;
import net.minecraft.client.resources.model.geometry.BakedQuad;

public interface IModelCache<K> {
   List<BakedQuad> bake(K var1);

   void clear();
}
