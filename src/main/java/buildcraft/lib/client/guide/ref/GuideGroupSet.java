package buildcraft.lib.client.guide.ref;

import buildcraft.lib.client.guide.entry.PageValue;
import buildcraft.lib.misc.LocaleUtil;
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
      PageValue<?> entry = GuideGroupManager.toPageValue(value);
      if (entry != null) {
         this.entries.add(entry);
      }

      return this;
   }

   public GuideGroupSet addArray(Object... values) {
      for (Object value : values) {
         this.addSingle(value);
      }

      return this;
   }

   public GuideGroupSet addCollection(Collection<? extends Object> values) {
      for (Object value : values) {
         this.addSingle(value);
      }

      return this;
   }

   public GuideGroupSet addKey(Object value) {
      PageValue<?> entry = GuideGroupManager.toPageValue(value);
      if (entry != null) {
         this.sources.add(entry);
      }

      return this;
   }

   public GuideGroupSet addKeyArray(Object... values) {
      for (Object value : values) {
         this.addKey(value);
      }

      return this;
   }

   public GuideGroupSet addKeyCollection(Collection<? extends Object> values) {
      for (Object value : values) {
         this.addKey(value);
      }

      return this;
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
