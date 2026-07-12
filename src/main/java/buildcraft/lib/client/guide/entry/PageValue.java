/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.entry;

import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.LocaleUtil;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class PageValue<T> {
   public final PageValueType<T> type;
   public final String title;
   public final T value;

   public PageValue(PageValueType<T> type, T value) {
      this(type, value, null);
   }

   public PageValue(PageValueType<T> type, T value, @Nullable String titleOverride) {
      this.type = type;
      this.title = titleOverride != null ? titleOverride : type.getTitle(value);
      this.value = value;
   }

   public static String getTitle(JsonObject json) {
      String override = getTitleOverride(json);
      return override != null ? override : "untitled";
   }

   @Nullable
   public static String getTitleOverride(JsonObject json) {
      if (json.has("title")) {
         String str = json.get("title").getAsString();
         String prefixed = "buildcraft.guide.page." + str;
         String localized = LocaleUtil.localize(prefixed);
         return prefixed.equals(localized) ? str : localized;
      } else {
         return json.has("title_raw") ? json.get("title_raw").getAsString() : null;
      }
   }

   public boolean matches(Object test) {
      return this.type.matches(this.value, test);
   }

   @Nullable
   public ISimpleDrawable createDrawable() {
      return this.type.createDrawable(this.value);
   }

   public Object getBasicValue() {
      return this.type.getBasicValue(this.value);
   }

   public List<String> getTooltip() {
      return this.type.getTooltip(this.value);
   }

   public PageValue<T> copyToValue() {
      return new PageValue<>(this.type, this.value);
   }

   @Override
   public int hashCode() {
      return this.value.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (obj.getClass() != this.getClass()) {
         return false;
      }

      PageValue<?> other = (PageValue<?>)obj;
      return Objects.equals(this.value, other.value);
   }
}
