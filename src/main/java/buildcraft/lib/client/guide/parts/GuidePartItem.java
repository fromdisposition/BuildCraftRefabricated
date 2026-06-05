package buildcraft.lib.client.guide.parts;

import net.minecraft.client.Minecraft;
import buildcraft.lib.gui.BCGraphics;
import net.minecraft.world.item.ItemStack;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.ItemStackKey;

public abstract class GuidePartItem extends GuidePart {
    public static final GuiRectangle STACK_RECT = new GuiRectangle(0, 0, 16, 16);

    public GuidePartItem(GuiGuide gui) {
        super(gui);
    }

    protected void drawItemStack(ItemStackKey stack, int x, int y) {
        drawItemStack(stack.baseStack, x, y);
    }

    protected void drawItemStack(ItemStack stack, int x, int y) {
        if (stack == null || stack.isEmpty()) return;

        BCGraphics graphics = GuiIcon.getGuiGraphics();
        if (graphics != null) {

            graphics.fakeItem(stack, x, y);
            graphics.itemDecorations(Minecraft.getInstance().font, stack, x, y);
        }

        if (STACK_RECT.offset(x, y).contains(gui.mouse)) {
            gui.tooltipStack = stack;
        }
    }

    protected void testClickItemStack(ItemStackKey stack, int x, int y) {
        testClickItemStack(stack.baseStack, x, y);
    }

    protected void testClickItemStack(ItemStack stack, int x, int y) {
        if (stack != null && !stack.isEmpty() && STACK_RECT.offset(x, y).contains(gui.mouse)) {
            GuidePageFactory factory = GuideManager.INSTANCE.getPageFor(stack);
            if (factory != null) {
                gui.openPage(factory.createNew(gui));
            }
        }
    }
}
