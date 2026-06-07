package buildcraft.robotics.gui;

import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.robotics.container.ContainerRequester;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiRequester extends BcScreen<ContainerRequester> {
   private static final Identifier TEXTURE_BASE = Identifier.parse("buildcraftrobotics:textures/gui/requester.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0.0, 0.0, 196.0, 181.0);

   public GuiRequester(ContainerRequester container, Inventory playerInventory, Component title) {
      super(container, playerInventory, title, 196, 181);
   }

   @Override
   protected void initGuiElements() {
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      String title = I18n.get("block.buildcraftrobotics.requester", new Object[0]);
      graphics.text(this.font, title, (this.imageWidth - this.font.width(title)) / 2, 6, -12566464, false);
   }
}
