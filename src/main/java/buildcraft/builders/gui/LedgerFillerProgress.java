package buildcraft.builders.gui;

import net.minecraft.client.Minecraft;
import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

import buildcraft.builders.container.ContainerFiller;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.ledger.Ledger_Neptune;

public class LedgerFillerProgress extends Ledger_Neptune {
    private final ContainerFiller container;

    public LedgerFillerProgress(BuildCraftGui gui, ContainerFiller container) {
        super(gui, 0x6CD41F, true);
        this.title = "gui.progress";
        this.container = container;
    }

    @Override
    protected void calculateMaxSize() {
        Font font = Minecraft.getInstance().font;
        int overhead = 2 + 16 + LEDGER_GAP + LEDGER_GAP + 2;

        int row1 = 16 + LEDGER_GAP + font.width(String.valueOf(container.getSyncedToBreak()));
        int row2 = 16 + LEDGER_GAP + font.width(String.valueOf(container.getSyncedToPlace()));
        int titleW = font.width(getTitle());
        int contentW = Math.max(Math.max(row1, row2), titleW);

        maxWidth = Math.max(CLOSED_WIDTH, overhead + contentW);

        maxHeight = Math.max(CLOSED_HEIGHT, LEDGER_GAP + (font.lineHeight + 3) + 2 * 18 + LEDGER_GAP);
    }

    @Override
    protected void drawIcon(double x, double y, BCGraphics graphics) {
        graphics.fakeItem(new ItemStack(Items.IRON_INGOT), (int) x, (int) y);
    }

    @Override
    public void drawBackground(float partialTicks) {
        super.drawBackground(partialTicks);

        BCGraphics graphics = GuiIcon.getGuiGraphics();
        if (graphics == null) return;

        if (interpWidth > CLOSED_WIDTH + 10) {
            int scissorX = (int) getX() + 2;
            int scissorY = (int) getY() + 4;
            int scissorW = (int) (interpWidth - LEDGER_GAP);
            int scissorH = (int) (interpHeight - LEDGER_GAP * 2);
            graphics.enableScissor(scissorX, scissorY, scissorX + scissorW, scissorY + scissorH);

            double iconX = getX() + 2;
            double iconY = getY() + 4;

            int textX = (int) iconX + 16 + LEDGER_GAP;
            int textY = (int) iconY + 1;

            Font font = Minecraft.getInstance().font;
            textY += font.lineHeight + 3;

            graphics.fakeItem(new ItemStack(Items.IRON_PICKAXE), textX, textY);
            graphics.text(font, String.valueOf(container.getSyncedToBreak()), textX + 20, textY + 4, 0xFF333333 | 0xFF000000, false);

            textY += 18;

            graphics.fakeItem(new ItemStack(Items.BRICKS), textX, textY);
            graphics.text(font, String.valueOf(container.getSyncedToPlace()), textX + 20, textY + 4, 0xFF333333 | 0xFF000000, false);

            graphics.disableScissor();
        }
    }
}
