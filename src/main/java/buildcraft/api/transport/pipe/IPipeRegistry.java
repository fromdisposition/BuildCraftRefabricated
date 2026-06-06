package buildcraft.api.transport.pipe;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.world.item.Item;

public interface IPipeRegistry {
   PipeDefinition getDefinition(String var1);

   void registerPipe(PipeDefinition var1);

   void setItemForPipe(PipeDefinition var1, @Nullable IItemPipe var2);

   IItemPipe getItemForPipe(PipeDefinition var1);

   IItemPipe createItemForPipe(PipeDefinition var1);

   IItemPipe createUnnamedItemForPipe(PipeDefinition var1, Consumer<Item> var2);

   Iterable<PipeDefinition> getAllRegisteredPipes();
}
