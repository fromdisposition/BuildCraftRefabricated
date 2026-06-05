package buildcraft.lib.client.guide.parts.recipe;

import java.util.Arrays;

import net.minecraft.world.item.ItemStack;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePartItem;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.recipe.ChangingItemStack;

public class GuideCrafting extends GuidePartItem {
    public static final GuiIcon CRAFTING_GRID = new GuiIcon(GuiGuide.ICONS_2, 119, 0, 116, 54);
    public static final GuiRectangle[][] ITEM_POSITION = new GuiRectangle[3][3];
    public static final GuiRectangle OUT_POSITION = new GuiRectangle(95, 19, 16, 16);
    public static final GuiRectangle OFFSET = new GuiRectangle(
        (GuiGuide.PAGE_LEFT_TEXT.width - CRAFTING_GRID.width) / 2, 0, CRAFTING_GRID.width, CRAFTING_GRID.height);
    public static final int PIXEL_HEIGHT = 60;

    static {
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                ITEM_POSITION[x][y] = new GuiRectangle(1 + x * 18, 1 + y * 18, 16, 16);
            }
        }
    }

    private final ChangingItemStack[][] input;
    private final ChangingItemStack output;
    private final int hash;

    GuideCrafting(GuiGuide gui, ChangingItemStack[][] input, ChangingItemStack output) {
        super(gui);
        this.input = input;
        this.output = output;
        this.hash = Arrays.deepHashCode(new Object[] { input, output });
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        GuideCrafting other = (GuideCrafting) obj;
        return Arrays.deepEquals(input, other.input) && output.equals(other.output);
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        if (current.pixel + PIXEL_HEIGHT > height) {
            current = current.newPage();
        }
        x += (int) OFFSET.x;
        y += (int) OFFSET.y + current.pixel;
        if (current.page == index) {
            CRAFTING_GRID.drawAt(x, y);
            for (int itemX = 0; itemX < input.length; itemX++) {
                for (int itemY = 0; itemY < input[itemX].length; itemY++) {
                    GuiRectangle rect = ITEM_POSITION[itemX][itemY];
                    drawItemStack(input[itemX][itemY].get(), x + (int) rect.x, y + (int) rect.y);
                }
            }
            drawItemStack(output.get(), x + (int) OUT_POSITION.x, y + (int) OUT_POSITION.y);
        }
        current = current.nextLine(PIXEL_HEIGHT, height);
        return current;
    }

    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
        int mouseX, int mouseY) {
        if (current.pixel + PIXEL_HEIGHT > height) {
            current = current.newPage();
        }
        x += (int) OFFSET.x;
        y += (int) OFFSET.y + current.pixel;
        if (current.page == index) {
            for (int itemX = 0; itemX < input.length; itemX++) {
                for (int itemY = 0; itemY < input[itemX].length; itemY++) {
                    GuiRectangle rect = ITEM_POSITION[itemX][itemY];
                    testClickItemStack(input[itemX][itemY].get(), x + (int) rect.x, y + (int) rect.y);
                }
            }
            testClickItemStack(output.get(), x + (int) OUT_POSITION.x, y + (int) OUT_POSITION.y);
        }
        current = current.nextLine(PIXEL_HEIGHT, height);
        return current;
    }
}
