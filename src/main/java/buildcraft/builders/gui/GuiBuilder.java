/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.gui;

import buildcraft.builders.container.ContainerBuilder;
import buildcraft.builders.snapshot.EnumContainerContentsMode;
import buildcraft.builders.snapshot.EnumFluidHandlingMode;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.button.BCButton;
import buildcraft.lib.gui.elem.GuiElementFluidTank;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.widget.WidgetFluidTank;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
//? if >= 1.21.10 {
import net.minecraft.client.input.InputWithModifiers;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GuiBuilder extends BcScreen<ContainerBuilder> {
   private static final Identifier TEXTURE_BASE = Identifier.parse("buildcraftbuilders:textures/gui/bcr/builder.png");
   private static final Identifier TEXTURE_BLUEPRINT = Identifier.parse("buildcraftbuilders:textures/gui/bcr/builder_blueprint.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0.0, 0.0, 176.0, 222.0);
   private static final GuiIcon ICON_BLUEPRINT_GUI = new GuiIcon(TEXTURE_BLUEPRINT, 169.0, 0.0, 87.0, 222.0);
   // Box left divider (x=169..178), down to just above its solid bottom frame (tex y=214). Restores the box's
   // clean vertical edge beside the tanks/slots when a mod-extended inventory has drawn the generic panel over it.
   private static final GuiIcon ICON_BLUEPRINT_EDGE = new GuiIcon(TEXTURE_BLUEPRINT, 169.0, 0.0, 10.0, 214.0);
   // The box's bottom-left corner (tex x=173..178, y=214..220): the frame band + shadow that close the divider into
   // the horizontal bottom frame. Blitted narrow (from x=173) so it covers the generic panel's dark groove/black
   // edge without pushing the box's shadow onto x=169..172, where the divider stays light -- i.e. a clean L-corner.
   private static final GuiIcon ICON_BLUEPRINT_CORNER = new GuiIcon(TEXTURE_BLUEPRINT, 173.0, 214.0, 6.0, 7.0);
   private static final GuiIcon ICON_TANK_OVERLAY = new GuiIcon(TEXTURE_BLUEPRINT, 0.0, 54.0, 16.0, 47.0);
   private GuiBuilder.FluidModeButton fluidModeButton;
   private GuiBuilder.ContentsModeButton contentsModeButton;

   public GuiBuilder(ContainerBuilder container, Inventory playerInv, Component title) {
      super(container, playerInv, title, 256, heightForSlots(container, 222));
      this.inventoryLabelY = this.imageHeight - 94;
   }

   @Override
   protected void initGuiElements() {
      if (((ContainerBuilder)this.menu).tile != null) {
         for (int i = 0; i < 4; i++) {
            int idx = i;
            WidgetFluidTank widget = idx < ((ContainerBuilder)this.menu).widgetTanks.size() ? ((ContainerBuilder)this.menu).widgetTanks.get(idx) : null;
            this.mainGui
               .shownElements
               .add(
                  new GuiElementFluidTank(
                     this.mainGui,
                     new GuiRectangle(179 + idx * 18, 145.0, 16.0, 47.0).offset(this.mainGui.rootElement),
                     widget != null ? widget.getTankStorage() : null,
                     widget,
                     ICON_TANK_OVERLAY
                  )
               );
         }

         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(80.0, 27.0, 16.0, 16.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo(
                     "buildcraft.help.builder.snapshot.title", -7811960, "buildcraft.help.builder.snapshot.desc1", "buildcraft.help.builder.snapshot.desc2"
                  )
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(8.0, 72.0, 160.0, 52.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo(
                     "buildcraft.help.builder.resources.title", -13176, "buildcraft.help.builder.resources.desc1", "buildcraft.help.builder.resources.desc2"
                  )
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(150.0, 20.0, 20.0, 20.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo(
                     "buildcraft.help.builder.fluid_mode.title",
                     -7811841,
                     "buildcraft.help.builder.fluid_mode.desc1",
                     "buildcraft.help.builder.fluid_mode.desc2"
                  )
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(130.0, 20.0, 20.0, 20.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo(
                     "buildcraft.help.builder.contents_mode.title",
                     -3372852,
                     "buildcraft.help.builder.contents_mode.desc1",
                     "buildcraft.help.builder.contents_mode.desc2"
                  )
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(179.0, 18.0, 70.0, 106.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo(
                     "buildcraft.help.builder.display.title", -1980113, "buildcraft.help.builder.display.desc1", "buildcraft.help.builder.display.desc2"
                  )
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(179.0, 145.0, 70.0, 47.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo(
                     "buildcraft.help.builder.tanks.title", -7820545, "buildcraft.help.builder.tanks.desc1", "buildcraft.help.builder.tanks.desc2"
                  )
               )
            );
      }
   }

   @Override
   protected void init() {
      super.init();
      this.fluidModeButton = new GuiBuilder.FluidModeButton(this.leftPos + 150, this.topPos + 20);
      this.addRenderableWidget(this.fluidModeButton);
      this.contentsModeButton = new GuiBuilder.ContentsModeButton(this.leftPos + 130, this.topPos + 20);
      this.addRenderableWidget(this.contentsModeButton);
   }

   @Override
   protected void containerTick() {
      super.containerTick();
      if (this.fluidModeButton != null) {
         this.fluidModeButton.refreshTooltip();
      }

      if (this.contentsModeButton != null) {
         this.contentsModeButton.refreshTooltip();
      }
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
      ICON_BLUEPRINT_GUI.drawAt(this.mainGui.rootElement.offset(169.0, 0.0));
   }

   @Override
   protected void drawExtendedInventoryChrome() {
      // A mod-extended inventory redraws the generic vanilla panel across the full 0..176 width, whose right frame
      // covers the blueprint box's left divider. Re-blit the box's divider so it keeps its clean edge beside the
      // tanks/slots, plus its bottom-left corner so the divider closes into the box's bottom frame (instead of the
      // generic panel's dark groove + black edge showing through as a notch).
      ICON_BLUEPRINT_EDGE.drawAt(this.mainGui.rootElement.offset(169.0, 0.0));
      ICON_BLUEPRINT_CORNER.drawAt(this.mainGui.rootElement.offset(173.0, 214.0));
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      // Block name on the canonical left title anchor (x=8, y=6).
      graphics.text(this.font, this.title.getString(), 8, 6, -12566464, false);
      // "Inventory" label, matching the fully-labelled screens (Tank, engines, ...). X = 8 is the inventory's own
      // left edge (addFullPlayerInventory(8, 140)); Y comes from playerInventoryLabelY() = firstPlayerRowY() - 12,
      // i.e. derived from the real slot rows (vanilla convention), not a hardcoded coordinate.
      graphics.text(this.font, this.playerInventoryTitle, 8, this.playerInventoryLabelY(), -12566464, false);
      int leftToBreak = ((ContainerBuilder)this.menu).getSyncedLeftToBreak();
      int leftToPlace = ((ContainerBuilder)this.menu).getSyncedLeftToPlace();
      int y = 50;
      // Draw these as plain strings in the vanilla label colour (-12566464 = 0xFF404040), the same as the title and
      // Inventory labels above. A Component with .withStyle(DARK_GRAY) would override that colour with 0x555555, which
      // is both inconsistent with the other labels and nearly invisible against a dark-mode resource pack's panel.
      if (leftToBreak > 0) {
         graphics.text(this.font, "Break: " + leftToBreak, 10, y, -12566464, false);
         y += 10;
      }

      if (leftToPlace > 0) {
         graphics.text(this.font, "Place: " + leftToPlace, 10, y, -12566464, false);
      }
   }

   private class ContentsModeButton extends BCButton {
      private static final ItemStack CHEST_ICON = new ItemStack(Items.CHEST);
      private static final ItemStack BARRIER_OVERLAY = new ItemStack(Items.BARRIER);
      private EnumContainerContentsMode lastKnown;

      ContentsModeButton(int x, int y) {
         super(x, y, 20, 20, Component.empty());
         this.refreshTooltip();
      }

      //? if >= 1.21.10 {
      public void onPress(InputWithModifiers modifiers) {
         ((ContainerBuilder)GuiBuilder.this.menu).sendMessage(13, buf -> {});
      }
      //?} else {
      /*public void onPress() {
         ((ContainerBuilder)GuiBuilder.this.menu).sendMessage(13, buf -> {});
      }
      *///?}

      @Override
      protected void drawButtonContent(BCGraphics graphics, int mouseX, int mouseY, float partialTick) {
         this.drawDefaultButtonSprite(graphics);
         graphics.item(CHEST_ICON, this.getX() + 2, this.getY() + 2);
         if (((ContainerBuilder)GuiBuilder.this.menu).getSyncedContentsMode() == EnumContainerContentsMode.IGNORE) {
            graphics.item(BARRIER_OVERLAY, this.getX() + 2, this.getY() + 2);
         }
      }

      @Override
      protected void updateWidgetNarration(NarrationElementOutput output) {
         this.defaultButtonNarrationText(output);
      }

      void refreshTooltip() {
         EnumContainerContentsMode mode = ((ContainerBuilder)GuiBuilder.this.menu).getSyncedContentsMode();
         if (mode != this.lastKnown) {
            this.lastKnown = mode;
            this.setTooltip(Tooltip.create(Component.translatable(mode.tooltipKey())));
         }
      }
   }

   private class FluidModeButton extends BCButton {
      private EnumFluidHandlingMode lastKnown;

      FluidModeButton(int x, int y) {
         super(x, y, 20, 20, Component.empty());
         this.refreshTooltip();
      }

      //? if >= 1.21.10 {
      public void onPress(InputWithModifiers modifiers) {
         ((ContainerBuilder)GuiBuilder.this.menu).sendMessage(12, buf -> {});
      }
      //?} else {
      /*public void onPress() {
         ((ContainerBuilder)GuiBuilder.this.menu).sendMessage(12, buf -> {});
      }
      *///?}

      @Override
      protected void drawButtonContent(BCGraphics graphics, int mouseX, int mouseY, float partialTick) {
         this.drawDefaultButtonSprite(graphics);
         EnumFluidHandlingMode mode = ((ContainerBuilder)GuiBuilder.this.menu).getSyncedFluidMode();
         graphics.item(mode.icon(), this.getX() + 2, this.getY() + 2);
      }

      @Override
      protected void updateWidgetNarration(NarrationElementOutput output) {
         this.defaultButtonNarrationText(output);
      }

      void refreshTooltip() {
         EnumFluidHandlingMode mode = ((ContainerBuilder)GuiBuilder.this.menu).getSyncedFluidMode();
         if (mode != this.lastKnown) {
            this.lastKnown = mode;
            this.setTooltip(Tooltip.create(Component.translatable(mode.tooltipKey())));
         }
      }
   }
}
