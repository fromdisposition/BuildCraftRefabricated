package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;

@FunctionalInterface
public interface GuidePageFactory extends GuidePartFactory {
   GuidePageBase createNew(GuiGuide var1);
}
