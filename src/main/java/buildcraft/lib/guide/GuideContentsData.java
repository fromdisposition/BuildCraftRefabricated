package buildcraft.lib.guide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

public class GuideContentsData {
   public static final GuideContentsData EMPTY = new GuideContentsData(null);
   @Nullable
   public final GuideBook book;
   public final List<String> loadedMods = new ArrayList<>();
   public final List<String> loadedOther = new ArrayList<>();

   public GuideContentsData(@Nullable GuideBook book) {
      this.book = book;
   }

   public void generate(Set<String> domains) {
      this.loadedMods.clear();
      this.loadedOther.clear();

      for (String domain : domains) {
         if (domain == null) {
            throw new IllegalArgumentException("Was given a null domain!");
         }

         if (domain.startsWith("buildcraft")) {
            this.loadedMods.add("BuildCraft");
         } else {
            this.loadedMods.add(domain);
         }
      }

      Collections.sort(this.loadedMods);
      Collections.sort(this.loadedOther);
   }
}
