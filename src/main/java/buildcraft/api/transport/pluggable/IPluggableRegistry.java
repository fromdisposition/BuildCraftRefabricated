package buildcraft.api.transport.pluggable;

public interface IPluggableRegistry {
    default void register(PluggableDefinition definition) {
        register(definition.identifier, definition);
    }

    void register(Object identifier, PluggableDefinition definition);

    PluggableDefinition getDefinition(Object identifier);
}
