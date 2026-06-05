package buildcraft.lib.guide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

public class GuideContentsData {

    public static final GuideContentsData EMPTY = new GuideContentsData(null);

    public final @Nullable GuideBook book;

    public final List<String> loadedMods = new ArrayList<>();
    public final List<String> loadedOther = new ArrayList<>();

    public GuideContentsData(@Nullable GuideBook book) {
        this.book = book;
    }

    public void generate(Set<String> domains) {
        loadedMods.clear();
        loadedOther.clear();
        for (String domain : domains) {
            if (domain == null) {
                throw new IllegalArgumentException("Was given a null domain!");
            }
            if (domain.startsWith("buildcraft")) {
                loadedMods.add("BuildCraft");
            } else {
                loadedMods.add(domain);
            }
        }
        Collections.sort(loadedMods);
        Collections.sort(loadedOther);
    }
}
