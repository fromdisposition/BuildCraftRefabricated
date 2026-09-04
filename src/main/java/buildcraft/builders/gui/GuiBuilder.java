/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.gui;

import buildcraft.lib.misc.LocaleUtil;
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
   // Height 214 stops just above the box's bottom frame; redrawn when a mod-extended inventory overdraws the divider.
   private static final GuiIcon ICON_BLUEPRINT_EDGE = new GuiIcon(TEXTURE_BLUEPRINT, 169.0, 0.0, 10.0, 214.0);
   // Blitted narrow from x=173 so it covers the generic panel's dark edge without overwriting the divider's light pixels at x=169..172.
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
      if ((this.menu).tile != null) {
         for (int i = 0; i < 4; i++) {
            int idx = i;
            WidgetFluidTank widget = idx < (this.menu).widgetTanks.size() ? (this.menu).widgetTanks.get(idx) : null;
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
      // Re-blit divider and corner because a mod-extended inventory's generic panel overdraws them.
      ICON_BLUEPRINT_EDGE.drawAt(this.mainGui.rootElement.offset(169.0, 0.0));
      ICON_BLUEPRINT_CORNER.drawAt(this.mainGui.rootElement.offset(173.0, 214.0));
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      graphics.text(this.font, this.title.getString(), 8, 6, -12566464, false);
      // X matches addFullPlayerInventory's left edge; Y derives from firstPlayerRowY(), not a hardcoded constant.
      graphics.text(this.font, this.playerInventoryTitle, 8, this.playerInventoryLabelY(), -12566464, false);
      int leftToBreak = (this.menu).getSyncedLeftToBreak();
      int leftToPlace = (this.menu).getSyncedLeftToPlace();
      int y = 50;
      // -12566464 = 0xFF404040, the vanilla label colour; Component.withStyle(DARK_GRAY) would use 0x555555, faint on dark-mode packs.
      if (leftToBreak > 0) {
         graphics.text(this.font, LocaleUtil.localize("gui.leftToBreak") + ": " + leftToBreak, 8, y, -12566464, false);
         y += 10;
      }

      if (leftToPlace > 0) {
         graphics.text(this.font, LocaleUtil.localize("gui.leftToPlace") + ": " + leftToPlace, 8, y, -12566464, false);
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
         (GuiBuilder.this.menu).sendMessage(ContainerBuilder.NET_CONTENTS_MODE_CLICK, buf -> {});
      }
      //?} else {
      /*public void onPress() {
         (GuiBuilder.this.menu).sendMessage(ContainerBuilder.NET_CONTENTS_MODE_CLICK, buf -> {});
      }
      *///?}

      @Override
      protected void drawButtonContent(BCGraphics graphics, int mouseX, int mouseY, float partialTick) {
         this.drawDefaultButtonSprite(graphics);
         graphics.item(CHEST_ICON, this.getX() + 2, this.getY() + 2);
         if ((GuiBuilder.this.menu).getSyncedContentsMode() == EnumContainerContentsMode.IGNORE) {
            graphics.item(BARRIER_OVERLAY, this.getX() + 2, this.getY() + 2);
         }
      }

      @Override
      protected void updateWidgetNarration(NarrationElementOutput output) {
         this.defaultButtonNarrationText(output);
      }

      void refreshTooltip() {
         EnumContainerContentsMode mode = (GuiBuilder.this.menu).getSyncedContentsMode();
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
         (GuiBuilder.this.menu).sendMessage(ContainerBuilder.NET_FLUID_MODE_CLICK, buf -> {});
      }
      //?} else {
      /*public void onPress() {
         (GuiBuilder.this.menu).sendMessage(ContainerBuilder.NET_FLUID_MODE_CLICK, buf -> {});
      }
      *///?}

      @Override
      protected void drawButtonContent(BCGraphics graphics, int mouseX, int mouseY, float partialTick) {
         this.drawDefaultButtonSprite(graphics);
         EnumFluidHandlingMode mode = (GuiBuilder.this.menu).getSyncedFluidMode();
         graphics.item(mode.icon(), this.getX() + 2, this.getY() + 2);
      }

      @Override
      protected void updateWidgetNarration(NarrationElementOutput output) {
         this.defaultButtonNarrationText(output);
      }

      void refreshTooltip() {
         EnumFluidHandlingMode mode = (GuiBuilder.this.menu).getSyncedFluidMode();
         if (mode != this.lastKnown) {
            this.lastKnown = mode;
            this.setTooltip(Tooltip.create(Component.translatable(mode.tooltipKey())));
         }
      }
   }
}
