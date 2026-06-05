package buildcraft.lib.client.guide.loader;

import java.io.IOException;
import java.io.InputStream;

import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.ProfilerFiller;

import buildcraft.lib.client.guide.entry.PageEntry;
import buildcraft.lib.client.guide.parts.GuidePageFactory;

public interface IPageLoader {
    GuidePageFactory loadPage(InputStream in, Identifier name, PageEntry<?> entry, ProfilerFiller prof) throws IOException;
}
