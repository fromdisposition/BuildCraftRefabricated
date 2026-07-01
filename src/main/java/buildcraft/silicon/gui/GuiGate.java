/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gui;

import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.statement.GuiElementStatement;
import buildcraft.lib.gui.statement.GuiElementStatementDrag;
import buildcraft.lib.gui.statement.GuiElementStatementParam;
import buildcraft.lib.gui.statement.GuiElementStatementSource;
import buildcraft.silicon.container.ContainerGate;
import net.minecraft.client.Minecraft;
//? if >= 1.21.10 {
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class GuiGate extends BcScreen<ContainerGate> {
   private static final Identifier TEXTURE_BASE = Identifier.parse("buildcraftsilicon:textures/gui/gate_interface.png");
   private static final GuiIcon BACKGROUND_TOP = new GuiIcon(TEXTURE_BASE, 0.0, 0.0, 176.0, 16.0);
   private static final GuiIcon BACKGROUND_BOTTOM = new GuiIcon(TEXTURE_BASE, 0.0, 48.0, 176.0, 101.0);
   private static final GuiIcon BACKGROUND_ROW = new GuiIcon(TEXTURE_BASE, 0.0, 23.0, 176.0, 18.0);
   private final int numRows;

   public GuiGate(ContainerGate container, Inventory playerInventory, Component title) {
      super(container, playerInventory, title, 176, 117 + container.slotHeight * 18);
      this.numRows = container.slotHeight;
   }

   @Override
   protected void initGuiElements() {
      if (((ContainerGate)this.menu).gate != null) {
         boolean twoColumns = ((ContainerGate)this.menu).gate.isSplitInTwo();
         int horizontalSlotCount = twoColumns ? 2 : 1;
         int verticalSlotCount = ((ContainerGate)this.menu).gate.variant.numSlots / horizontalSlotCount;
         int numTriggerArgs = ((ContainerGate)this.menu).gate.variant.numTriggerArgs;
         int numActionArgs = ((ContainerGate)this.menu).gate.variant.numActionArgs;
         int slotPairWidth = 18 * (3 + numTriggerArgs + numActionArgs);
         int slotPairStart = (162 - (slotPairWidth + (twoColumns ? slotPairWidth + 18 : 0))) / 2;
         this.mainGui.shownElements.add(new GuiElementStatementDrag(this.mainGui));
         int totalHeight = verticalSlotCount * 18;
         int rowBaseY = 16;

         for (int col = 0; col < horizontalSlotCount; col++) {
            int baseX = slotPairStart + 7 + col * (18 + slotPairWidth);
            int actionStartX = baseX + 18 * (2 + numTriggerArgs);
            this.mainGui
               .shownElements
               .add(
                  new DummyHelpElement(
                     new GuiRectangle(baseX, rowBaseY, 18.0, totalHeight).offset(this.mainGui.rootElement),
                     new ElementHelpInfo(
                        "buildcraft.help.gate.trigger.title", -30584, "buildcraft.help.gate.trigger.desc1", "buildcraft.help.gate.trigger.desc2"
                     )
                  )
               );
            if (numTriggerArgs > 0) {
               this.mainGui
                  .shownElements
                  .add(
                     new DummyHelpElement(
                        new GuiRectangle(baseX + 18, rowBaseY, 18 * numTriggerArgs, totalHeight).offset(this.mainGui.rootElement),
                        new ElementHelpInfo("buildcraft.help.gate.params.title", -2249985, "buildcraft.help.gate.params.desc")
                     )
                  );
            }

            this.mainGui
               .shownElements
               .add(
                  new DummyHelpElement(
                     new GuiRectangle(baseX + 18 * (1 + numTriggerArgs), rowBaseY, 18.0, totalHeight).offset(this.mainGui.rootElement),
                     new ElementHelpInfo("buildcraft.help.gate.connection.title", -7798870, "buildcraft.help.gate.connection.desc")
                  )
               );
            this.mainGui
               .shownElements
               .add(
                  new DummyHelpElement(
                     new GuiRectangle(actionStartX, rowBaseY, 18.0, totalHeight).offset(this.mainGui.rootElement),
                     new ElementHelpInfo(
                        "buildcraft.help.gate.action.title", -7811841, "buildcraft.help.gate.action.desc1", "buildcraft.help.gate.action.desc2"
                     )
                  )
               );
            if (numActionArgs > 0) {
               this.mainGui
                  .shownElements
                  .add(
                     new DummyHelpElement(
                        new GuiRectangle(actionStartX + 18, rowBaseY, 18 * numActionArgs, totalHeight).offset(this.mainGui.rootElement),
                        new ElementHelpInfo("buildcraft.help.gate.params.title", -2249985, "buildcraft.help.gate.params.desc")
                     )
                  );
            }
         }

         this.mainGui.shownElements.add(new GuiElementStatementSource<>(this.mainGui, true, ((ContainerGate)this.menu).possibleTriggersContext));
         this.mainGui.shownElements.add(new GuiElementStatementSource<>(this.mainGui, false, ((ContainerGate)this.menu).possibleActionsContext));

         for (int row = 0; row < verticalSlotCount; row++) {
            for (int col = 0; col < horizontalSlotCount; col++) {
               int pairIndex = row + col * verticalSlotCount;
               int baseX = slotPairStart + 7 + col * (18 + slotPairWidth);
               int baseY = 16 + row * 18;
               IGuiArea triggerArea = new GuiRectangle(baseX, baseY, 18.0, 18.0).offset(this.mainGui.rootElement);
               this.mainGui
                  .shownElements
                  .add(
                     new GuiElementStatement<>(
                        this.mainGui,
                        triggerArea,
                        ((ContainerGate)this.menu).gate.statements[pairIndex].trigger,
                        ((ContainerGate)this.menu).possibleTriggersContext,
                        true
                     )
                  );

               for (int i = 0; i < numTriggerArgs; i++) {
                  IGuiArea paramArea = new GuiRectangle(baseX + 18 * (i + 1), baseY, 18.0, 18.0).offset(this.mainGui.rootElement);
                  this.mainGui
                     .shownElements
                     .add(
                        new GuiElementStatementParam(
                           this.mainGui, paramArea, ((ContainerGate)this.menu).gate, ((ContainerGate)this.menu).gate.statements[pairIndex].trigger, i, true
                        )
                     );
               }

               int actionStartX = baseX + 18 * (2 + numTriggerArgs);
               IGuiArea actionArea = new GuiRectangle(actionStartX, baseY, 18.0, 18.0).offset(this.mainGui.rootElement);
               this.mainGui
                  .shownElements
                  .add(
                     new GuiElementStatement<>(
                        this.mainGui,
                        actionArea,
                        ((ContainerGate)this.menu).gate.statements[pairIndex].action,
                        ((ContainerGate)this.menu).possibleActionsContext,
                        true
                     )
                  );

               for (int i = 0; i < numActionArgs; i++) {
                  IGuiArea paramArea = new GuiRectangle(actionStartX + 18 * (i + 1), baseY, 18.0, 18.0).offset(this.mainGui.rootElement);
                  this.mainGui
                     .shownElements
                     .add(
                        new GuiElementStatementParam(
                           this.mainGui, paramArea, ((ContainerGate)this.menu).gate, ((ContainerGate)this.menu).gate.statements[pairIndex].action, i, true
                        )
                     );
               }
            }
         }
      }
   }

   //? if >= 1.21.10 {
   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      return this.bcMouseClicked((int)event.x(), (int)event.y(), event.button()) || super.mouseClicked(event, doubleClick);
   }
   //?} else {
   /*@Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return this.bcMouseClicked((int)mouseX, (int)mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
   }
   *///?}

   private boolean bcMouseClicked(int clickX, int clickY, int button) {
      if (button == 0 && ((ContainerGate)this.menu).gate != null) {
         boolean twoColumns = ((ContainerGate)this.menu).gate.isSplitInTwo();
         int horizontalSlotCount = twoColumns ? 2 : 1;
         int verticalSlotCount = ((ContainerGate)this.menu).gate.variant.numSlots / horizontalSlotCount;
         int numTriggerArgs = ((ContainerGate)this.menu).gate.variant.numTriggerArgs;
         int numActionArgs = ((ContainerGate)this.menu).gate.variant.numActionArgs;
         int slotPairWidth = 18 * (3 + numTriggerArgs + numActionArgs);
         int slotPairStart = (162 - (slotPairWidth + (twoColumns ? slotPairWidth + 18 : 0))) / 2;
         int mx = clickX - this.leftPos;
         int my = clickY - this.topPos;

         for (int row = 0; row < verticalSlotCount - 1; row++) {
            for (int col = 0; col < horizontalSlotCount; col++) {
               int connBaseX = slotPairStart + 7 + col * (18 + slotPairWidth) + 18 * (1 + numTriggerArgs);
               int connBaseY = 25 + row * 18;
               if (mx >= connBaseX && mx < connBaseX + 18 && my >= connBaseY && my < connBaseY + 18) {
                  int pairIndex = row + col * verticalSlotCount;
                  boolean newState = !((ContainerGate)this.menu).gate.connections[pairIndex];
                  ((ContainerGate)this.menu).setConnected(pairIndex, newState);
                  if (Minecraft.getInstance().player != null) {
                     Minecraft.getInstance().player.playSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                  }

                  return true;
               }
            }
         }
      }

      return false;
   }

   @Override
   protected void init() {
      super.init();
      ((ContainerGate)this.menu).requestValidStatements();
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      if (((ContainerGate)this.menu).gate == null) {
         BACKGROUND_TOP.drawAt(this.leftPos, this.topPos);
         BACKGROUND_BOTTOM.drawAt(this.leftPos, this.topPos + 16);
      } else {
         BACKGROUND_TOP.drawAt(this.leftPos, this.topPos);

         for (int i = 0; i < this.numRows; i++) {
            BACKGROUND_ROW.drawAt(this.leftPos, this.topPos + 16 + i * 18);
         }

         BACKGROUND_BOTTOM.drawAt(this.leftPos, this.topPos + 16 + this.numRows * 18);
         boolean twoColumns = ((ContainerGate)this.menu).gate.isSplitInTwo();
         int horizontalSlotCount = twoColumns ? 2 : 1;
         int verticalSlotCount = ((ContainerGate)this.menu).gate.variant.numSlots / horizontalSlotCount;
         int numTriggerArgs = ((ContainerGate)this.menu).gate.variant.numTriggerArgs;
         int numActionArgs = ((ContainerGate)this.menu).gate.variant.numActionArgs;
         int slotPairWidth = 18 * (3 + numTriggerArgs + numActionArgs);
         int slotPairStart = (162 - (slotPairWidth + (twoColumns ? slotPairWidth + 18 : 0))) / 2;

         for (int row = 0; row < verticalSlotCount; row++) {
            for (int col = 0; col < horizontalSlotCount; col++) {
               int pairIndex = row + col * verticalSlotCount;
               int baseX = this.leftPos + slotPairStart + 7 + col * (18 + slotPairWidth);
               int baseY = this.topPos + 16 + row * 18;
               int connectorPos = baseX + 18 * (1 + numTriggerArgs);
               boolean triggerOn = ((ContainerGate)this.menu).gate.triggerOn[pairIndex]
                  && ((ContainerGate)this.menu).gate.statements[pairIndex].trigger.get() != null;
               boolean actionOn = ((ContainerGate)this.menu).gate.actionOn[pairIndex]
                  && ((ContainerGate)this.menu).gate.statements[pairIndex].action.get() != null;
               boolean connectedIsOn = false;
               if (pairIndex < ((ContainerGate)this.menu).gate.connections.length && ((ContainerGate)this.menu).gate.connections[pairIndex]) {
                  connectedIsOn = ((ContainerGate)this.menu).gate.actionOn[pairIndex] || ((ContainerGate)this.menu).gate.actionOn[pairIndex + 1];
               } else if (pairIndex > 0 && ((ContainerGate)this.menu).gate.connections[pairIndex - 1]) {
                  connectedIsOn = ((ContainerGate)this.menu).gate.actionOn[pairIndex] || ((ContainerGate)this.menu).gate.actionOn[pairIndex - 1];
               } else {
                  connectedIsOn = actionOn;
               }

               new GuiIcon(TEXTURE_BASE, 176 + (triggerOn ? 18 : 0), 18.0, 7.0, 18.0).drawAt(connectorPos, baseY);
               new GuiIcon(TEXTURE_BASE, 187 + (actionOn ? 18 : 0), 18.0, 7.0, 18.0).drawAt(connectorPos + 11, baseY);
               new GuiIcon(TEXTURE_BASE, 180 + (connectedIsOn ? 18 : 0), 18.0, 4.0, 18.0).drawAt(connectorPos + 7, baseY);
               if (row < verticalSlotCount - 1) {
                  int connBaseX = connectorPos;
                  int connBaseY = baseY + 9;
                  boolean isConnected = ((ContainerGate)this.menu).gate.connections[pairIndex];
                  boolean actionAbove = ((ContainerGate)this.menu).gate.actionOn[pairIndex];
                  boolean actionBelow = ((ContainerGate)this.menu).gate.actionOn[pairIndex + 1];
                  new GuiIcon(TEXTURE_BASE, 176 + (actionAbove ? 18 : 0), 36 + (isConnected ? 18 : 0), 18.0, 9.0).drawAt(connBaseX, connBaseY);
                  new GuiIcon(TEXTURE_BASE, 176 + (actionBelow ? 18 : 0), 45 + (isConnected ? 18 : 0), 18.0, 9.0).drawAt(connBaseX, connBaseY + 9);
                  int mx = (int)this.mainGui.mouse.getX();
                  int my = (int)this.mainGui.mouse.getY();
                  boolean hovered = mx >= connBaseX && mx < connBaseX + 18 && my >= connBaseY && my < connBaseY + 18;
                  int btnU = hovered ? 194 : 176;
                  int btnV = 72;
                  new GuiIcon(TEXTURE_BASE, btnU, btnV, 18.0, 18.0).drawAt(connBaseX, connBaseY);
               }
            }
         }
      }
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      String titleStr = ((ContainerGate)this.menu).gate.variant.getLocalizedName().getString();
      graphics.text(this.font, titleStr, (this.imageWidth - this.font.width(titleStr)) / 2, 6, -12566464, false);
      String invStr = Component.translatable("container.inventory").getString();
      graphics.text(this.font, invStr, 8, 16 + this.numRows * 18 + 4, -12566464, false);
   }
}
