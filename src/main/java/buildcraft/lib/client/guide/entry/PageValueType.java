/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.entry;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.util.profiling.ProfilerFiller;

import buildcraft.api.registry.IScriptableRegistry.OptionallyDisabled;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.gui.ISimpleDrawable;

public abstract class PageValueType<T> {

    public abstract OptionallyDisabled<PageEntry<T>> deserialize(Identifier name, JsonObject json,
        JsonDeserializationContext ctx);

    public abstract Class<T> getEntryClass();

    public boolean matches(T value, Object test) {
        return Objects.equals(test, value);
    }

    @Nullable
    public abstract ISimpleDrawable createDrawable(T value);

    public Object getBasicValue(T value) {
        return value;
    }

    public abstract String getTitle(T value);

    public abstract List<String> getTooltip(T value);

    public abstract void iterateAllDefault(IEntryLinkConsumer consumer, ProfilerFiller prof);

    public OptionallyDisabled<Object> createLink(String to, ProfilerFiller prof) {
        return new OptionallyDisabled<>(getClass().getSimpleName() + " doesn't support links");
    }

    @Nullable
    public final PageValue<T> wrap(Object value) {
        T typed = getEntryClass().cast(value);
        if (isValid(typed)) {
            return new PageValue<>(this, typed);
        } else {
            return null;
        }
    }

    protected boolean isValid(T typed) {
        return true;
    }

    public void addPageEntries(T value, GuiGuide gui, List<GuidePart> parts) {}
}
