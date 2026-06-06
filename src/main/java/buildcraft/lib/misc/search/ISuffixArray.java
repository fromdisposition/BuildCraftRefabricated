package buildcraft.lib.misc.search;

import java.util.List;
import net.minecraft.util.profiling.ProfilerFiller;

public interface ISuffixArray<T> {
   void add(T var1, String var2);

   void generate(ProfilerFiller var1);

   ISuffixArray.SearchResult<T> search(String var1, int var2);

   final class SearchResult<T> {
      public final List<T> results;
      public final int realResultCount;

      public SearchResult(List<T> results, int realResultCount) {
         this.results = results;
         this.realResultCount = realResultCount;
      }

      public SearchResult(List<T> results) {
         this(results, results.size());
      }

      public boolean hasAllResults() {
         return this.results.size() == this.realResultCount;
      }
   }
}
