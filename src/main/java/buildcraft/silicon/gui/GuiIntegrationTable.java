package buildcraft.silicon.gui;

import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.silicon.container.ContainerIntegrationTable;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiIntegrationTable extends BcScreen<ContainerIntegrationTable> {
   private static final Identifier TEXTURE_BASE = Identifier.parse("buildcraftsilicon:textures/gui/integration_table.png");
   private static final int SIZE_X = 176;
   private static final int SIZE_Y = 191;
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0.0, 0.0, 176.0, 191.0);
   private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, 176.0, 0.0, 4.0, 70.0);
   private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(164.0, 22.0, 4.0, 70.0);
   private static final int INPUT_X = 19;
   private static final int INPUT_Y = 24;
   private static final int INPUT_W = 66;
   private static final int INPUT_H = 66;
   private static final int OUTPUT_X = 101;
   private static final int OUTPUT_Y = 36;
   private static final int OUTPUT_W = 53;
   private static final int OUTPUT_H = 29;

   public GuiIntegrationTable(ContainerIntegrationTable container, Inventory playerInventory, Component title) {
      super(container, playerInventory, title, 176, 191);
   }

   @Override
   protected void initGuiElements() {
      this.mainGui.shownElements.add(new LedgerTablePower(this.mainGui, ((ContainerIntegrationTable)this.menu).tile, true));
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(19.0, 24.0, 66.0, 66.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.integration_table.input.title", -13176, "buildcraft.help.integration_table.input.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(101.0, 36.0, 53.0, 29.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.integration_table.output.title", -7811960, "buildcraft.help.integration_table.output.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               RECT_PROGRESS.offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.integration_table.power.title", -2249985, "buildcraft.help.integration_table.power.desc")
            )
         );
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
      long target = ((ContainerIntegrationTable)this.menu).tile.getTarget();
      if (target != 0L) {
         double v = (double)((ContainerIntegrationTable)this.menu).tile.power / target;
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
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      String title = I18n.get("block.buildcraftsilicon.integration_table", new Object[0]);
      graphics.text(this.font, title, (this.imageWidth - this.font.width(title)) / 2, 10, -12566464, false);
   }
}
