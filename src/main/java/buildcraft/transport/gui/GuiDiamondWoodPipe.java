/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.gui;

import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.button.BCButton;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.transport.container.ContainerDiamondWoodPipe;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
//? if >= 1.21.10 {
import net.minecraft.client.input.InputWithModifiers;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiDiamondWoodPipe extends BcScreen<ContainerDiamondWoodPipe> {
   private static final Identifier TEXTURE = Identifier.parse("buildcrafttransport:textures/gui/bcr/pipe_emerald.png");
   private static final Identifier TEXTURE_BUTTON = Identifier.parse("buildcrafttransport:textures/gui/bcr/pipe_emerald_button.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0.0, 0.0, 175.0, 156.0);
   private static final GuiIcon ICON_ROUND_ROBIN_INDEX = new GuiIcon(TEXTURE, 176.0, 0.0, 20.0, 20.0);
   private static final GuiIcon ICON_ROUND_ROBIN_NONE = new GuiIcon(TEXTURE, 176.0, 20.0, 20.0, 20.0);
   private GuiDiamondWoodPipe.FilterButton whiteListButton;
   private GuiDiamondWoodPipe.FilterButton blackListButton;
   private GuiDiamondWoodPipe.FilterButton roundRobinButton;

   public GuiDiamondWoodPipe(ContainerDiamondWoodPipe menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title, 175, heightForSlots(menu, 156));
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      // Title, centred: this.title is the name passed at menu open (the gui.buildcraft.pipe_diamond_wood.title
      // lang string). Centred on imageWidth, no hardcoded coordinate.
      String titleStr = this.title.getString();
      graphics.text(this.font, titleStr, (this.imageWidth - this.font.width(titleStr)) / 2, 6, -12566464, false);
      // "Inventory" label: X = 8 matches the player inventory's left edge (addFullPlayerInventory(8, 79)),
      // Y = playerInventoryLabelY() derives from the real slot rows, not hardcoded.
      graphics.text(this.font, this.playerInventoryTitle, 8, this.playerInventoryLabelY(), -12566464, false);
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
      if (((ContainerDiamondWoodPipe)this.menu).behaviour.pipe.getFlow() instanceof IFlowItems
         && ((ContainerDiamondWoodPipe)this.menu).behaviour.filterMode == PipeBehaviourWoodDiamond.FilterMode.ROUND_ROBIN) {
         GuiIcon icon = ((ContainerDiamondWoodPipe)this.menu).behaviour.filterValid ? ICON_ROUND_ROBIN_INDEX : ICON_ROUND_ROBIN_NONE;
         int xOffset = ((ContainerDiamondWoodPipe)this.menu).behaviour.filterValid ? 18 * ((ContainerDiamondWoodPipe)this.menu).behaviour.currentFilter : 0;
         icon.drawAt(this.mainGui.rootElement.getX() + 6.0 + xOffset, this.mainGui.rootElement.getY() + 16.0);
      }
   }

   @Override
   protected void initGuiElements() {
      int bx = this.leftPos + 8;
      int by = this.topPos + 40;
      this.whiteListButton = new GuiDiamondWoodPipe.FilterButton(
         bx, by, PipeBehaviourWoodDiamond.FilterMode.WHITE_LIST, 19, 19, "tip.PipeItemsEmerald.whitelist"
      );
      this.blackListButton = new GuiDiamondWoodPipe.FilterButton(
         bx + 18, by, PipeBehaviourWoodDiamond.FilterMode.BLACK_LIST, 37, 19, "tip.PipeItemsEmerald.blacklist"
      );
      this.addRenderableWidget(this.whiteListButton);
      this.addRenderableWidget(this.blackListButton);
      if (((ContainerDiamondWoodPipe)this.menu).behaviour.pipe.getFlow() instanceof IFlowItems) {
         this.roundRobinButton = new GuiDiamondWoodPipe.FilterButton(
            bx + 36, by, PipeBehaviourWoodDiamond.FilterMode.ROUND_ROBIN, 55, 19, "tip.PipeItemsEmerald.roundrobin"
         );
         this.addRenderableWidget(this.roundRobinButton);
      }

      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(8.0, 18.0, 160.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.diamond_wood_pipe.filter.title",
                  -7811841,
                  "buildcraft.help.diamond_wood_pipe.filter.desc1",
                  "buildcraft.help.diamond_wood_pipe.filter.desc2"
               )
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(8.0, 40.0, 54.0, 18.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.diamond_wood_pipe.mode.title", -13176, "buildcraft.help.diamond_wood_pipe.mode.desc")
            )
         );
   }

   private void setFilterMode(PipeBehaviourWoodDiamond.FilterMode mode) {
      ((ContainerDiamondWoodPipe)this.menu).behaviour.filterMode = mode;
      ((ContainerDiamondWoodPipe)this.menu).sendNewFilterMode(mode);
   }

   private class FilterButton extends BCButton {
      private final PipeBehaviourWoodDiamond.FilterMode mode;
      private final int iconU;
      private final int iconV;

      public FilterButton(int x, int y, PipeBehaviourWoodDiamond.FilterMode mode, int u, int v, String tooltipKey) {
         super(x, y, 18, 18, Component.empty());
         this.mode = mode;
         this.iconU = u;
         this.iconV = v;
         this.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
      }

      //? if >= 1.21.10 {
      public void onPress(InputWithModifiers modifiers) {
         GuiDiamondWoodPipe.this.setFilterMode(this.mode);
      }
      //?} else {
      /*public void onPress() {
         GuiDiamondWoodPipe.this.setFilterMode(this.mode);
      }
      *///?}

      @Override
      protected void drawButtonContent(BCGraphics graphics, int mouseX, int mouseY, float partialTick) {
         boolean selected = ((ContainerDiamondWoodPipe)GuiDiamondWoodPipe.this.menu).behaviour.filterMode == this.mode;
         int state;
         if (!this.active) {
            state = 0;
         } else if (this.isHovered()) {
            state = selected ? 4 : 2;
         } else {
            state = selected ? 3 : 1;
         }

         int baseU = state * 18;
         int baseV = 0;
         GuiIcon bgIcon = new GuiIcon(GuiDiamondWoodPipe.TEXTURE_BUTTON, baseU, baseV, this.width, this.height);
         bgIcon.drawAt(this.getX(), this.getY());
         GuiIcon fgIcon = new GuiIcon(GuiDiamondWoodPipe.TEXTURE_BUTTON, this.iconU, this.iconV, 16.0, 16.0);
         fgIcon.drawAt(this.getX() + 1, this.getY() + 1);
      }

      @Override
      protected void updateWidgetNarration(NarrationElementOutput output) {
         this.defaultButtonNarrationText(output);
      }
   }
}
