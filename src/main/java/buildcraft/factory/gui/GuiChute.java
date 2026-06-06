package buildcraft.factory.gui;

import buildcraft.factory.container.ContainerChute;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiChute extends BcScreen<ContainerChute> {
   private static final Identifier TEXTURE = Identifier.parse("buildcraftfactory:textures/gui/chute.png");
   private static final int SIZE_X = 176;
   private static final int SIZE_Y = 153;
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0.0, 0.0, 176.0, 153.0);
   private static final int SLOTS_X = 62;
   private static final int SLOTS_Y = 18;
   private static final int SLOTS_W = 52;
   private static final int SLOTS_H = 34;

   public GuiChute(ContainerChute menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title, 176, 153);
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
   }

   @Override
   protected void initGuiElements() {
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(62.0, 18.0, 52.0, 34.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.chute.slots.title", -7811960, "buildcraft.help.chute.slots.desc1", "buildcraft.help.chute.slots.desc2")
            )
         );
   }
}
