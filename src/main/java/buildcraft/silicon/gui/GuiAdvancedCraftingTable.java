/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gui;

import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.silicon.container.ContainerAdvancedCraftingTable;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiAdvancedCraftingTable extends BcScreen<ContainerAdvancedCraftingTable> {
   private static final Identifier TEXTURE_BASE = Identifier.parse("buildcraftsilicon:textures/gui/advanced_crafting_table.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0.0, 0.0, 176.0, 241.0);
   private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, 176.0, 0.0, 4.0, 70.0);
   private ACTRecipeBookComponent recipeBookComponent;
   private ImageButton recipeBookButton;
   private boolean widthTooNarrow;

   public GuiAdvancedCraftingTable(ContainerAdvancedCraftingTable container, Inventory playerInventory, Component title) {
      super(container, playerInventory, title, 176, 241);
   }

   @Override
   protected boolean shouldAddHelpLedger() {
      return false;
   }

   @Override
   protected void initGuiElements() {
      if (((ContainerAdvancedCraftingTable)this.menu).tile != null) {
         this.mainGui
            .shownElements
            .add(
               new LedgerOwnership(
                  this.mainGui,
                  () -> ((ContainerAdvancedCraftingTable)this.menu).tile != null ? ((ContainerAdvancedCraftingTable)this.menu).tile.getOwner() : null,
                  true
               )
            );
      }

      this.mainGui.shownElements.add(new LedgerTablePower(this.mainGui, ((ContainerAdvancedCraftingTable)this.menu).tile, true));
   }

   @Override
   protected void init() {
      super.init();
      this.widthTooNarrow = this.width < 379;
      this.recipeBookComponent = new ACTRecipeBookComponent((ContainerAdvancedCraftingTable)this.menu);
      this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow);
      this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
      ScreenPosition buttonPos = this.getRecipeBookButtonPosition();
      this.recipeBookButton = new ImageButton(buttonPos.x(), buttonPos.y(), 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, p -> {
         this.recipeBookComponent.toggleVisibility();
         this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
         ScreenPosition newPos = this.getRecipeBookButtonPosition();
         this.recipeBookButton.setPosition(newPos.x(), newPos.y());
      });
      this.addRenderableWidget(this.recipeBookButton);
      this.addRenderableWidget(this.recipeBookComponent);
   }

   private ScreenPosition getRecipeBookButtonPosition() {
      return new ScreenPosition(this.leftPos + 7, this.topPos + 33);
   }

   @Override
   protected void containerTick() {
      super.containerTick();
      if (this.recipeBookComponent != null) {
         this.recipeBookComponent.tick();
      }
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
      long target = ((ContainerAdvancedCraftingTable)this.menu).tile.getTarget();
      if (target != 0L) {
         double v = (double)((ContainerAdvancedCraftingTable)this.menu).tile.power / target;
         ICON_PROGRESS.drawCutInside(
            new GuiRectangle(
                  164.0,
                  (int)(7.0 + 70.0 * Math.max(1.0 - v, 0.0)),
                  4.0,
                  (int)Math.ceil(70.0 * Math.min(v, 1.0))
               )
               .offset(this.mainGui.rootElement)
         );
      }
   }

   @Override
   protected void drawTooltipLayer(int mouseX, int mouseY, float partialTick) {
      if (this.recipeBookComponent != null && this.recipeBookComponent.isVisible()) {
         BCGraphics graphics = GuiIcon.getGuiGraphics();
         this.recipeBookComponent.extractTooltip(graphics.raw, mouseX, mouseY, this.hoveredSlot);
      }
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      String title = I18n.get("block.buildcraftsilicon.advanced_crafting_table", new Object[0]);
      graphics.text(this.font, title, (this.imageWidth - this.font.width(title)) / 2, 5, -12566464, false);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean entered) {
      if (this.recipeBookComponent != null && this.recipeBookComponent.mouseClicked(event, entered)) {
         this.setFocused(this.recipeBookComponent);
         return true;
      } else {
         return super.mouseClicked(event, entered);
      }
   }

   protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
      boolean outside = mouseX < left || mouseY < top || mouseX >= left + this.imageWidth || mouseY >= top + this.imageHeight;
      return this.recipeBookComponent != null
         ? this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight) && outside
         : outside;
   }

   public void recipesUpdated() {
      if (this.recipeBookComponent != null) {
         this.recipeBookComponent.recipesUpdated();
      }
   }

   public boolean keyPressed(KeyEvent event) {
      return this.recipeBookComponent != null && this.recipeBookComponent.keyPressed(event) ? true : super.keyPressed(event);
   }

   protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
      return super.isHovering(x, y, width, height, mouseX, mouseY);
   }
}
