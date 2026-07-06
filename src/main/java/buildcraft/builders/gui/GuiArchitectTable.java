/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.gui;

import buildcraft.builders.client.render.BlueprintRenderer;
import buildcraft.builders.container.ContainerArchitectTable;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.ClientArchitectPreviews;
import buildcraft.builders.snapshot.ClientSnapshots;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.pos.GuiRectangle;
import net.minecraft.client.gui.components.EditBox;
//? if >= 1.21.10 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiArchitectTable extends BcScreen<ContainerArchitectTable> {
   private static final Identifier TEXTURE_BASE = Identifier.parse("buildcraftbuilders:textures/gui/architect.png");
   private EditBox nameField;
   private int previewRefreshCounter = 0;

   public GuiArchitectTable(ContainerArchitectTable container, Inventory playerInv, Component title) {
      super(container, playerInv, title, 176, heightForSlots(container, 240));
      this.inventoryLabelY = this.imageHeight - 94;
   }

   @Override
   protected void initGuiElements() {
      if (((ContainerArchitectTable)this.menu).tile != null) {
         this.mainGui
            .shownElements
            .add(
               new LedgerOwnership(
                  this.mainGui, () -> ((ContainerArchitectTable)this.menu).tile != null ? ((ContainerArchitectTable)this.menu).tile.getOwner() : null, true
               )
            );
      }

      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(8.0, 8.0, 160.0, 100.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.architect.preview.title", -7811841, "buildcraft.help.architect.preview.desc1", "buildcraft.help.architect.preview.desc2"
               )
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(52.0, 125.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.architect.snapshot_in.title",
                  -7811960,
                  "buildcraft.help.architect.snapshot_in.desc1",
                  "buildcraft.help.architect.snapshot_in.desc2"
               )
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(77.0, 125.0, 22.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.architect.progress.title", -13176, "buildcraft.help.architect.progress.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(111.0, 125.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.architect.snapshot_out.title",
                  -7798904,
                  "buildcraft.help.architect.snapshot_out.desc1",
                  "buildcraft.help.architect.snapshot_out.desc2"
               )
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(8.0, 145.0, 160.0, 12.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.architect.name.title", -1980113, "buildcraft.help.architect.name.desc")
            )
         );
   }

   @Override
   protected void init() {
      super.init();
      this.nameField = new EditBox(this.font, this.leftPos + 8, this.topPos + 145, 160, 12, Component.empty());
      this.nameField.setValue(((ContainerArchitectTable)this.menu).getTileName());
      this.nameField.setFocused(false);
      this.nameField.setResponder(newText -> {
         String trimmed = newText.trim();
         ((ContainerArchitectTable)this.menu).setTileName(trimmed);
         ((ContainerArchitectTable)this.menu).sendMessage(10, buf -> buf.writeUtf(trimmed));
      });
      this.addRenderableWidget(this.nameField);
   }

   //? if >= 1.21.10 {
   public boolean keyPressed(KeyEvent event) {
      if (this.nameField.isFocused()) {
         if (event.key() == 257 || event.key() == 335) {
            this.setFocused(null);
            return true;
         }

         if (event.key() == 256) {
            return super.keyPressed(event);
         }

         if (this.nameField.keyPressed(event) || this.nameField.canConsumeInput()) {
            return true;
         }
      }

      return super.keyPressed(event);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean entered) {
      if (this.nameField.isFocused() && !this.nameField.isMouseOver(event.x(), event.y())) {
         this.setFocused(null);
      }

      return super.mouseClicked(event, entered);
   }
   //?} else {
   /*public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.nameField.isFocused()) {
         if (keyCode == 257 || keyCode == 335) {
            this.setFocused(null);
            return true;
         }

         if (keyCode == 256) {
            return super.keyPressed(keyCode, scanCode, modifiers);
         }

         if (this.nameField.keyPressed(keyCode, scanCode, modifiers) || this.nameField.canConsumeInput()) {
            return true;
         }
      }

      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.nameField.isFocused() && !this.nameField.isMouseOver(mouseX, mouseY)) {
         this.setFocused(null);
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }
   *///?}

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      graphics.blit(TEXTURE_BASE, this.leftPos, this.topPos, 0.0F, 0.0F, 176, 240, 256, 256);
      int total = ((ContainerArchitectTable)this.menu).getSyncedTotal();
      if (total > 0) {
         int progress = ((ContainerArchitectTable)this.menu).getSyncedProgress();
         int progressWidth = Math.min(22, (int)(22.0F * progress / total));
         if (progressWidth > 0) {
            graphics.blit(TEXTURE_BASE, this.leftPos + 77, this.topPos + 125, 0.0F, 240.0F, progressWidth, 16, 256, 256);
         }
      }

      Snapshot snapshot = null;
      ItemStack snapshotStack = ((ContainerArchitectTable)this.menu).getSlot(1).getItem();
      if (snapshotStack.isEmpty()) {
         snapshotStack = ((ContainerArchitectTable)this.menu).getSlot(0).getItem();
      }

      if (!snapshotStack.isEmpty() && snapshotStack.getItem() instanceof ItemSnapshot) {
         Snapshot.Header header = ItemSnapshot.getHeader(snapshotStack);
         if (header != null) {
            snapshot = ClientSnapshots.INSTANCE.getSnapshot(header.key);
         }
      }

      if (snapshot == null && ((ContainerArchitectTable)this.menu).tile != null) {
         snapshot = ClientArchitectPreviews.INSTANCE.get(((ContainerArchitectTable)this.menu).tile.getBlockPos());
      }

      if (snapshot != null) {
         BlueprintRenderer.renderSnapshot(graphics, snapshot, this.leftPos + 8, this.topPos + 8, 160, 100);
      }
   }

   @Override
   protected void containerTick() {
      super.containerTick();
      if (((ContainerArchitectTable)this.menu).tile != null) {
         this.previewRefreshCounter++;
         if (this.previewRefreshCounter >= 40) {
            this.previewRefreshCounter = 0;
            ClientArchitectPreviews.INSTANCE.requestRefresh(((ContainerArchitectTable)this.menu).tile.getBlockPos());
         }
      }
   }

   public void removed() {
      super.removed();
      if (((ContainerArchitectTable)this.menu).tile != null) {
         ClientArchitectPreviews.INSTANCE.invalidate(((ContainerArchitectTable)this.menu).tile.getBlockPos());
      }
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      String titleStr = this.title.getString();
      graphics.text(this.font, titleStr, (this.imageWidth - this.font.width(titleStr)) / 2, 111, -12566464, false);
   }
}
