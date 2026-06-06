package buildcraft.lib.client.guide.loader;

import buildcraft.lib.client.guide.entry.PageEntry;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.ProfilerFiller;

public interface IPageLoader {
   GuidePageFactory loadPage(InputStream var1, Identifier var2, PageEntry<?> var3, ProfilerFiller var4) throws IOException;
}
