/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.gui;

import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.button.BCButton;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.container.ContainerEmzuliPipe_BC8;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;

public class GuiEmzuliPipe_BC8 extends BcScreen<ContainerEmzuliPipe_BC8> {
   private static final Identifier TEXTURE = Identifier.parse("buildcrafttransport:textures/gui/pipe_emzuli.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0.0, 0.0, 176.0, 166.0);
   private GuiEmzuliPipe_BC8.PaintButton[] paintButtons = new GuiEmzuliPipe_BC8.PaintButton[4];
   private GuiEmzuliPipe_BC8.PaintButton activePressedButton = null;

   public GuiEmzuliPipe_BC8(ContainerEmzuliPipe_BC8 menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title, 176, 166);
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);
   }

   @Override
   protected void initGuiElements() {
      this.paintButtons[0] = this.addPaintButton(PipeBehaviourEmzuli.SlotIndex.SQUARE, 49, 19);
      this.paintButtons[1] = this.addPaintButton(PipeBehaviourEmzuli.SlotIndex.CIRCLE, 49, 47);
      this.paintButtons[2] = this.addPaintButton(PipeBehaviourEmzuli.SlotIndex.TRIANGLE, 106, 19);
      this.paintButtons[3] = this.addPaintButton(PipeBehaviourEmzuli.SlotIndex.CROSS, 106, 47);

      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(25.0, 21.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.emzuli.filter.title", -7811841, "buildcraft.help.emzuli.filter.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(25.0, 49.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.emzuli.filter.title", -7811841, "buildcraft.help.emzuli.filter.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(134.0, 21.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.emzuli.filter.title", -7811841, "buildcraft.help.emzuli.filter.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(134.0, 49.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.emzuli.filter.title", -7811841, "buildcraft.help.emzuli.filter.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(49.0, 19.0, 20.0, 20.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.emzuli.paint.title", -2249985, "buildcraft.help.emzuli.paint.desc1", "buildcraft.help.emzuli.paint.desc2"
               )
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(49.0, 47.0, 20.0, 20.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.emzuli.paint.title", -2249985, "buildcraft.help.emzuli.paint.desc1", "buildcraft.help.emzuli.paint.desc2"
               )
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(106.0, 19.0, 20.0, 20.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.emzuli.paint.title", -2249985, "buildcraft.help.emzuli.paint.desc1", "buildcraft.help.emzuli.paint.desc2"
               )
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(106.0, 47.0, 20.0, 20.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.emzuli.paint.title", -2249985, "buildcraft.help.emzuli.paint.desc1", "buildcraft.help.emzuli.paint.desc2"
               )
            )
         );
   }

   private GuiEmzuliPipe_BC8.PaintButton addPaintButton(PipeBehaviourEmzuli.SlotIndex index, int x, int y) {
      int bx = this.leftPos + x;
      int by = this.topPos + y;
      GuiEmzuliPipe_BC8.PaintButton btn = new GuiEmzuliPipe_BC8.PaintButton(index, bx, by);
      this.addRenderableWidget(btn);
      return btn;
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      int mouseX = (int)event.x();
      int mouseY = (int)event.y();
      int button = event.button();

      for (GuiEmzuliPipe_BC8.PaintButton btn : this.paintButtons) {
         if (btn != null && btn.isMouseOver(mouseX, mouseY)) {
            btn.handleClick(button);
            this.activePressedButton = btn;
            return true;
         }
      }

      return super.mouseClicked(event, doubleClick);
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      if (this.activePressedButton != null) {
         this.activePressedButton = null;
         return true;
      } else {
         return super.mouseReleased(event);
      }
   }

   private static DyeColor cycleColour(DyeColor current) {
      if (current == null) {
         return DyeColor.WHITE;
      }

      int next = current.ordinal() + 1;
      return next >= 16 ? null : DyeColor.byId(next);
   }

   private static DyeColor cycleColourBackward(DyeColor current) {
      if (current == null) {
         return DyeColor.byId(15);
      }

      int next = current.ordinal() - 1;
      return next < 0 ? null : DyeColor.byId(next);
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      String titleStr = Component.translatable("gui.pipes.emzuli.title").getString();
      int titleX = (this.imageWidth - this.font.width(titleStr)) / 2;
      graphics.text(this.font, titleStr, titleX, 6, -12566464, false);
      graphics.text(this.font, this.playerInventoryTitle, 8, this.imageHeight - 93, -12566464, false);
      PipeBehaviourEmzuli.SlotIndex currentSlot = ((ContainerEmzuliPipe_BC8)this.menu).behaviour.getCurrentSlot();

      for (PipeBehaviourEmzuli.SlotIndex index : ((ContainerEmzuliPipe_BC8)this.menu).behaviour.getActiveSlots()) {
         boolean current = index == currentSlot;
         int ix = index.ordinal() < 2 ? 4 : 155;
         int iy = index.ordinal() % 2 == 0 ? 21 : 49;
         int colour = current ? -16711936 : -256;
         graphics.fill(ix, iy, ix + 4, iy + 16, colour);
      }
   }

   private class PaintButton extends BCButton {
      private final PipeBehaviourEmzuli.SlotIndex index;
      private int pressedButton = -1;

      public PaintButton(PipeBehaviourEmzuli.SlotIndex index, int x, int y) {
         super(x, y, 20, 20, Component.empty());
         this.index = index;
         this.updateTooltip();
      }

      public void onPress(InputWithModifiers input) {
      }

      public void handleClick(int button) {
         DyeColor current = ((ContainerEmzuliPipe_BC8)GuiEmzuliPipe_BC8.this.menu).behaviour.slotColours.get(this.index);
         DyeColor next;
         switch (button) {
            case 0:
               next = GuiEmzuliPipe_BC8.cycleColour(current);
               break;
            case 1:
               next = GuiEmzuliPipe_BC8.cycleColourBackward(current);
               break;
            case 2:
               next = null;
               break;
            default:
               return;
         }

         ((ContainerEmzuliPipe_BC8)GuiEmzuliPipe_BC8.this.menu).paintWidgets.get(this.index).setColour(next);
         if (next == null) {
            ((ContainerEmzuliPipe_BC8)GuiEmzuliPipe_BC8.this.menu).behaviour.slotColours.remove(this.index);
         } else {
            ((ContainerEmzuliPipe_BC8)GuiEmzuliPipe_BC8.this.menu).behaviour.slotColours.put(this.index, next);
         }

         this.updateTooltip();
         this.pressedButton = button;
      }

      private void updateTooltip() {
         DyeColor colour = ((ContainerEmzuliPipe_BC8)GuiEmzuliPipe_BC8.this.menu).behaviour.slotColours.get(this.index);
         Component tooltip;
         if (colour == null) {
            tooltip = Component.translatable("gui.pipes.emzuli.nopaint");
         } else {
            tooltip = Component.translatable("gui.pipes.emzuli.paint", new Object[]{ColourUtil.getTextFullTooltip(colour)});
         }

         this.setTooltip(Tooltip.create(tooltip));
      }

      @Override
      protected void drawButtonContent(BCGraphics graphics, int mouseX, int mouseY, float partialTick) {
         int v = GuiEmzuliPipe_BC8.this.activePressedButton == this ? 20 : 0;
         GuiIcon bgIcon = new GuiIcon(GuiEmzuliPipe_BC8.TEXTURE, 176.0, v, 20.0, 20.0, 256);
         bgIcon.drawAt(this.getX(), this.getY());
         DyeColor colour = ((ContainerEmzuliPipe_BC8)GuiEmzuliPipe_BC8.this.menu).behaviour.slotColours.get(this.index);
         if (colour == null) {
            GuiIcon noPaint = new GuiIcon(GuiEmzuliPipe_BC8.TEXTURE, 176.0, 40.0, 16.0, 16.0, 256);
            noPaint.drawAt(this.getX() + 2, this.getY() + 2);
         } else {
            Identifier brushTex = Identifier.parse("buildcraftcore:textures/item/paintbrush/" + colour.getName() + ".png");
            GuiIcon brushIcon = new GuiIcon(brushTex, 0.0, 0.0, 16.0, 16.0, 16);
            brushIcon.drawAt(this.getX() + 2, this.getY() + 2);
         }
      }

      @Override
      protected void updateWidgetNarration(NarrationElementOutput output) {
         this.defaultButtonNarrationText(output);
      }
   }
}
