package buildcraft.api.transport.pipe;

import java.util.List;

import net.minecraft.client.resources.model.geometry.BakedQuad;

public interface IPipeFlowBaker<F extends PipeFlow> {
    List<BakedQuad> bake(F flow);
}
