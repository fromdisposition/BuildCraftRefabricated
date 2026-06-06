package buildcraft.robotics.gui;

import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.robotics.container.ContainerZonePlanner;
import buildcraft.robotics.tile.TileZonePlanner;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class GuiZonePlanner extends BcScreen<ContainerZonePlanner> {
   private static final int SIZE_X = 256;
   private static final int SIZE_Y = 228;
   private static final int MAP_X = 17;
   private static final int MAP_Y = 17;
   private static final int MAP_W = 213;
   private static final int MAP_H = 117;
   private static final int BAR_X = 17;
   private static final int BAR_Y = 137;
   private static final int PANEL_BG = -3750202;
   private static final int PANEL_BORDER = -13158601;
   private static final int SLOT_BG = -7631989;
   private static final int SLOT_BORDER = -13158601;
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
      int x = this.getGuiLeftPos();
      int y = this.getGuiTopPos();
      graphics.fill(x, y, x + 256, y + 228, -3750202);
      graphics.fill(x, y, x + 256, y + 1, -13158601);
      graphics.fill(x, y + 228 - 1, x + 256, y + 228, -13158601);
      graphics.fill(x, y, x + 1, y + 228, -13158601);
      graphics.fill(x + 256 - 1, y, x + 256, y + 228, -13158601);

      for (Slot slot : ((ContainerZonePlanner)this.getMenu()).slots) {
         int sx = x + slot.x;
         int sy = y + slot.y;
         graphics.fill(sx - 1, sy - 1, sx + 17, sy + 17, -13158601);
         graphics.fill(sx, sy, sx + 16, sy + 16, -7631989);
      }
   }

   public boolean keyPressed(KeyEvent event) {
      return this.mainGui.onKeyTyped('\u0000', event.key()) ? true : super.keyPressed(event);
   }

   @Override
   protected void initGuiElements() {
      TileZonePlanner tile = ((ContainerZonePlanner)this.getMenu()).tile;
      this.mainGui.shownElements.add(new ZonePlannerMapElement(this, tile, 17, 17, 213, 117, 17, 137));
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(17.0, 17.0, 213.0, 117.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.zone_planner.map.title", -7811960, "buildcraft.help.zone_planner.map.desc1", "buildcraft.help.zone_planner.map.desc2"
               )
            )
         );
   }
}
