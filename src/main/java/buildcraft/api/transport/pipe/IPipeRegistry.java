package buildcraft.api.transport.pipe;

import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.world.item.Item;

public interface IPipeRegistry {
    PipeDefinition getDefinition(String identifier);

    void registerPipe(PipeDefinition definition);

    void setItemForPipe(PipeDefinition definition, @Nullable IItemPipe item);

    IItemPipe getItemForPipe(PipeDefinition definition);

    IItemPipe createItemForPipe(PipeDefinition definition);

    IItemPipe createUnnamedItemForPipe(PipeDefinition definition, Consumer<Item> postCreate);

    Iterable<PipeDefinition> getAllRegisteredPipes();
}
