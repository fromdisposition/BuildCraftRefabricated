/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.gui;

import buildcraft.builders.container.ContainerElectronicLibrary;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.pos.GuiRectangle;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiElectronicLibrary extends BcScreen<ContainerElectronicLibrary> {
   private static final Identifier TEXTURE = Identifier.parse("buildcraftbuilders:textures/gui/electronic_library.png");
   private static final int SIZE_X = 244;
   private static final int SIZE_Y = 220;
   private static final int LIST_X = 8;
   private static final int LIST_Y = 22;
   private static final int LIST_W = 154;
   private static final int LIST_ROW_H = 8;
   private static final int LIST_MAX_ROWS = 13;
   private static final int LIST_HELP_X = 7;
   private static final int LIST_HELP_W = 155;
   private static final int LIST_HELP_H = 108;
   private static final int DOWN_OUT_X = 175;
   private static final int DOWN_OUT_Y = 57;
   private static final int DOWN_IN_X = 219;
   private static final int DOWN_IN_Y = 57;
   private static final int UP_IN_X = 175;
   private static final int UP_IN_Y = 79;
   private static final int UP_OUT_X = 219;
   private static final int UP_OUT_Y = 79;
   private static final int ARROW_DOWN_X = 194;
   private static final int ARROW_DOWN_Y = 58;
   private static final int ARROW_UP_X = 194;
   private static final int ARROW_UP_Y = 79;
   private static final int ARROW_W = 22;
   private static final int ARROW_H = 16;
   private static final int FILLED_DOWN_U = 234;
   private static final int FILLED_DOWN_V = 240;
   private static final int FILLED_UP_U = 234;
   private static final int FILLED_UP_V = 224;
   private static final int DEL_X = 174;
   private static final int DEL_Y = 109;
   private static final int DEL_W = 60;
   private static final int DEL_H = 20;
   private Button deleteButton;

   public GuiElectronicLibrary(ContainerElectronicLibrary container, Inventory playerInv, Component title) {
      super(container, playerInv, title, 244, 220);
      this.inventoryLabelY = this.imageHeight - 94;
   }

   @Override
   protected void init() {
      super.init();
      this.deleteButton = Button.builder(Component.translatable("gui.del"), b -> this.onDeletePressed())
         .bounds(this.leftPos + 174, this.topPos + 109, 60, 20)
         .build();
      this.addRenderableWidget(this.deleteButton);
      this.updateDeleteButtonActive();
   }

   @Override
   protected void initGuiElements() {
      if (((ContainerElectronicLibrary)this.menu).tile != null) {
         this.mainGui
            .shownElements
            .add(
               new LedgerOwnership(
                  this.mainGui,
                  () -> ((ContainerElectronicLibrary)this.menu).tile != null ? ((ContainerElectronicLibrary)this.menu).tile.getOwner() : null,
                  true
               )
            );
      }

      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(7.0, 22.0, 155.0, 108.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.library.list.title", -1376, "buildcraft.help.library.list.desc1", "buildcraft.help.library.list.desc2")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(219.0, 57.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.library.download_in.title",
                  -7811960,
                  "buildcraft.help.library.download_in.desc1",
                  "buildcraft.help.library.download_in.desc2"
               )
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(194.0, 58.0, 22.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.library.download_arrow.title", -7811841, "buildcraft.help.library.download_arrow.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(175.0, 57.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.library.download_out.title", -7798904, "buildcraft.help.library.download_out.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(175.0, 79.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.library.upload_in.title", -13176, "buildcraft.help.library.upload_in.desc1", "buildcraft.help.library.upload_in.desc2"
               )
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(194.0, 79.0, 22.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.library.upload_arrow.title", -7820545, "buildcraft.help.library.upload_arrow.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(219.0, 79.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.library.upload_out.title", -3364216, "buildcraft.help.library.upload_out.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(174.0, 109.0, 60.0, 20.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.library.delete.title", -30584, "buildcraft.help.library.delete.desc")
            )
         );
   }

   @Override
   protected void containerTick() {
      super.containerTick();
      this.updateDeleteButtonActive();
   }

   private void updateDeleteButtonActive() {
      if (this.deleteButton != null) {
         Snapshot.Key selected = ((ContainerElectronicLibrary)this.menu).tile != null ? ((ContainerElectronicLibrary)this.menu).tile.selected : null;
         boolean canDelete = selected != null && GlobalSavedDataSnapshots.get(GlobalSavedDataSnapshots.Side.CLIENT).getSnapshot(selected) != null;
         this.deleteButton.active = canDelete;
      }
   }

   private void onDeletePressed() {
      GlobalSavedDataSnapshots clientSnapshots = GlobalSavedDataSnapshots.get(GlobalSavedDataSnapshots.Side.CLIENT);
      Snapshot.Key selected = ((ContainerElectronicLibrary)this.menu).tile != null ? ((ContainerElectronicLibrary)this.menu).tile.selected : null;
      if (selected != null && clientSnapshots.getSnapshot(selected) != null) {
         clientSnapshots.removeSnapshot(selected);
         ((ContainerElectronicLibrary)this.menu).sendSelectedToServer(null);
         if (((ContainerElectronicLibrary)this.menu).tile != null) {
            ((ContainerElectronicLibrary)this.menu).tile.selected = null;
         }

         this.updateDeleteButtonActive();
      }
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
      int progressDown = ((ContainerElectronicLibrary)this.menu).getSyncedProgressDown();
      if (progressDown > 0) {
         int w = Math.min(22, Math.max(1, (int)Math.ceil(22.0F * (progressDown / 50.0F))));
         graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 194 + 22 - w, this.topPos + 58, 256 - w, 240.0F, w, 16, 256, 256);
      }

      int progressUp = ((ContainerElectronicLibrary)this.menu).getSyncedProgressUp();
      if (progressUp > 0) {
         int w = Math.min(22, Math.max(1, (int)Math.ceil(22.0F * (progressUp / 50.0F))));
         graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 194, this.topPos + 79, 234.0F, 224.0F, w, 16, 256, 256);
      }
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      if (graphics != null) {
         GlobalSavedDataSnapshots snapshots = GlobalSavedDataSnapshots.get(GlobalSavedDataSnapshots.Side.CLIENT);
         List<Snapshot.Key> list = snapshots.getList();
         Snapshot.Key selected = ((ContainerElectronicLibrary)this.menu).tile != null ? ((ContainerElectronicLibrary)this.menu).tile.selected : null;
         int rowY = 22;

         for (int i = 0; i < list.size() && i < 13; i++) {
            Snapshot.Key key = list.get(i);
            boolean isSelected = key.equals(selected);
            if (isSelected) {
               graphics.fill(8, rowY, 162, rowY + 8, -2141891243);
            }

            int colour = isSelected ? -1376 : -2039584;
            String text = key.header == null ? key.toString() : key.header.name;
            graphics.text(this.font, text, 8, rowY, colour, false);
            rowY += 8;
         }

         String titleStr = Component.translatable("tile.buildcraftbuilders.library.name").getString();
         graphics.text(this.font, titleStr, (this.imageWidth - this.font.width(titleStr)) / 2, 6, -12566464, false);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      double mouseX = event.x();
      double mouseY = event.y();
      GlobalSavedDataSnapshots snapshots = GlobalSavedDataSnapshots.get(GlobalSavedDataSnapshots.Side.CLIENT);
      List<Snapshot.Key> list = snapshots.getList();
      int rowY = this.topPos + 22;

      for (int i = 0; i < list.size() && i < 13; i++) {
         if (mouseX >= this.leftPos + 8 && mouseX < this.leftPos + 8 + 154 && mouseY >= rowY && mouseY < rowY + 8) {
            Snapshot.Key key = list.get(i);
            ((ContainerElectronicLibrary)this.menu).sendSelectedToServer(key);
            if (((ContainerElectronicLibrary)this.menu).tile != null) {
               ((ContainerElectronicLibrary)this.menu).tile.selected = key;
            }

            this.updateDeleteButtonActive();
            return true;
         }

         rowY += 8;
      }

      return super.mouseClicked(event, doubleClick);
   }
}
