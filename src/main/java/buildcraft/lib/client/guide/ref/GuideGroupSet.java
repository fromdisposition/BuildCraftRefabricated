/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.ref;

import buildcraft.lib.client.guide.entry.PageValue;
import buildcraft.lib.misc.LocaleUtil;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.resources.Identifier;

public final class GuideGroupSet {
   public final Identifier group;
   public final List<PageValue<?>> sources;
   public final List<PageValue<?>> entries;

   public GuideGroupSet(Identifier group) {
      this.group = group;
      this.sources = new ArrayList<>();
      this.entries = new ArrayList<>();
   }

   public String getTitle(GuideGroupSet.GroupDirection dir) {
      String post = this.group.getNamespace() + "." + this.group.getPath();
      return LocaleUtil.localize(dir.localePrefix + post);
   }

   public List<PageValue<?>> getValues(GuideGroupSet.GroupDirection direction) {
      return direction == GuideGroupSet.GroupDirection.SRC_TO_ENTRY ? this.entries : this.sources;
   }

   public GuideGroupSet addSingle(Object value) {
      this.forEachFlattened(value, this::addSingleElement);
      return this;
   }

   public GuideGroupSet addArray(Object... values) {
      for (Object value : values) {
         this.forEachFlattened(value, this::addSingleElement);
      }

      return this;
   }

   public GuideGroupSet addCollection(Collection<? extends Object> values) {
      for (Object value : values) {
         this.forEachFlattened(value, this::addSingleElement);
      }

      return this;
   }

   public GuideGroupSet addKey(Object value) {
      this.forEachFlattened(value, this::addKeyElement);
      return this;
   }

   public GuideGroupSet addKeyArray(Object... values) {
      for (Object value : values) {
         this.forEachFlattened(value, this::addKeyElement);
      }

      return this;
   }

   public GuideGroupSet addKeyCollection(Collection<? extends Object> values) {
      for (Object value : values) {
         this.forEachFlattened(value, this::addKeyElement);
      }

      return this;
   }

   private void addSingleElement(Object value) {
      PageValue<?> entry = GuideGroupManager.toPageValue(value);
      if (entry != null) {
         this.entries.add(entry);
      }
   }

   private void addKeyElement(Object value) {
      PageValue<?> entry = GuideGroupManager.toPageValue(value);
      if (entry != null) {
         this.sources.add(entry);
      }
   }

   private void forEachFlattened(Object value, java.util.function.Consumer<Object> consumer) {
      if (value != null && value.getClass().isArray()) {
         int length = Array.getLength(value);

         for (int i = 0; i < length; i++) {
            consumer.accept(Array.get(value, i));
         }
      } else {
         consumer.accept(value);
      }
   }

   public enum GroupDirection {
      SRC_TO_ENTRY("to."),
      ENTRY_TO_SRC("from.");

      public final String localePrefix;

      GroupDirection(String localePrefix) {
         this.localePrefix = "buildcraft.guide.group." + localePrefix;
      }
   }
}
