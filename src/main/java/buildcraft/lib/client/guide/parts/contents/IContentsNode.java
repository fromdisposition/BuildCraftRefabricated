package buildcraft.lib.client.guide.parts.contents;

import java.util.Set;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePart;

public interface IContentsNode {
    String getSearchName();

    int getSortIndex();

    boolean isVisible();
    void calcVisibility();
    void resetVisibility();
    void setVisible(Set<PageLink> matches);
    void sort();
    IContentsNode[] getVisibleChildren();
    void addChild(IContentsNode node);
    GuidePart createGuidePart(GuiGuide gui);
}
