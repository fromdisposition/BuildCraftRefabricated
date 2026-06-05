package buildcraft.lib.misc.search;

import net.minecraft.resources.Identifier;

import java.util.List;

import net.minecraft.util.profiling.ProfilerFiller;

public interface ISuffixArray<T> {
    void add(T obj, String name);

    void generate(ProfilerFiller prof);

    SearchResult<T> search(String substring, int maxResults);

    public static final class SearchResult<T> {
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
            return results.size() == realResultCount;
        }
    }
}
