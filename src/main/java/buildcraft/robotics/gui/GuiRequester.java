/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.gui;

import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.robotics.container.ContainerRequester;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiRequester extends BcScreen<ContainerRequester> {
   private static final Identifier TEXTURE_BASE = Identifier.parse("buildcraftrobotics:textures/gui/requester.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0.0, 0.0, 196.0, 204.0);
   // The 196-wide texture leaves machine chrome on BOTH sides of the 176-wide player inventory (panel at x=11..187).
   // A mod-extended inventory draws the generic vanilla panel whose opaque edges (left: black outline + white bevel
   // x=11..13; right: dark + black x=184..186) clash with the machine frame. These re-blit the requester's own
   // light frame over both edges, plus a 1px foot each for the bottom-corner shadow. Anchored to the player
   // inventory: the tall patch spans its first row minus 1 (y=121) down to the panel's bottom frame (y=200), and
   // the foot covers that frame (y=201..202). Source == draw position, so they just redraw the texture's own pixels.
   private static final GuiIcon ICON_EDGE_L = new GuiIcon(TEXTURE_BASE, 11.0, 121.0, 3.0, 80.0);
   private static final GuiIcon ICON_EDGE_R = new GuiIcon(TEXTURE_BASE, 184.0, 121.0, 3.0, 80.0);
   private static final GuiIcon ICON_EDGE_L_FOOT = new GuiIcon(TEXTURE_BASE, 11.0, 201.0, 1.0, 2.0);
   private static final GuiIcon ICON_EDGE_R_FOOT = new GuiIcon(TEXTURE_BASE, 186.0, 201.0, 1.0, 2.0);

   public GuiRequester(ContainerRequester container, Inventory playerInventory, Component title) {
      super(container, playerInventory, title, 196, heightForSlots(container, 204));
   }

   @Override
   protected void drawExtendedInventoryChrome() {
      ICON_EDGE_L.drawAt(this.mainGui.rootElement.offset(11.0, 121.0));
      ICON_EDGE_R.drawAt(this.mainGui.rootElement.offset(184.0, 121.0));
      ICON_EDGE_L_FOOT.drawAt(this.mainGui.rootElement.offset(11.0, 201.0));
      ICON_EDGE_R_FOOT.drawAt(this.mainGui.rootElement.offset(186.0, 201.0));
   }

   @Override
   protected void initGuiElements() {
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(9.0, 18.0, 70.0, 88.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.requester.requests.title", -13176, "buildcraft.help.requester.requests.desc1", "buildcraft.help.requester.requests.desc2")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(117.0, 18.0, 70.0, 88.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.requester.items.title", -7811960, "buildcraft.help.requester.items.desc")
            )
         );
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      // Both labels sit on their own content's left edge, which is not the vanilla x=8 here: this GUI is 196 wide, so
      // the request grid starts at x=9 and the 176-wide player inventory is inset to x=19.
      graphics.text(this.font, I18n.get("block.buildcraftrobotics.requester", new Object[0]), 9, 6, -12566464, false);
      // Y = playerInventoryLabelY() derives from the real slot rows (firstPlayerRowY() - 12), so it follows the
      // inventory automatically.
      graphics.text(this.font, this.playerInventoryTitle, 19, this.playerInventoryLabelY(), -12566464, false);
   }
}
