package buildcraft.lib.client.guide;

import buildcraft.api.registry.IReloadableRegistry;
import buildcraft.api.registry.IScriptableRegistry;
import buildcraft.lib.client.guide.entry.PageEntry;
import buildcraft.lib.client.guide.entry.PageEntryExternal;
import buildcraft.lib.client.guide.entry.PageEntryFluidStack;
import buildcraft.lib.client.guide.entry.PageEntryItemStack;
import buildcraft.lib.client.guide.entry.PageEntryStatement;
import buildcraft.lib.client.guide.entry.PageValueType;
import buildcraft.lib.script.ScriptableRegistry;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;

public class GuidePageRegistry extends ScriptableRegistry<PageEntry<?>> {
   public static final GuidePageRegistry INSTANCE = new GuidePageRegistry();
   public final Map<String, PageValueType<?>> types = new HashMap<>();

   private GuidePageRegistry() {
      super(IReloadableRegistry.PackType.RESOURCE_PACK, "buildcraft/guide");
      this.addType("item_stack", PageEntryItemStack.INSTANCE);
      this.addType("fluid_stack", PageEntryFluidStack.INSTANCE);
      this.addType("external", PageEntryExternal.INSTANCE);
      this.addType("statement", PageEntryStatement.INSTANCE);
   }

   public <T> void addType(String name, PageValueType<T> type) {
      this.types.put(name, type);
      this.addCustomType(name, (id, json, ctx) -> {
         IScriptableRegistry.OptionallyDisabled<PageEntry<T>> o1 = type.deserialize((Identifier)id, json, ctx);
         return o1.isPresent() ? new IScriptableRegistry.OptionallyDisabled<>(o1.get()) : new IScriptableRegistry.OptionallyDisabled<>(o1.getDisabledReason());
      });
   }
}
