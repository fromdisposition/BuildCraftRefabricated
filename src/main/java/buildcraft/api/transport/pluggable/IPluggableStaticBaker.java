package buildcraft.api.transport.pluggable;

import java.util.List;

import net.minecraft.client.resources.model.geometry.BakedQuad;

public interface IPluggableStaticBaker<K extends PluggableModelKey> {
    List<BakedQuad> bake(K key);
}
