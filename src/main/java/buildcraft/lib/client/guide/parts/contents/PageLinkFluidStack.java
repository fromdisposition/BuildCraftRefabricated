package buildcraft.lib.client.guide.parts.contents;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.profiling.ProfilerFiller;

import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.fluids.FluidTypes;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.entry.FluidStackValueFilter;
import buildcraft.lib.client.guide.entry.PageEntryFluidStack;
import buildcraft.lib.client.guide.entry.PageValue;
import buildcraft.lib.client.guide.parts.GuidePage;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.gui.GuiFluid;
import buildcraft.lib.gui.ISimpleDrawable;

public final class PageLinkFluidStack extends PageLink {

    public final FluidStack stack;
    public final List<String> tooltip;
    public final String searchText;

    public static PageLinkFluidStack create(boolean startVisible, FluidStack stack, ProfilerFiller prof) {
        prof.push("create_page_link_fluid");
        String displayName = FluidTypes.of(stack.getFluid()).getDescription(stack).getString();
        List<String> tooltip = Collections.singletonList(displayName);
        String searchText = displayName.toLowerCase(Locale.ROOT);
        ISimpleDrawable icon = new GuiFluid(stack);
        PageLine text = new PageLine(icon, icon, 2, displayName, true);
        prof.pop();
        return new PageLinkFluidStack(text, startVisible, stack, tooltip, searchText);
    }

    private PageLinkFluidStack(
        PageLine text, boolean startVisible, FluidStack stack, List<String> tooltip, String searchText
    ) {
        super(text, startVisible);
        this.stack = stack;
        this.tooltip = tooltip;
        this.searchText = searchText;
    }

    @Override
    public String getSearchName() {
        return searchText;
    }

    @Override
    public List<String> getTooltip() {
        return tooltip.size() == 1 ? null : tooltip;
    }

    @Override
    public void appendTooltip(GuiGuide gui) {
        if (tooltip.size() > 1) {
            gui.tooltips.add(tooltip);
        }
    }

    @Override
    public GuidePageFactory getFactoryLink() {
        FluidStackValueFilter filter = new FluidStackValueFilter(stack);
        return g -> new GuidePage(g, ImmutableList.of(),
            new PageValue<>(PageEntryFluidStack.INSTANCE, filter));
    }
}
