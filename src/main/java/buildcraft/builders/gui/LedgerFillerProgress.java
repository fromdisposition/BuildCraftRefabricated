package buildcraft.builders.gui;

import buildcraft.builders.container.ContainerFiller;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.ledger.Ledger_Neptune;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class LedgerFillerProgress extends Ledger_Neptune {
   private final ContainerFiller container;

   public LedgerFillerProgress(BuildCraftGui gui, ContainerFiller container) {
      super(gui, 7132191, true);
      this.title = "gui.progress";
      this.container = container;
   }

   @Override
   protected void calculateMaxSize() {
      Font font = Minecraft.getInstance().font;
      int overhead = 28;
      int row1 = 20 + font.width(String.valueOf(this.container.getSyncedToBreak()));
      int row2 = 20 + font.width(String.valueOf(this.container.getSyncedToPlace()));
      int titleW = font.width(this.getTitle());
      int contentW = Math.max(Math.max(row1, row2), titleW);
      this.maxWidth = Math.max(22, overhead + contentW);
      this.maxHeight = Math.max(24, 4 + 9 + 3 + 36 + 4);
   }

   @Override
   protected void drawIcon(double x, double y, BCGraphics graphics) {
      graphics.fakeItem(new ItemStack(Items.IRON_INGOT), (int)x, (int)y);
   }

   @Override
   public void drawBackground(float partialTicks) {
      super.drawBackground(partialTicks);
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      if (graphics != null) {
         if (this.interpWidth > 32.0) {
            int scissorX = (int)this.getX() + 2;
            int scissorY = (int)this.getY() + 4;
            int scissorW = (int)(this.interpWidth - 4.0);
            int scissorH = (int)(this.interpHeight - 8.0);
            graphics.enableScissor(scissorX, scissorY, scissorX + scissorW, scissorY + scissorH);
            double iconX = this.getX() + 2.0;
            double iconY = this.getY() + 4.0;
            int textX = (int)iconX + 16 + 4;
            int textY = (int)iconY + 1;
            Font font = Minecraft.getInstance().font;
            textY += 9 + 3;
            graphics.fakeItem(new ItemStack(Items.IRON_PICKAXE), textX, textY);
            graphics.text(font, String.valueOf(this.container.getSyncedToBreak()), textX + 20, textY + 4, -13421773, false);
            textY += 18;
            graphics.fakeItem(new ItemStack(Items.BRICKS), textX, textY);
            graphics.text(font, String.valueOf(this.container.getSyncedToPlace()), textX + 20, textY + 4, -13421773, false);
            graphics.disableScissor();
         }
      }
   }
}
