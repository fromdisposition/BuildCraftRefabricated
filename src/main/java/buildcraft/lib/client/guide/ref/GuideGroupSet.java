package buildcraft.lib.client.guide.ref;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import buildcraft.lib.client.guide.entry.PageValue;

public final class GuideGroupSet {

    public enum GroupDirection {
        SRC_TO_ENTRY("to."),
        ENTRY_TO_SRC("from.");

        public final String localePrefix;

        private GroupDirection(String localePrefix) {
            this.localePrefix = "buildcraft.guide.group." + localePrefix;
        }
    }

    public final Identifier group;
    public final List<PageValue<?>> sources;
    public final List<PageValue<?>> entries;

    public GuideGroupSet(Identifier group) {
        this.group = group;
        this.sources = new ArrayList<>();
        this.entries = new ArrayList<>();
    }

    public String getTitle(GroupDirection dir) {
        String post = group.getNamespace() + "." + group.getPath();
        return buildcraft.lib.misc.LocaleUtil.localize(dir.localePrefix + post);
    }

    public List<PageValue<?>> getValues(GroupDirection direction) {
        return direction == GroupDirection.SRC_TO_ENTRY ? entries : sources;
    }

    public GuideGroupSet addSingle(Object value) {
        PageValue<?> entry = GuideGroupManager.toPageValue(value);
        if (entry != null) entries.add(entry);
        return this;
    }

    public GuideGroupSet addArray(Object... values) {
        for (Object value : values) addSingle(value);
        return this;
    }

    public GuideGroupSet addCollection(Collection<? extends Object> values) {
        for (Object value : values) addSingle(value);
        return this;
    }

    public GuideGroupSet addKey(Object value) {
        PageValue<?> entry = GuideGroupManager.toPageValue(value);
        if (entry != null) sources.add(entry);
        return this;
    }

    public GuideGroupSet addKeyArray(Object... values) {
        for (Object value : values) addKey(value);
        return this;
    }

    public GuideGroupSet addKeyCollection(Collection<? extends Object> values) {
        for (Object value : values) addKey(value);
        return this;
    }
}
