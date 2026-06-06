package buildcraft.api.transport.pluggable;

public interface IPluggableRegistry {
   default void register(PluggableDefinition definition) {
      this.register(definition.identifier, definition);
   }

   void register(Object var1, PluggableDefinition var2);

   PluggableDefinition getDefinition(Object var1);
}
