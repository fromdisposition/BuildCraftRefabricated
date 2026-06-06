package buildcraft.lib.registry;

import buildcraft.api.transport.pluggable.IPluggableRegistry;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import java.util.HashMap;
import java.util.Map;

public class PluggableRegistry implements IPluggableRegistry {
   public static final PluggableRegistry INSTANCE = new PluggableRegistry();
   private final Map<Object, PluggableDefinition> definitions = new HashMap<>();

   @Override
   public void register(Object identifier, PluggableDefinition definition) {
      this.definitions.put(identifier, definition);
   }

   @Override
   public PluggableDefinition getDefinition(Object identifier) {
      return this.definitions.get(identifier);
   }
}
