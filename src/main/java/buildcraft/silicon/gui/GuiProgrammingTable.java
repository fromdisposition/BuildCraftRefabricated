package buildcraft.silicon.gui;

import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.silicon.container.ContainerProgrammingTable;
import buildcraft.silicon.tile.TileProgrammingTable;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiProgrammingTable extends BcScreen<ContainerProgrammingTable> {
   private static final Identifier TEXTURE_BASE = Identifier.parse("buildcraftsilicon:textures/gui/programming_table.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0.0, 0.0, 176.0, 207.0);
   private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, 176.0, 18.0, 4.0, 70.0);
   private static final GuiIcon ICON_OPTION_SELECTED = new GuiIcon(TEXTURE_BASE, 196.0, 1.0, 16.0, 16.0);
   private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(164.0, 36.0, 4.0, 70.0);

   public GuiProgrammingTable(ContainerProgrammingTable container, Inventory playerInventory, Component title) {
      super(container, playerInventory, title, 176, 207);
   }

   @Override
   protected void initGuiElements() {
      this.mainGui.shownElements.add(new LedgerTablePower(this.mainGui, this.menu.tile, true));
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(8.0, 36.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.programming_table.input.title", -13176, "buildcraft.help.programming_table.input.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(43.0, 36.0, 106.0, 70.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.programming_table.options.title", -7811960, "buildcraft.help.programming_table.options.desc")
            )
         );
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
      TileProgrammingTable tile = this.menu.tile;
      long target = tile.getTarget();
      if (target > 0L) {
         double v = (double)tile.power / target;
         ICON_PROGRESS.drawCutInside(
            new GuiRectangle(
                  RECT_PROGRESS.x,
                  (int)(RECT_PROGRESS.y + RECT_PROGRESS.height * Math.max(1.0 - v, 0.0)),
                  RECT_PROGRESS.width,
                  (int)Math.ceil(RECT_PROGRESS.height * Math.min(v, 1.0))
               )
               .offset(this.mainGui.rootElement)
         );
      }

      for (int i = 0; i < TileProgrammingTable.WIDTH * TileProgrammingTable.HEIGHT; i++) {
         if (!tile.getOptionStack(i).isEmpty() && tile.optionId == i) {
            IGuiArea area = this.getOptionArea(i);
            ICON_OPTION_SELECTED.drawAt(area);
         }
      }
   }

   private IGuiArea getOptionArea(int index) {
      int x = index % TileProgrammingTable.WIDTH;
      int y = index / TileProgrammingTable.WIDTH;
      return new GuiRectangle(43 + x * 18, 36 + y * 18, 16.0, 16.0).offset(this.mainGui.rootElement);
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      String title = I18n.get("block.buildcraftsilicon.programming_table", new Object[0]);
      graphics.text(this.font, title, (this.imageWidth - this.font.width(title)) / 2, 15, -12566464, false);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (event.button() == 0) {
         int mouseX = (int)event.x();
         int mouseY = (int)event.y();

         for (int i = 0; i < TileProgrammingTable.WIDTH * TileProgrammingTable.HEIGHT; i++) {
            if (!this.menu.tile.getOptionStack(i).isEmpty() && this.getOptionArea(i).contains(mouseX, mouseY)) {
               if (this.minecraft != null && this.minecraft.gameMode != null) {
                  this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, i);
               }

               return true;
            }
         }
      }

      return super.mouseClicked(event, doubleClick);
   }
}
