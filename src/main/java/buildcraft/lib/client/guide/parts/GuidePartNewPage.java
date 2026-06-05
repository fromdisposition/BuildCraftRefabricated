package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;

public class GuidePartNewPage extends GuidePart {

    private final int minPixelThreshold;

    public GuidePartNewPage(GuiGuide gui) {
        this(gui, 0);
    }

    public GuidePartNewPage(GuiGuide gui, int minPixelThreshold) {
        super(gui);
        this.minPixelThreshold = minPixelThreshold;
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        if (current.pixel < minPixelThreshold) {
            return current;
        }
        return current.newPage();
    }

    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
        int mouseX, int mouseY) {
        if (current.pixel < minPixelThreshold) {
            return current;
        }
        return current.newPage();
    }
}
