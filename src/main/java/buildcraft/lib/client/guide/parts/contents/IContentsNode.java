package buildcraft.lib.client.guide.parts.contents;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePart;
import java.util.Set;

public interface IContentsNode {
   String getSearchName();

   int getSortIndex();

   boolean isVisible();

   void calcVisibility();

   void resetVisibility();

   void setVisible(Set<PageLink> var1);

   void sort();

   IContentsNode[] getVisibleChildren();

   void addChild(IContentsNode var1);

   GuidePart createGuidePart(GuiGuide var1);
}
