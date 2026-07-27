/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.lib.gui.ledger.LedgerHelp;
import buildcraft.lib.gui.ledger.Ledger_Neptune;
import buildcraft.lib.gui.pos.IGuiArea;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
//? if >= 1.21.10 {
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public abstract class BcScreen<C extends net.minecraft.world.inventory.AbstractContainerMenu & IBcMenu> extends AbstractContainerScreen<C> {
   public final BuildCraftGui mainGui;

   // Vanilla player-inventory texture, sliced like the vanilla chest screens do. One tile-able row of 9 slots,
   // and the "gap + hotbar + bottom frame" block; both include the panel's side frames. Reused so a machine GUI
   // only ships its own machine header and the whole player inventory comes from vanilla pixels (and any mod
   // that extends the inventory composes for free).
   // Exactly the texture and regions vanilla's ContainerScreen.extractBackground uses to draw the player
   // inventory: generic_54.png, the (0,126,176,96) block. INV_ROW is one 9-slot row of it (tex y139, tileable at
   // 18px); INV_BOTTOM is the rest of that block (gap + hotbar + bottom frame, tex y192..221).
   private static final Identifier CONTAINER_BG = Identifier.parse("minecraft:textures/gui/container/generic_54.png");
   private static final GuiIcon INV_ROW = new GuiIcon(CONTAINER_BG, 0.0, 139.0, 176.0, 18.0);
   private static final GuiIcon INV_BOTTOM = new GuiIcon(CONTAINER_BG, 0.0, 192.0, 176.0, 30.0);

   public int getGuiLeftPos() {
      return this.leftPos;
   }

   public int getGuiTopPos() {
      return this.topPos;
   }

   public int getGuiImageWidth() {
      return this.imageWidth;
   }

   public int getGuiImageHeight() {
      return this.imageHeight;
   }

   /**
    * Height (px) needed to fit every slot of {@code menu}, never smaller than {@code defaultHeight}. Lets a GUI
    * grow when a mod extends the player inventory (extra rows) so the added slots stay on the window. Uses the
    * vanilla 6px bottom padding, so an un-extended inventory keeps the original size exactly (no regression).
    */
   protected static int heightForSlots(net.minecraft.world.inventory.AbstractContainerMenu menu, int defaultHeight) {
      int maxY = -1;
      for (net.minecraft.world.inventory.Slot slot : menu.slots) {
         if (slot.container instanceof Inventory) {
            maxY = Math.max(maxY, slot.y);
         }
      }

      return maxY < 0 ? defaultHeight : Math.max(defaultHeight, maxY + 18 + 6);
   }

   /**
    * When a mod has extended the player inventory beyond its vanilla 4 rows (3 main + hotbar), draws the vanilla
    * inventory panel from generic_54.png so the added rows get a proper background. Otherwise draws nothing: the
    * machine's own GUI texture already bakes a correct, pixel-perfect player inventory, so we leave it untouched
    * (no seams, no double-drawing). Only the extended case -- which the baked art can't cover -- is rendered here.
    */
   protected void drawPlayerInventoryBackground() {
      int top = (int) this.mainGui.rootElement.getY();
      java.util.SortedSet<Integer> rows = new java.util.TreeSet<>();
      int minX = Integer.MAX_VALUE;
      for (Slot slot : this.menu.slots) {
         if (slot.container instanceof Inventory) {
            rows.add(slot.y);
            minX = Math.min(minX, slot.x);
         }
      }

      // 4 rows == a stock player inventory the machine texture already draws correctly. Nothing to add.
      if (rows.size() <= 4) {
         return;
      }

      // Extended: the baked art can't match the moved hotbar / extra rows, so redraw the whole player inventory
      // from vanilla generic_54.png. One tile-able 9-slot band per main row, then the gap+hotbar+bottom block for
      // the lowest (hotbar) row, shifted to wherever this container's inventory starts.
      int left = (int) this.mainGui.rootElement.getX() + minX - 8;
      int hotbarY = rows.last();
      for (int rowY : rows) {
         if (rowY == hotbarY) {
            INV_BOTTOM.drawAt(left, top + rowY - 6);
         } else {
            INV_ROW.drawAt(left, top + rowY - 1);
         }
      }

      // The generic 176-wide vanilla panel above may not match a GUI whose inventory has custom chrome beside it
      // (a side box, a divider, a non-standard frame). Only reached in the extended case, so a GUI can patch those
      // regions with slices of its own texture without ever touching the pixel-perfect un-extended look.
      this.drawExtendedInventoryChrome();
   }

   /**
    * Called after the vanilla inventory panel has been drawn for a mod-extended player inventory. Override in a GUI
    * whose inventory area carries custom frames the generic panel can't reproduce, to blit those regions from the
    * GUI's own texture back on top (e.g. a side divider, a box edge, a bottom frame). Position slices relative to
    * {@link #firstPlayerRowY()} / the actual slot rows so they follow the inventory as the mod grows it. No-op by
    * default; never runs for an un-extended inventory (nothing is overdrawn there).
    */
   protected void drawExtendedInventoryChrome() {
   }

   /** Container-relative Y of the topmost player-inventory row, or -1 if this menu has no player inventory. */
   protected int firstPlayerRowY() {
      int minY = Integer.MAX_VALUE;
      for (Slot slot : this.menu.slots) {
         if (slot.container instanceof Inventory) {
            minY = Math.min(minY, slot.y);
         }
      }

      return minY == Integer.MAX_VALUE ? -1 : minY;
   }

   /** Y for a screen's "Inventory" label: just above the first player-inventory row, independent of imageHeight
    * (which grows when a mod extends the inventory). Returns -1 if this menu has no player inventory. */
   protected int playerInventoryLabelY() {
      int minY = this.firstPlayerRowY();
      return minY < 0 ? -1 : minY - 12;
   }

   protected BcScreen(C container, Inventory playerInventory, Component title) {
      super(container, playerInventory, title);
      IGuiArea rootArea = BuildCraftGui.createWindowedArea(this);
      this.mainGui = new BuildCraftGui(this, rootArea);
   }

   protected BcScreen(C container, Inventory playerInventory, Component title, int xSize, int ySize) {
      //? if >= 26.1 {
      super(container, playerInventory, title, xSize, ySize);
      //?} else {
      /*super(container, playerInventory, title);
      this.imageWidth = xSize;
      this.imageHeight = ySize;
      *///?}
      IGuiArea rootArea = BuildCraftGui.createWindowedArea(this);
      this.mainGui = new BuildCraftGui(this, rootArea);
   }

   protected abstract void initGuiElements();

   protected boolean shouldAddHelpLedger() {
      return true;
   }

   protected void init() {
      super.init();
      Map<String, Ledger_Neptune> oldLedgers = new LinkedHashMap<>();

      for (IGuiElement elem : this.mainGui.shownElements) {
         if (elem instanceof Ledger_Neptune ledger) {
            oldLedgers.put(elem.getClass().getName(), ledger);
         }
      }

      IGuiArea rootArea = BuildCraftGui.createWindowedArea(this);
      this.mainGui.lowerLeftLedgerPos = rootArea.offset(0.0, 5.0);
      this.mainGui.lowerRightLedgerPos = rootArea.getPosition(1, -1).offset(0.0, 5.0);
      this.mainGui.shownElements.clear();
      this.initGuiElements();
      if (this.shouldAddHelpLedger()) {
         this.mainGui.shownElements.add(new LedgerHelp(this.mainGui, false));
      }

      if (!oldLedgers.isEmpty()) {
         for (IGuiElement elem : this.mainGui.shownElements) {
            if (elem instanceof Ledger_Neptune ledger) {
               Ledger_Neptune oldLedger = oldLedgers.get(elem.getClass().getName());
               if (oldLedger != null) {
                  ledger.copyAnimationStateFrom(oldLedger);
               }
            }
         }
      }
   }

   protected void containerTick() {
      super.containerTick();
      this.mainGui.tick();
   }

   //? if >= 26.1 {
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      super.extractBackground(graphics, mouseX, mouseY, partialTicks);
      BCGraphics bcg = new BCGraphics(graphics);
      GuiIcon.setGuiGraphics(bcg);
      this.mainGui.drawBackgroundLayer(partialTicks, mouseX, mouseY, () -> {
         this.drawBackgroundTexture(bcg);
         this.drawPlayerInventoryBackground();
      });
      this.mainGui.drawElementBackgrounds();
   }

   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      BCGraphics bcg = new BCGraphics(graphics);
      GuiIcon.setGuiGraphics(bcg);
      super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
      bcg.nextStratum();
      this.mainGui.drawDragLayer(bcg);
      this.mainGui.drawMenuOverlayLayer(bcg);
      this.drawTooltipLayer(mouseX, mouseY, partialTicks);
   }
   //?} else {
   /*// 1.21.x uses immediate-mode screen rendering: renderBg draws the GUI texture/background layer,
   // and the drag/menu-overlay/tooltip passes run in render() after the vanilla container render.
   @Override
   protected void renderBg(GuiGraphicsExtractor graphics, float partialTicks, int mouseX, int mouseY) {
      BCGraphics bcg = new BCGraphics(graphics);
      GuiIcon.setGuiGraphics(bcg);
      this.mainGui.drawBackgroundLayer(partialTicks, mouseX, mouseY, () -> {
         this.drawBackgroundTexture(bcg);
         this.drawPlayerInventoryBackground();
      });
      this.mainGui.drawElementBackgrounds();
   }

   @Override
   public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      super.render(graphics, mouseX, mouseY, partialTicks);
      BCGraphics bcg = new BCGraphics(graphics);
      GuiIcon.setGuiGraphics(bcg);
      bcg.nextStratum();
      this.mainGui.drawDragLayer(bcg);
      this.mainGui.drawMenuOverlayLayer(bcg);
      this.drawTooltipLayer(mouseX, mouseY, partialTicks);
   }
   *///?}

   //? if >= 1.21.10 {
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      int mouseX = (int)event.x();
      int mouseY = (int)event.y();
      int button = event.button();
      return this.mainGui.onMouseClicked(mouseX, mouseY, button) ? true : super.mouseClicked(event, doubleClick);
   }

   public boolean mouseReleased(MouseButtonEvent event) {
      int mouseX = (int)event.x();
      int mouseY = (int)event.y();
      int button = event.button();
      this.mainGui.onMouseReleased(mouseX, mouseY, button);
      return super.mouseReleased(event);
   }

   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      int mouseX = (int)event.x();
      int mouseY = (int)event.y();
      int button = event.button();
      this.mainGui.onMouseDragged(mouseX, mouseY, button, 0L);
      return super.mouseDragged(event, dragX, dragY);
   }
   //?} else {
   /*public boolean mouseClicked(double mouseXd, double mouseYd, int button) {
      int mouseX = (int)mouseXd;
      int mouseY = (int)mouseYd;
      return this.mainGui.onMouseClicked(mouseX, mouseY, button) ? true : super.mouseClicked(mouseXd, mouseYd, button);
   }

   public boolean mouseReleased(double mouseXd, double mouseYd, int button) {
      int mouseX = (int)mouseXd;
      int mouseY = (int)mouseYd;
      this.mainGui.onMouseReleased(mouseX, mouseY, button);
      return super.mouseReleased(mouseXd, mouseYd, button);
   }

   public boolean mouseDragged(double mouseXd, double mouseYd, int button, double dragX, double dragY) {
      int mouseX = (int)mouseXd;
      int mouseY = (int)mouseYd;
      this.mainGui.onMouseDragged(mouseX, mouseY, button, 0L);
      return super.mouseDragged(mouseXd, mouseYd, button, dragX, dragY);
   }
   *///?}

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      return this.mainGui.onMouseScroll((int)mouseX, (int)mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
   }

   //? if >= 26.1 {
   protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      BCGraphics bcg = new BCGraphics(graphics);
      GuiIcon.setGuiGraphics(bcg);
      this.mainGui.preDrawForeground();
      this.mainGui.drawElementForegrounds(null);
      this.mainGui.postDrawForeground();
      this.drawForegroundLayer();
   }
   //?} else {
   /*@Override
   protected void renderLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      BCGraphics bcg = new BCGraphics(graphics);
      GuiIcon.setGuiGraphics(bcg);
      this.mainGui.preDrawForeground();
      this.mainGui.drawElementForegrounds(null);
      this.mainGui.postDrawForeground();
      this.drawForegroundLayer();
   }
   *///?}

   protected void drawForegroundLayer() {
   }

   protected void drawBackgroundTexture(BCGraphics graphics) {
   }

   protected void drawTooltipLayer(int mouseX, int mouseY, float partialTick) {
   }
}
