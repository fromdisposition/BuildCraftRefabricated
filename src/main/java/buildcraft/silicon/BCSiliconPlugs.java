package buildcraft.silicon;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.silicon.plug.FilterEventHandler;
import buildcraft.silicon.plug.PluggableFacade;
import buildcraft.silicon.plug.PluggableGate;
import buildcraft.silicon.plug.PluggableLens;
import buildcraft.silicon.plug.PluggableLightSensor;
import buildcraft.silicon.plug.PluggablePulsar;
import buildcraft.silicon.plug.PluggableTimer;
import buildcraft.transport.pipe.PipeEventBus;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCSiliconPlugs {
   private static final Logger LOGGER = LoggerFactory.getLogger("BuildCraft");
   public static PluggableDefinition facade;
   public static PluggableDefinition gate;
   public static PluggableDefinition pulsar;
   public static PluggableDefinition lens;
   public static PluggableDefinition lightSensor;
   public static PluggableDefinition timer;
   private static final List<PluggableDefinition> PENDING = new ArrayList<>();

   public static void preInit() {
      facade = create("facade", PluggableFacade::new, PluggableFacade::new);
      gate = create("gate", PluggableGate::new, PluggableGate::new);
      pulsar = create("pulsar", PluggablePulsar::new, PluggablePulsar::new);
      lens = create("lens", PluggableLens::new, PluggableLens::new);
      lightSensor = createSimple("light_sensor", PluggableLightSensor::new);
      timer = createSimple("timer", PluggableTimer::new);
      PipeEventBus.registerGlobalHandler(FilterEventHandler.class);
   }

   public static void registerAll() {
      if (PipeApi.pluggableRegistry == null) {
         LOGGER.error("[silicon.plugs] PipeApi.pluggableRegistry is null at registerAll! Pluggables (facades, gates, etc.) will NOT be saved or loaded.");
      } else {
         for (PluggableDefinition def : PENDING) {
            PipeApi.pluggableRegistry.register(def);
         }

         LOGGER.info("[silicon.plugs] Registered {} pluggable definitions", PENDING.size());
         PENDING.clear();
      }
   }

   private static PluggableDefinition create(String name, PluggableDefinition.IPluggableNbtReader reader, PluggableDefinition.IPluggableNetLoader loader) {
      PluggableDefinition def = new PluggableDefinition(idFor(name), reader, loader);
      PENDING.add(def);
      return def;
   }

   private static PluggableDefinition createSimple(String name, PluggableDefinition.IPluggableCreator creator) {
      PluggableDefinition def = new PluggableDefinition(idFor(name), creator);
      PENDING.add(def);
      return def;
   }

   private static Identifier idFor(String name) {
      return Identifier.fromNamespaceAndPath("buildcraftsilicon", name);
   }
}
