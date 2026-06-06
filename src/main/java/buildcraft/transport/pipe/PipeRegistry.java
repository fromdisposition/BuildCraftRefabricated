package buildcraft.transport.pipe;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.IPipeRegistry;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.fabric.BCRegistries;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.item.ItemPipeHolder;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public enum PipeRegistry implements IPipeRegistry {
   INSTANCE;

   private final Map<String, PipeDefinition> definitions = new HashMap<>();
   private final Map<PipeDefinition, IItemPipe> pipeItems = new IdentityHashMap<>();

   @Override
   public void registerPipe(PipeDefinition definition) {
      this.definitions.put(definition.identifier, definition);
   }

   @Override
   public void setItemForPipe(PipeDefinition definition, @Nullable IItemPipe item) {
      if (definition == null) {
         throw new NullPointerException("definition");
      }

      if (item == null) {
         this.pipeItems.remove(definition);
      } else {
         this.pipeItems.put(definition, item);
      }
   }

   @Override
   public IItemPipe createItemForPipe(PipeDefinition definition) {
      Identifier id = parseIdentifier(definition.identifier);
      ItemPipeHolder item = BCRegistries.registerItem(
         id.getNamespace(), "pipe_" + id.getPath(), props -> new ItemPipeHolder(BCTransportBlocks.PIPE_HOLDER, () -> definition, props)
      );
      if (this.definitions.containsValue(definition)) {
         this.setItemForPipe(definition, item);
      }

      return item;
   }

   @Override
   public IItemPipe createUnnamedItemForPipe(PipeDefinition definition, Consumer<Item> postCreate) {
      ItemPipeHolder item = new ItemPipeHolder(BCTransportBlocks.PIPE_HOLDER, () -> definition, new Properties());
      postCreate.accept(item);
      if (this.definitions.containsValue(definition)) {
         this.setItemForPipe(definition, item);
      }

      return item;
   }

   private static Identifier parseIdentifier(String identifier) {
      int colon = identifier.indexOf(58);
      return colon >= 0
         ? Identifier.fromNamespaceAndPath(identifier.substring(0, colon), identifier.substring(colon + 1))
         : Identifier.fromNamespaceAndPath("buildcrafttransport", identifier);
   }

   @Override
   public IItemPipe getItemForPipe(PipeDefinition definition) {
      return this.pipeItems.get(definition);
   }

   @Nullable
   @Override
   public PipeDefinition getDefinition(String identifier) {
      return this.definitions.get(identifier);
   }

   @Nonnull
   public PipeDefinition loadDefinition(String identifier) throws InvalidInputDataException {
      PipeDefinition def = this.getDefinition(identifier);
      if (def == null) {
         throw new InvalidInputDataException("Unknown pipe definition " + identifier);
      } else {
         return def;
      }
   }

   @Override
   public Iterable<PipeDefinition> getAllRegisteredPipes() {
      return ImmutableList.copyOf(this.definitions.values());
   }
}
