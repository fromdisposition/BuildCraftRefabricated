/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.lib.misc.GuiUtil;
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

   // Sliced from vanilla generic_54.png like ContainerScreen.extractBackground: INV_ROW is one tileable 9-slot row
   // (tex y139, 18px), INV_BOTTOM is gap+hotbar+bottom (tex y192-221) -- reused so machine GUIs ship no inventory art.
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

   /** Height needed to fit every slot of {@code menu}; grows for a mod-extended inventory (extra rows) using the
    * vanilla 6px bottom padding, so an un-extended inventory keeps its original size exactly. */
   protected static int heightForSlots(net.minecraft.world.inventory.AbstractContainerMenu menu, int defaultHeight) {
      int maxY = -1;
      for (net.minecraft.world.inventory.Slot slot : menu.slots) {
         if (slot.container instanceof Inventory) {
            maxY = Math.max(maxY, slot.y);
         }
      }

      return maxY < 0 ? defaultHeight : Math.max(defaultHeight, maxY + 18 + 6);
   }

   /** Draws the vanilla inventory panel from generic_54.png only when a mod has extended the player inventory past
    * 4 rows; an un-extended inventory already has a pixel-perfect background baked into the machine's own texture. */
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

      // Baked art can't match moved/extra rows, so redraw the whole player inventory: one tileable 9-slot band per
      // main row, then the gap+hotbar+bottom block for the hotbar row.
      int left = (int) this.mainGui.rootElement.getX() + minX - 8;
      int hotbarY = rows.last();
      for (int rowY : rows) {
         if (rowY == hotbarY) {
            INV_BOTTOM.drawAt(left, top + rowY - 6);
         } else {
            INV_ROW.drawAt(left, top + rowY - 1);
         }
      }

      // The generic 176-wide panel may not match custom chrome beside the inventory (a side box, divider, frame);
      // only reached in the extended case, so a GUI can patch those regions without touching the un-extended look.
      this.drawExtendedInventoryChrome();
   }

   /** Override to blit custom frames back over the generic panel for a mod-extended inventory (e.g. a side divider);
    * position relative to {@link #firstPlayerRowY()} so slices follow the inventory as it grows. No-op by default. */
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
      int button = GuiUtil.classicButton(event.button());
      return this.mainGui.onMouseClicked(mouseX, mouseY, button) ? true : super.mouseClicked(event, doubleClick);
   }

   public boolean mouseReleased(MouseButtonEvent event) {
      int mouseX = (int)event.x();
      int mouseY = (int)event.y();
      int button = GuiUtil.classicButton(event.button());
      this.mainGui.onMouseReleased(mouseX, mouseY, button);
      return super.mouseReleased(event);
   }

   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      int mouseX = (int)event.x();
      int mouseY = (int)event.y();
      int button = GuiUtil.classicButton(event.button());
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
