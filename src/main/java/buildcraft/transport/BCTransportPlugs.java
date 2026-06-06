package buildcraft.transport;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.lib.registry.PluggableRegistry;
import buildcraft.transport.plug.PluggableBlocker;
import buildcraft.transport.plug.PluggablePowerAdaptor;
import net.minecraft.resources.Identifier;

public class BCTransportPlugs {
   public static PluggableDefinition blocker;
   public static PluggableDefinition powerAdaptor;

   public static void preInit() {
      PipeApi.pluggableRegistry = PluggableRegistry.INSTANCE;
      blocker = register("blocker", PluggableBlocker::new);
      powerAdaptor = register("power_adaptor", PluggablePowerAdaptor::new);
   }

   private static PluggableDefinition register(String name, PluggableDefinition.IPluggableCreator creator) {
      PluggableDefinition def = new PluggableDefinition(idFor(name), creator);
      PipeApi.pluggableRegistry.register(def);
      return def;
   }

   private static Identifier idFor(String name) {
      return Identifier.fromNamespaceAndPath("buildcrafttransport", name);
   }
}
