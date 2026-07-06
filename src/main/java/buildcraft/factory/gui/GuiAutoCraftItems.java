/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.gui;

import buildcraft.factory.container.ContainerAutoCraftItems;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.ItemHandlerSimple;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
//? if >= 1.21.10 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiAutoCraftItems extends BcScreen<ContainerAutoCraftItems> {
   private static final Identifier TEXTURE = Identifier.parse("buildcraftfactory:textures/gui/autobench_item.png");
   private static final Identifier TEXTURE_MISC = Identifier.parse("buildcraftlib:textures/gui/misc_slots.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0.0, 0.0, 176.0, 197.0);
   private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE, 176.0, 0.0, 23.0, 10.0);
   private static final GuiIcon ICON_FILTER_OVERLAY_SAME = new GuiIcon(TEXTURE_MISC, 54.0, 0.0, 18.0, 18.0);
   private static final GuiIcon ICON_FILTER_OVERLAY_DIFFERENT = new GuiIcon(TEXTURE_MISC, 72.0, 0.0, 18.0, 18.0);
   private AWRecipeBookComponent recipeBookComponent;
   private ImageButton recipeBookButton;
   private boolean widthTooNarrow;

   public GuiAutoCraftItems(ContainerAutoCraftItems menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title, 176, heightForSlots(menu, 197));
   }

   @Override
   protected boolean shouldAddHelpLedger() {
      return false;
   }

   @Override
   protected void initGuiElements() {
      if (((ContainerAutoCraftItems)this.menu).tile != null) {
         this.mainGui
            .shownElements
            .add(
               new LedgerOwnership(
                  this.mainGui, () -> ((ContainerAutoCraftItems)this.menu).tile != null ? ((ContainerAutoCraftItems)this.menu).tile.getOwner() : null, true
               )
            );
      }
   }

   @Override
   protected void init() {
      super.init();
      this.widthTooNarrow = this.width < 379;
      //? if >= 1.21.10 {
      this.recipeBookComponent = new AWRecipeBookComponent((ContainerAutoCraftItems)this.menu);
      this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow);
      //?} else {
      /*// 1.21.1: AWRecipeBookComponent is a thin alias of the concrete vanilla RecipeBookComponent;
      // instantiate no-arg and pass the menu to the 5-arg init.
      this.recipeBookComponent = new AWRecipeBookComponent();
      this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, (ContainerAutoCraftItems)this.menu);
      *///?}
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
      return new ScreenPosition(this.leftPos + 5, this.topPos + 34);
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
      if (((ContainerAutoCraftItems)this.menu).tile != null) {
         double progress = ((ContainerAutoCraftItems)this.menu).tile.getProgress(0.0F);
         if (progress > 0.0) {
            int progressWidth = (int)(ICON_PROGRESS.width * Math.min(progress, 1.0));
            if (progressWidth > 0) {
               int px = 90 + (int)this.mainGui.rootElement.getX();
               int py = 47 + (int)this.mainGui.rootElement.getY();
               ICON_PROGRESS.drawCutInside(px, py, progressWidth, ICON_PROGRESS.height);
            }
         }
      }

      if (this.hasFilters()) {
         ItemHandlerSimple filters = ((ContainerAutoCraftItems)this.menu).tile.invMaterialFilter;

         for (int s = 0; s < filters.getSlots(); s++) {
            ItemStack filterStack = filters.getStackInSlot(s);
            if (!filterStack.isEmpty()) {
               SlotBase slot = ((ContainerAutoCraftItems)this.menu).materialSlots[s];
               int x = slot.x + (int)this.mainGui.rootElement.getX();
               int y = slot.y + (int)this.mainGui.rootElement.getY();
               graphics.fakeItem(filterStack, x, y);
               graphics.itemDecorations(this.font, filterStack, x, y, null);
            }
         }

         for (int s = 0; s < filters.getSlots(); s++) {
            ItemStack filterStack = filters.getStackInSlot(s);
            if (!filterStack.isEmpty()) {
               SlotBase slot = ((ContainerAutoCraftItems)this.menu).materialSlots[s];
               ItemStack real = slot.getItem();
               GuiIcon icon;
               if (!real.isEmpty() && !StackUtil.canMerge(real, filterStack)) {
                  icon = ICON_FILTER_OVERLAY_DIFFERENT;
               } else {
                  icon = ICON_FILTER_OVERLAY_SAME;
               }

               int x = slot.x + (int)this.mainGui.rootElement.getX() - 1;
               int y = slot.y + (int)this.mainGui.rootElement.getY() - 1;
               icon.drawAt(x, y);
            }
         }
      }
   }

   private boolean hasFilters() {
      if (((ContainerAutoCraftItems)this.menu).tile == null) {
         return false;
      }

      ItemHandlerSimple filters = ((ContainerAutoCraftItems)this.menu).tile.invMaterialFilter;

      for (int s = 0; s < filters.getSlots(); s++) {
         if (!filters.getStackInSlot(s).isEmpty()) {
            return true;
         }
      }

      return false;
   }

   @Override
   protected void drawTooltipLayer(int mouseX, int mouseY, float partialTick) {
      if (this.recipeBookComponent != null && this.recipeBookComponent.isVisible()) {
         BCGraphics graphics = GuiIcon.getGuiGraphics();
         //? if >= 1.21.10 {
         //? if >= 26.1 {
         this.recipeBookComponent.extractTooltip(graphics.raw, mouseX, mouseY, this.hoveredSlot);
         //?} else {
         /*this.recipeBookComponent.renderTooltip(graphics.raw, mouseX, mouseY, this.hoveredSlot);
         *///?}
         //?} else {
         /*// 1.21.1: RecipeBookComponent.renderTooltip(graphics, leftPos, topPos, mouseX, mouseY).
         this.recipeBookComponent.renderTooltip(graphics.raw, this.leftPos, this.topPos, mouseX, mouseY);
         *///?}
      }
   }

   //? if >= 1.21.10 {
   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean entered) {
      if (this.recipeBookComponent != null && this.recipeBookComponent.mouseClicked(event, entered)) {
         this.setFocused(this.recipeBookComponent);
         return true;
      } else {
         return super.mouseClicked(event, entered);
      }
   }
   //?} else {
   /*@Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.recipeBookComponent != null && this.recipeBookComponent.mouseClicked(mouseX, mouseY, button)) {
         this.setFocused(this.recipeBookComponent);
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }
   *///?}

   protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
      boolean outside = mouseX < left || mouseY < top || mouseX >= left + this.imageWidth || mouseY >= top + this.imageHeight;
      //? if >= 1.21.10 {
      return this.recipeBookComponent != null
         ? this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight) && outside
         : outside;
      //?} else {
      /*// 1.21.1 hasClickedOutside takes the mouse button as a 7th arg.
      return this.recipeBookComponent != null
         ? this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, button) && outside
         : outside;
      *///?}
   }

   public void recipesUpdated() {
      if (this.recipeBookComponent != null) {
         this.recipeBookComponent.recipesUpdated();
      }
   }

   //? if >= 1.21.10 {
   public boolean keyPressed(KeyEvent event) {
      return this.recipeBookComponent != null && this.recipeBookComponent.keyPressed(event) ? true : super.keyPressed(event);
   }
   //?} else {
   /*public boolean keyPressed(int key, int scancode, int modifiers) {
      return this.recipeBookComponent != null && this.recipeBookComponent.keyPressed(key, scancode, modifiers) ? true : super.keyPressed(key, scancode, modifiers);
   }
   *///?}

   protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
      return super.isHovering(x, y, width, height, mouseX, mouseY);
   }
}
