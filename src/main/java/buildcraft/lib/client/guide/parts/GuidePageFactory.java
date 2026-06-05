package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;

@FunctionalInterface
public interface GuidePageFactory extends GuidePartFactory {
    @Override
    GuidePageBase createNew(GuiGuide gui);
}
