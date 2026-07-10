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
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.robotics.container.ContainerZonePlanner;
import buildcraft.robotics.tile.TileZonePlanner;
//? if >= 1.21.10 {
import net.minecraft.client.input.KeyEvent;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiZonePlanner extends BcScreen<ContainerZonePlanner> {
   private static final Identifier TEXTURE = Identifier.parse("buildcraftrobotics:textures/gui/bcr/zone_planner.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0.0, 0.0, 256.0, 244.0);
   // The texture only carries the right-pointing (import) arrow now. The export arrow is the same sprite drawn
   // transposed -- see drawProgressDown.
   private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE, 0.0, 244.0, 28.0, 9.0);
   private static final int PROGRESS_U = 0;
   private static final int PROGRESS_V = 244;
   private static final int PROGRESS_LENGTH = 28;
   private static final int PROGRESS_THICKNESS = 9;
   // Native divider strip between the paintbrush grid and the player inventory (tex x=80..82). Masks the generic
   // vanilla panel's intruding left edge (opaque black outline + white bevel) when a mod-extended inventory is
   // drawn here; the 1px foot restores the divider's bottom shadow.
   private static final GuiIcon ICON_INV_DIVIDER = new GuiIcon(TEXTURE, 80.0, 161.0, 3.0, 80.0);
   private static final GuiIcon ICON_INV_DIVIDER_FOOT = new GuiIcon(TEXTURE, 80.0, 241.0, 1.0, 2.0);
   private boolean requestedLayers = false;

   public GuiZonePlanner(ContainerZonePlanner menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title, 256, heightForSlots(menu, 244));
   }

   @Override
   protected void init() {
      super.init();
      if (!this.requestedLayers) {
         ((ContainerZonePlanner)this.getMenu()).requestLayers();
         this.requestedLayers = true;
      }
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
      TileZonePlanner tile = ((ContainerZonePlanner)this.getMenu()).tile;
      if (tile != null) {
         int left = this.getGuiLeftPos();
         int top = this.getGuiTopPos();
         double fracIn = progressFraction(tile.getProgressInput());
         double fracOut = progressFraction(tile.getProgressOutput());
         if (fracIn > 0.0) {
            ICON_PROGRESS.drawCutInside(left + 44, top + 133, PROGRESS_LENGTH * fracIn, PROGRESS_THICKNESS);
         }

         if (fracOut > 0.0) {
            drawProgressDown(graphics, left + 236, top + 54, fracOut);
         }
      }
   }

   /**
    * Draws the single right-pointing progress sprite as a down-pointing one at (x, y), filling top to bottom.
    *
    * <p>The needed mapping is the transpose (sprite pixel (sx, sy) -> screen (x + sy, y + sx)): a plain 90 degree turn
    * would put the sprite's bottom shadow row on the arrow's left instead of its right. A transpose mirrors, and a
    * mirroring pose is not an option -- GUI_TEXTURED culls back faces, so the flipped winding would drop the quad. So
    * the mirror is folded into the source instead: under a clockwise quarter turn, feeding the sprite's rows bottom-up
    * is exactly the transpose. Hence one 1px-tall blit per sprite row, row {@code r} placed at local y = -r-1 so that
    * the turn lands it on screen column x + r.
    */
   private static void drawProgressDown(BCGraphics graphics, int x, int y, double fraction) {
      int filled = (int)(PROGRESS_LENGTH * fraction);
      if (filled > 0) {
         graphics.pushPoseGui();
         graphics.translateGui(x, y);
         graphics.rotateGui((float)(Math.PI / 2.0));

         for (int row = 0; row < PROGRESS_THICKNESS; row++) {
            graphics.blit(TEXTURE, 0, -row - 1, PROGRESS_U, PROGRESS_V + row, filled, 1, 256, 256);
         }

         graphics.popPoseGui();
      }
   }

   @Override
   protected void drawExtendedInventoryChrome() {
      // A mod-extended inventory draws the generic vanilla panel at x=80; its left edge (opaque black outline +
      // white bevel) clashes with the light paintbrush/inventory divider. Re-blit the machine's own divider slice
      // over it, plus a 1px foot for the divider's bottom shadow.
      ICON_INV_DIVIDER.drawAt(this.mainGui.rootElement.offset(80.0, 161.0));
      ICON_INV_DIVIDER_FOOT.drawAt(this.mainGui.rootElement.offset(80.0, 241.0));
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      // x=8 is the canonical title anchor and also the map's left edge; the label sits in the 14px band above it.
      graphics.text(this.font, this.title.getString(), 8, 6, -12566464, false);
      // The brush grid and the player inventory share a first row, so both labels share playerInventoryLabelY()
      // (firstPlayerRowY() - 12) and stay level. Each sits on its own grid's left edge: this GUI is 256 wide, so the
      // 176-wide player inventory is inset to x=88 rather than the vanilla x=8.
      int labelY = this.playerInventoryLabelY();
      graphics.text(this.font, LocaleUtil.localize("buildcraft.gui.zone_planner.brushes"), 8, labelY, -12566464, false);
      graphics.text(this.font, this.playerInventoryTitle, 88, labelY, -12566464, false);
   }

   private static double progressFraction(int progress) {
      return progress < 0 ? 0.0 : Math.min(1.0, progress / 200.0);
   }

   //? if >= 1.21.10 {
   public boolean keyPressed(KeyEvent event) {
      return this.mainGui.onKeyTyped('\u0000', event.key()) ? true : super.keyPressed(event);
   }
   //?} else {
   /*public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.mainGui.onKeyTyped('\u0000', keyCode) ? true : super.keyPressed(keyCode, scanCode, modifiers);
   }
   *///?}

   @Override
   protected void initGuiElements() {
      TileZonePlanner tile = ((ContainerZonePlanner)this.getMenu()).tile;
      this.mainGui.shownElements.add(new ZonePlannerMapElement(this, tile, 8, 18, 213, 100));
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(8.0, 18.0, 213.0, 100.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.zone_planner.map.title", -7811960, "buildcraft.help.zone_planner.map.desc1", "buildcraft.help.zone_planner.map.desc2"
               )
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(8.0, 162.0, 70.0, 70.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.zone_planner.paintbrushes.title", -13176, "buildcraft.help.zone_planner.paintbrushes.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(8.0, 130.0, 36.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.zone_planner.import.title",
                  -7811960,
                  "buildcraft.help.zone_planner.import.desc1",
                  "buildcraft.help.zone_planner.import.desc2"
               )
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(44.0, 133.0, 28.0, 9.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.zone_planner.import_progress.title", -2249985, "buildcraft.help.zone_planner.import_progress.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(74.0, 130.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.zone_planner.import_result.title", -10665929, "buildcraft.help.zone_planner.import_result.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(233.0, 18.0, 16.0, 34.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.zone_planner.export.title",
                  -7811960,
                  "buildcraft.help.zone_planner.export.desc1",
                  "buildcraft.help.zone_planner.export.desc2"
               )
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(236.0, 54.0, 9.0, 28.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.zone_planner.export_progress.title", -2249985, "buildcraft.help.zone_planner.export_progress.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(233.0, 84.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.zone_planner.export_result.title", -10665929, "buildcraft.help.zone_planner.export_result.desc")
            )
         );
   }
}
