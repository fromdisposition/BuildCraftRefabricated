package buildcraft.lib.client.guide;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

import buildcraft.lib.client.guide.entry.PageEntry;
import buildcraft.lib.client.guide.entry.PageEntryExternal;
import buildcraft.lib.client.guide.entry.PageEntryFluidStack;
import buildcraft.lib.client.guide.entry.PageEntryItemStack;
import buildcraft.lib.client.guide.entry.PageEntryStatement;
import buildcraft.lib.client.guide.entry.PageValueType;
import buildcraft.lib.script.ScriptableRegistry;

public class GuidePageRegistry extends ScriptableRegistry<PageEntry<?>> {

    public static final GuidePageRegistry INSTANCE = new GuidePageRegistry();

    public final Map<String, PageValueType<?>> types = new HashMap<>();

    private GuidePageRegistry() {
        super(PackType.RESOURCE_PACK, "buildcraft/guide");
        addType("item_stack", PageEntryItemStack.INSTANCE);
        addType("fluid_stack", PageEntryFluidStack.INSTANCE);
        addType("external", PageEntryExternal.INSTANCE);
        addType("statement", PageEntryStatement.INSTANCE);
    }

    public <T> void addType(String name, PageValueType<T> type) {
        types.put(name, type);
        addCustomType(name, (id, json, ctx) -> {
            OptionallyDisabled<PageEntry<T>> o1 = type.deserialize((Identifier) id, json, ctx);

            if (o1.isPresent()) {
                return new OptionallyDisabled<>(o1.get());
            } else {
                return new OptionallyDisabled<>(o1.getDisabledReason());
            }
        });
    }
}
