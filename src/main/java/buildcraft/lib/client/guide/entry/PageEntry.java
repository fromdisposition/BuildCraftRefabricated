package buildcraft.lib.client.guide.entry;

import buildcraft.lib.client.guide.data.JsonTypeTags;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;

public final class PageEntry<T> extends PageValue<T> {
   public final JsonTypeTags typeTags;
   public final Identifier book;
   public final boolean creativeOnly;
   public final int sortIndex;

   public PageEntry(PageValueType<T> type, JsonTypeTags typeTags, Identifier book, T value) {
      this(type, typeTags, book, value, false);
   }

   public PageEntry(PageValueType<T> type, JsonTypeTags typeTags, Identifier book, T value, boolean creativeOnly) {
      super(type, value);
      this.typeTags = typeTags;
      this.book = book;
      this.creativeOnly = creativeOnly;
      this.sortIndex = 0;
   }

   public PageEntry(PageValueType<T> type, Identifier name, JsonObject json, T value) {
      super(type, value, PageValue.getTitleOverride(json));
      this.book = json.has("book") ? Identifier.parse(json.get("book").getAsString()) : Identifier.parse("buildcraftcore:guide");
      String tagType = json.has("tag_type") ? json.get("tag_type").getAsString() : "";
      String subType = json.has("tag_subtype") ? json.get("tag_subtype").getAsString() : "";
      this.typeTags = new JsonTypeTags(name.getNamespace(), tagType, subType);
      this.creativeOnly = json.has("creative_only") && json.get("creative_only").getAsBoolean();
      this.sortIndex = json.has("sort") ? json.get("sort").getAsInt() : 0;
   }

   public PageValue<T> toPageValue() {
      return new PageValue<>(this.type, this.value);
   }

   @Override
   public String toString() {
      return this.value.getClass().getSimpleName() + ": " + this.value;
   }
}
