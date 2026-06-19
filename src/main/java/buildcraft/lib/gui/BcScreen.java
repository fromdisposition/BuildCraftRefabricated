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
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class BcScreen<C extends net.minecraft.world.inventory.AbstractContainerMenu & IBcMenu> extends AbstractContainerScreen<C> {
   public final BuildCraftGui mainGui;

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
      this.mainGui.drawBackgroundLayer(partialTicks, mouseX, mouseY, () -> this.drawBackgroundTexture(bcg));
      this.mainGui.drawElementBackgrounds();
   }

   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      BCGraphics bcg = new BCGraphics(graphics);
      GuiIcon.setGuiGraphics(bcg);
      super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
      graphics.nextStratum();
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
      this.mainGui.drawBackgroundLayer(partialTicks, mouseX, mouseY, () -> this.drawBackgroundTexture(bcg));
      this.mainGui.drawElementBackgrounds();
   }

   @Override
   public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      super.render(graphics, mouseX, mouseY, partialTicks);
      BCGraphics bcg = new BCGraphics(graphics);
      GuiIcon.setGuiGraphics(bcg);
      graphics.nextStratum();
      this.mainGui.drawDragLayer(bcg);
      this.mainGui.drawMenuOverlayLayer(bcg);
      this.drawTooltipLayer(mouseX, mouseY, partialTicks);
   }
   *///?}

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
