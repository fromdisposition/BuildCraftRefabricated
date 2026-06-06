package buildcraft.lib.client.guide;

import com.google.common.collect.ImmutableList;

public class TypeOrder {
   public final String localeKey;
   public final ImmutableList<ETypeTag> tags;

   public TypeOrder(String localeKey, ETypeTag... tags) {
      this.localeKey = localeKey;
      this.tags = ImmutableList.copyOf(tags);
   }

   @Override
   public int hashCode() {
      return this.tags.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (this.getClass() != obj.getClass()) {
         return false;
      }

      TypeOrder other = (TypeOrder)obj;
      return this.tags.equals(other.tags);
   }
}
