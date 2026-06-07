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
   private static final int SIZE_X = 256;
   private static final int SIZE_Y = 228;
   private static final int MAP_X = 8;
   private static final int MAP_Y = 9;
   private static final int MAP_W = 213;
   private static final int MAP_H = 100;
   private static final int PROGRESS_INPUT_X = 44;
   private static final int PROGRESS_INPUT_Y = 128;
   private static final int PROGRESS_INPUT_W = 28;
   private static final int PROGRESS_INPUT_H = 9;
   private static final int PROGRESS_OUTPUT_X = 236;
   private static final int PROGRESS_OUTPUT_Y = 45;
   private static final int PROGRESS_OUTPUT_W = 9;
   private static final int PROGRESS_OUTPUT_H = 28;
   private static final int PROGRESS_TARGET = 200;
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
            ICON_PROGRESS_INPUT.drawCutInside(left + PROGRESS_INPUT_X, top + PROGRESS_INPUT_Y, PROGRESS_INPUT_W * fracIn, PROGRESS_INPUT_H);
         }

         if (fracOut > 0.0) {
            ICON_PROGRESS_OUTPUT.drawCutInside(left + PROGRESS_OUTPUT_X, top + PROGRESS_OUTPUT_Y, PROGRESS_OUTPUT_W, PROGRESS_OUTPUT_H * fracOut);
         }
      }
   }

   private static double progressFraction(int progress) {
      return progress < 0 ? 0.0 : Math.min(1.0, progress / (double)PROGRESS_TARGET);
   }

   public boolean keyPressed(KeyEvent event) {
      return this.mainGui.onKeyTyped('\u0000', event.key()) ? true : super.keyPressed(event);
   }

   @Override
   protected void initGuiElements() {
      TileZonePlanner tile = ((ContainerZonePlanner)this.getMenu()).tile;
      this.mainGui.shownElements.add(new ZonePlannerMapElement(this, tile, MAP_X, MAP_Y, MAP_W, MAP_H));
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle((double)MAP_X, (double)MAP_Y, (double)MAP_W, (double)MAP_H).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.zone_planner.map.title", -7811960, "buildcraft.help.zone_planner.map.desc1", "buildcraft.help.zone_planner.map.desc2"
               )
            )
         );
   }
}
