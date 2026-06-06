package buildcraft.api.transport.pipe;

import buildcraft.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableModelKey;

public enum PipeApiClient {
   INSTANCE;

   public static PipeApiClient.IClientRegistry registry;

   public interface IClientRegistry {
      <F extends PipeFlow> void registerRenderer(Class<? extends F> var1, IPipeFlowRenderer<F> var2);

      <B extends PipeBehaviour> void registerRenderer(Class<? extends B> var1, IPipeBehaviourRenderer<B> var2);

      <P extends PipePluggable> void registerRenderer(Class<? extends P> var1, IPlugDynamicRenderer<P> var2);

      <P extends PluggableModelKey> void registerBaker(Class<? extends P> var1, IPluggableStaticBaker<P> var2);
   }
}
