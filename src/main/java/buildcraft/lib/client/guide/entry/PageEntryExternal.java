package buildcraft.lib.client.guide.entry;

import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.util.profiling.ProfilerFiller;

import buildcraft.api.registry.IScriptableRegistry.OptionallyDisabled;

import buildcraft.lib.gui.ISimpleDrawable;

public class PageEntryExternal extends PageValueType<String> {

    public static final PageEntryExternal INSTANCE = new PageEntryExternal();

    @Override
    public OptionallyDisabled<PageEntry<String>> deserialize(Identifier name, JsonObject json,
        JsonDeserializationContext ctx) {
        String value = PageValue.getTitle(json);
        return new OptionallyDisabled<>(new PageEntry<>(this, name, json, value));
    }

    @Override
    public Class<String> getEntryClass() {
        return String.class;
    }

    @Override
    public List<String> getTooltip(String value) {
        return Collections.singletonList(value);
    }

    @Override
    @Nullable
    public ISimpleDrawable createDrawable(String value) {
        return null;
    }

    @Override
    public String getTitle(String value) {
        return value;
    }

    @Override
    public void iterateAllDefault(IEntryLinkConsumer consumer, ProfilerFiller prof) {

    }
}
