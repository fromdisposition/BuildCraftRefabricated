package buildcraft.lib.client.guide.parts.contents;

import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.util.profiling.ProfilerFiller;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;

public final class PageLinkItemStack extends PageLink {

    public final ItemStack stack;
    public final List<String> tooltip;
    public final String searchText;

    public static PageLinkItemStack create(boolean startVisible, ItemStack stack, ProfilerFiller prof) {
        prof.push("create_page_link");
        String displayName = stack.getHoverName().getString();
        List<String> tooltip = Collections.singletonList(displayName);
        String searchText = displayName.toLowerCase(java.util.Locale.ROOT);
        ISimpleDrawable icon = new GuiStack(stack);
        PageLine text = new PageLine(icon, icon, 2, displayName, true);
        prof.pop();
        return new PageLinkItemStack(text, startVisible, stack, tooltip, searchText);
    }

    private PageLinkItemStack(
        PageLine text, boolean startVisible, ItemStack stack, List<String> tooltip, String searchText
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
        return GuideManager.INSTANCE.getPageFor(stack);
    }
}
