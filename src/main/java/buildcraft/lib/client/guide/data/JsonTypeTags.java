package buildcraft.lib.client.guide.data;

import buildcraft.api.core.BCLog;
import buildcraft.lib.client.guide.ETypeTag;
import buildcraft.lib.client.guide.TypeOrder;
import java.util.ArrayList;
import java.util.List;

public class JsonTypeTags {
   public static final JsonTypeTags EMPTY = new JsonTypeTags("", "", "");
   public final String domain;
   public final String type;
   public final String subType;

   public JsonTypeTags(String domain, String type, String subType) {
      this.domain = domain;
      this.type = type;
      this.subType = subType;
   }

   public JsonTypeTags(String type) {
      this(null, type, null);
   }

   public String[] getOrdered(TypeOrder typeOrder) {
      if (this.domain == null && this.subType == null) {
         return new String[]{this.type};
      }

      List<String> collected = new ArrayList<>(typeOrder.tags.size());

      for (int i = 0; i < typeOrder.tags.size(); i++) {
         ETypeTag tag = (ETypeTag)typeOrder.tags.get(i);
         String raw = this.getRaw(tag);
         if (raw != null && !raw.isEmpty()) {
            collected.add(tag.preText + raw);
         }
      }

      return collected.toArray(new String[0]);
   }

   private String getRaw(ETypeTag tag) {
      if (tag == ETypeTag.MOD) {
         return getMod(this.domain, 0);
      } else if (tag == ETypeTag.SUB_MOD) {
         return getMod(this.domain, 1);
      } else if (tag == ETypeTag.TYPE) {
         return this.type;
      } else if (tag == ETypeTag.SUB_TYPE) {
         return this.subType;
      } else {
         throw new IllegalStateException("Don't know the type " + tag);
      }
   }

   private String getTyped(ETypeTag tag) {
      String raw = this.getRaw(tag);
      return tag.preText + (raw == null ? "" : raw);
   }

   private static String getMod(String domain, int index) {
      if (domain.startsWith("buildcraft")) {
         return index == 0 ? "buildcraft" : domain.substring("buildcraft".length());
      } else {
         return index == 0 ? domain : "";
      }
   }

   public JsonTypeTags inheritMissingTags(JsonTypeTags parent) {
      String d = firstNonEmpty(this.domain, parent.domain, "unknown");
      String t = firstNonEmpty(this.type, parent.type, "unknown");
      String st = firstNonEmpty(this.subType, parent.subType, "unknown");
      return new JsonTypeTags(d, t, st);
   }

   private static String firstNonEmpty(String... strings) {
      String current = null;

      for (String string : strings) {
         current = string;
         if (current != null && !current.isBlank()) {
            break;
         }
      }

      return current;
   }

   public void printContents(int indent) {
      StringBuilder f = new StringBuilder();

      while (indent > 0) {
         f.append("  ");
         indent--;
      }

      BCLog.logger.info(f + "domain = " + this.domain + ",");
      BCLog.logger.info(f + "type = " + this.type + ",");
      BCLog.logger.info(f + "sub_type = " + this.subType);
   }
}
