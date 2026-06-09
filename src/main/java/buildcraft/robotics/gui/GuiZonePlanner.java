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
import buildcraft.robotics.container.ContainerZonePlanner;
import buildcraft.robotics.tile.TileZonePlanner;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiZonePlanner extends BcScreen<ContainerZonePlanner> {
   private static final Identifier TEXTURE = Identifier.parse("buildcraftrobotics:textures/gui/zone_planner.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0.0, 0.0, 256.0, 228.0);
   private static final GuiIcon ICON_PROGRESS_INPUT = new GuiIcon(TEXTURE, 9.0, 228.0, 28.0, 9.0);
   private static final GuiIcon ICON_PROGRESS_OUTPUT = new GuiIcon(TEXTURE, 0.0, 228.0, 9.0, 28.0);
   private boolean requestedLayers = false;

   public GuiZonePlanner(ContainerZonePlanner menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title, 256, 228);
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
            ICON_PROGRESS_INPUT.drawCutInside(left + 44, top + 128, 28.0 * fracIn, 9.0);
         }

         if (fracOut > 0.0) {
            ICON_PROGRESS_OUTPUT.drawCutInside(left + 236, top + 45, 9.0, 28.0 * fracOut);
         }
      }
   }

   private static double progressFraction(int progress) {
      return progress < 0 ? 0.0 : Math.min(1.0, progress / 200.0);
   }

   public boolean keyPressed(KeyEvent event) {
      return this.mainGui.onKeyTyped('\u0000', event.key()) ? true : super.keyPressed(event);
   }

   @Override
   protected void initGuiElements() {
      TileZonePlanner tile = ((ContainerZonePlanner)this.getMenu()).tile;
      this.mainGui.shownElements.add(new ZonePlannerMapElement(this, tile, 8, 9, 213, 100));
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(8.0, 9.0, 213.0, 100.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.zone_planner.map.title", -7811960, "buildcraft.help.zone_planner.map.desc1", "buildcraft.help.zone_planner.map.desc2"
               )
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(8.0, 146.0, 70.0, 70.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.zone_planner.paintbrushes.title", -13176, "buildcraft.help.zone_planner.paintbrushes.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(8.0, 125.0, 36.0, 16.0).offset(this.mainGui.rootElement),
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
               new GuiRectangle(44.0, 128.0, 28.0, 9.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.zone_planner.import_progress.title", -2249985, "buildcraft.help.zone_planner.import_progress.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(74.0, 125.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.zone_planner.import_result.title", -10665929, "buildcraft.help.zone_planner.import_result.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(233.0, 9.0, 16.0, 34.0).offset(this.mainGui.rootElement),
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
               new GuiRectangle(236.0, 45.0, 9.0, 28.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.zone_planner.export_progress.title", -2249985, "buildcraft.help.zone_planner.export_progress.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(233.0, 75.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.zone_planner.export_result.title", -10665929, "buildcraft.help.zone_planner.export_result.desc")
            )
         );
   }
}
