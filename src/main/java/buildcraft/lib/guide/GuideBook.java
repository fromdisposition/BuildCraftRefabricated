/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.guide;

import buildcraft.api.registry.IScriptableRegistry;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class GuideBook {
   public static final IScriptableRegistry.ISimpleEntryDeserializer<GuideBook> DESERIALISER = GuideBook::deserialize;
   public final Identifier name;
   public final Identifier itemIcon;
   public final Component title;
   public final boolean appendAllEntries;
   public final GuideContentsData data = new GuideContentsData(this);

   private static GuideBook deserialize(Object nameObj, JsonObject json, JsonDeserializationContext ctx) {
      Identifier name = (Identifier)nameObj;
      Identifier itemIcon = Identifier.parse("buildcraftlib:guide_main");
      String titleStr = json.has("title") ? json.get("title").getAsString() : name.toString();
      Component title = Component.translatable(titleStr);
      boolean addAll = json.has("all_entries") ? json.get("all_entries").getAsBoolean() : true;
      return new GuideBook(name, itemIcon, title, addAll);
   }

   public GuideBook(Identifier name, Identifier itemIcon, Component title, boolean appendAllEntries) {
      this.name = name;
      this.itemIcon = itemIcon;
      this.title = title;
      this.appendAllEntries = appendAllEntries;
   }

   @Override
   public String toString() {
      return "GuideBook [ " + this.name + ", title = " + this.title.getString() + " ]";
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else {
         return obj != null && obj.getClass() == this.getClass() ? this.name.equals(((GuideBook)obj).name) : false;
      }
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }
}
