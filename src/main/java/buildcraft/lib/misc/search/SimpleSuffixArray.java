package buildcraft.lib.misc.search;

import buildcraft.api.core.BCLog;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import net.minecraft.util.profiling.ProfilerFiller;

@SuppressWarnings("unchecked")
public class SimpleSuffixArray<T> implements ISuffixArray<T> {
   private static final boolean ADD_IS_GENERATE = true;
   private static final boolean USE_AVL = true;
   private static final boolean SPLIT_WORDS = true;
   private final List<String> tempAddedNames = new ArrayList<>();
   private final List<T> tempAddedObjects = new ArrayList<>();
   private int maxLength = 0;
   private final Object2ObjectSortedMap<String, List<T>> suffixArray = new Object2ObjectAVLTreeMap();

   @Override
   public void add(T obj, String name) {
      int end = name.length();

      for (int s = name.length() - 1; s >= 0; s--) {
         char c = name.charAt(s);
         if (c != '\n' && c != ' ') {
            String suffix = name.substring(s, end);
            List<T> list = (List<T>)this.suffixArray.get(suffix);
            if (list == null) {
               list = new ArrayList<>();
               this.suffixArray.put(suffix, list);
            }

            this.maxLength = Math.max(this.maxLength, suffix.length());
            list.add(obj);
         } else {
            end = s;
         }
      }
   }

   @Override
   public void generate(ProfilerFiller prof) {
      BCLog.logger.info("[lib.search] Max suffix length is " + this.maxLength);
      ObjectBidirectionalIterator var2 = this.suffixArray.keySet().iterator();

      while (var2.hasNext()) {
         String suffix = (String)var2.next();
         if (suffix.length() == this.maxLength) {
            BCLog.logger.info("[lib.search]   '" + suffix + "'");
         }
      }
   }

   @Override
   public ISuffixArray.SearchResult<T> search(String substring, int maxResults) {
      List<T> entries = new ArrayList<>();
      boolean first = true;
      String[] array = substring.split(" ");

      for (String s : array) {
         Collection<T> real = first ? entries : new HashSet<>();
         ObjectIterator var11 = this.suffixArray.subMap(s, s + "\uffff").values().iterator();

         while (var11.hasNext()) {
            List<T> values = (List<T>)var11.next();
            real.addAll(values);
         }

         if (!first) {
            entries.retainAll(real);
         }

         first = false;
      }

      int realResultCount;
      if (entries.size() > maxResults) {
         realResultCount = entries.size();
         entries.subList(maxResults, entries.size()).clear();
      } else {
         realResultCount = entries.size();
      }

      return new ISuffixArray.SearchResult<>(entries, realResultCount);
   }
}
