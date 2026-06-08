/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.entry;

import buildcraft.api.registry.IScriptableRegistry;
import buildcraft.lib.gui.ISimpleDrawable;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.ProfilerFiller;

public class PageEntryExternal extends PageValueType<String> {
   public static final PageEntryExternal INSTANCE = new PageEntryExternal();

   @Override
   public IScriptableRegistry.OptionallyDisabled<PageEntry<String>> deserialize(Identifier name, JsonObject json, JsonDeserializationContext ctx) {
      String value = PageValue.getTitle(json);
      return new IScriptableRegistry.OptionallyDisabled<>(new PageEntry<>(this, name, json, value));
   }

   @Override
   public Class<String> getEntryClass() {
      return String.class;
   }

   public List<String> getTooltip(String value) {
      return Collections.singletonList(value);
   }

   @Nullable
   public ISimpleDrawable createDrawable(String value) {
      return null;
   }

   public String getTitle(String value) {
      return value;
   }

   @Override
   public void iterateAllDefault(IEntryLinkConsumer consumer, ProfilerFiller prof) {
   }
}
