package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;

@FunctionalInterface
public interface GuidePartFactory {
   GuidePart createNew(GuiGuide var1);
}
