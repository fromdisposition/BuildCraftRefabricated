/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.gui;

import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.builders.client.render.BlueprintRenderer;
import buildcraft.builders.container.ContainerReplacer;
import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.BuildersNetworkAsync;
import buildcraft.builders.snapshot.ClientSnapshots;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.gui.pos.GuiRectangle;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
//? if >= 1.21.10 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GuiReplacer extends BcScreen<ContainerReplacer> {
   private static final Identifier TEXTURE = Identifier.parse("buildcraftbuilders:textures/gui/replacer.png");
   private EditBox nameField;
   private Button replaceButton;
   private Snapshot.Key lastSeededKey;
   private int cacheFingerprint = Integer.MIN_VALUE;
   private boolean previewCacheValid;
   private boolean summaryCacheValid;
   @Nullable
   private Blueprint cachedPreviewBlueprint;
   @Nullable
   private String cachedSummaryText;
   private volatile int asyncPreviewFingerprint = Integer.MIN_VALUE;
   @Nullable
   private volatile Blueprint asyncPreviewResult;
   private volatile boolean asyncPreviewPending;
   private static final int REPLACE_ANIM_DURATION = 10;
   private int replaceAnimTicks;

   public GuiReplacer(ContainerReplacer container, Inventory playerInv, Component title) {
      super(container, playerInv, title, 176, heightForSlots(container, 256));
      this.inventoryLabelY = this.imageHeight - 94;
   }

   @Override
   protected void initGuiElements() {
      if (((ContainerReplacer)this.menu).tile != null) {
         this.mainGui
            .shownElements
            .add(
               new LedgerOwnership(
                  this.mainGui, () -> ((ContainerReplacer)this.menu).tile != null ? ((ContainerReplacer)this.menu).tile.getOwner() : null, true
               )
            );
      }

      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(8.0, 18.0, 160.0, 95.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.replacer.preview.title", -7811841, "buildcraft.help.replacer.preview.desc1", "buildcraft.help.replacer.preview.desc2"
               )
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(8.0, 120.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.replacer.snapshot.title", -7811960, "buildcraft.help.replacer.snapshot.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(29.0, 122.0, 140.0, 12.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.replacer.name.title", -1980113, "buildcraft.help.replacer.name.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(8.0, 142.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.replacer.from.title", -30584, "buildcraft.help.replacer.from.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(56.0, 142.0, 16.0, 16.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.replacer.to.title", -7798904, "buildcraft.help.replacer.to.desc")
            )
         );
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(80.0, 140.0, 60.0, 20.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo(
                  "buildcraft.help.replacer.replace.title", -3364216, "buildcraft.help.replacer.replace.desc1", "buildcraft.help.replacer.replace.desc2"
               )
            )
         );
   }

   @Override
   protected void init() {
      super.init();
      this.nameField = new EditBox(this.font, this.leftPos + 29, this.topPos + 122, 140, 12, Component.empty());
      this.nameField.setMaxLength(64);
      this.nameField.setValue(((ContainerReplacer)this.menu).getBlueprintName());
      this.nameField.setFocused(false);
      this.lastSeededKey = this.currentBlueprintKey();
      this.addRenderableWidget(this.nameField);
      this.replaceButton = Button.builder(Component.translatable("gui.buildcraft.replacer.replace"), b -> this.onReplacePressed())
         .bounds(this.leftPos + 80, this.topPos + 140, 60, 20)
         .build();
      this.addRenderableWidget(this.replaceButton);
      this.updateReplaceButtonActive();
   }

   private void onReplacePressed() {
      String newName = this.nameField.getValue().trim();
      ((ContainerReplacer)this.menu).sendMessage(10, buf -> buf.writeUtf(newName));
      this.replaceAnimTicks = REPLACE_ANIM_DURATION;
   }

   @Override
   protected void containerTick() {
      super.containerTick();
      Snapshot.Key currentKey = this.currentBlueprintKey();
      boolean keyChanged = !Objects.equals(currentKey, this.lastSeededKey);
      if (keyChanged && this.nameField != null && !this.nameField.isFocused()) {
         this.nameField.setValue(((ContainerReplacer)this.menu).getBlueprintName());
         this.lastSeededKey = currentKey;
      } else if (keyChanged && this.nameField != null) {
         this.lastSeededKey = currentKey;
      }

      if (this.replaceAnimTicks > 0) {
         this.replaceAnimTicks--;
      }

      this.invalidatePreviewCacheIfNeeded();
      this.updateReplaceButtonActive();
   }

   private int computeCacheFingerprint() {
      if (((ContainerReplacer)this.menu).slots.size() < 3) {
         return 0;
      }

      int hash = 1;
      hash = 31 * hash + ((ContainerReplacer)this.menu).getSlot(0).getItem().hashCode();
      hash = 31 * hash + ((ContainerReplacer)this.menu).getSlot(1).getItem().hashCode();
      hash = 31 * hash + ((ContainerReplacer)this.menu).getSlot(2).getItem().hashCode();
      Snapshot.Key key = this.currentBlueprintKey();
      if (key != null && key.hash != null) {
         hash = 31 * hash + Arrays.hashCode(key.hash);
      }

      return hash;
   }

   private void invalidatePreviewCacheIfNeeded() {
      int fingerprint = this.computeCacheFingerprint();
      if (fingerprint != this.cacheFingerprint) {
         this.cacheFingerprint = fingerprint;
         this.previewCacheValid = false;
         this.summaryCacheValid = false;
         this.cachedPreviewBlueprint = null;
         this.cachedSummaryText = null;
         this.asyncPreviewPending = false;
         this.asyncPreviewResult = null;
      }
   }

   private Blueprint getPreviewBlueprint() {
      this.invalidatePreviewCacheIfNeeded();
      if (!this.previewCacheValid) {
         Blueprint blueprint = this.resolveCurrentBlueprint();
         if (blueprint != null && this.canApplyAsyncReplacement()) {
            int fingerprint = this.cacheFingerprint;
            if (this.asyncPreviewPending && this.asyncPreviewFingerprint == fingerprint && this.asyncPreviewResult != null) {
               this.cachedPreviewBlueprint = this.asyncPreviewResult;
            } else {
               this.scheduleAsyncPreview(blueprint, fingerprint);
               this.cachedPreviewBlueprint = blueprint;
            }
         } else {
            this.cachedPreviewBlueprint = blueprint;
         }

         this.previewCacheValid = true;
      }

      return this.cachedPreviewBlueprint;
   }

   private boolean canApplyAsyncReplacement() {
      ItemStack from = ((ContainerReplacer)this.menu).getSlot(1).getItem();
      ItemStack to = ((ContainerReplacer)this.menu).getSlot(2).getItem();
      return !from.isEmpty() && !to.isEmpty() && ItemSchematicSingle.getSchematicSafe(from) != null && ItemSchematicSingle.getSchematicSafe(to) != null;
   }

   private void scheduleAsyncPreview(Blueprint blueprint, int fingerprint) {
      if (!this.asyncPreviewPending || this.asyncPreviewFingerprint != fingerprint) {
         this.asyncPreviewPending = true;
         this.asyncPreviewFingerprint = fingerprint;
         this.asyncPreviewResult = null;
         Blueprint source = blueprint.copy();
         ItemStack fromStack = ((ContainerReplacer)this.menu).getSlot(1).getItem().copy();
         ItemStack toStack = ((ContainerReplacer)this.menu).getSlot(2).getItem().copy();
         BuildersNetworkAsync.runClientDecompress(() -> {
            ISchematicBlock from = ItemSchematicSingle.getSchematicSafe(fromStack);
            ISchematicBlock to = ItemSchematicSingle.getSchematicSafe(toStack);
            Blueprint preview = source;
            if (from != null && to != null) {
               preview = source.copy();
               preview.replace(from, to);
            }

            Blueprint result = preview;
            Minecraft.getInstance().execute(() -> {
               if (this.asyncPreviewFingerprint == fingerprint) {
                  this.asyncPreviewResult = result;
                  this.asyncPreviewPending = false;
                  this.previewCacheValid = false;
               }
            });
         });
      }
   }

   @Nullable
   private String getSummaryText() {
      this.invalidatePreviewCacheIfNeeded();
      if (!this.summaryCacheValid) {
         this.cachedSummaryText = this.buildSummaryTextUncached();
         this.summaryCacheValid = true;
      }

      return this.cachedSummaryText;
   }

   private void updateReplaceButtonActive() {
      if (this.replaceButton != null) {
         this.replaceButton.active = this.canReplace();
      }
   }

   private boolean canReplace() {
      ItemStack snap = ((ContainerReplacer)this.menu).getSlot(0).getItem();
      ItemStack from = ((ContainerReplacer)this.menu).getSlot(1).getItem();
      ItemStack to = ((ContainerReplacer)this.menu).getSlot(2).getItem();
      if (!snap.isEmpty() && !from.isEmpty() && !to.isEmpty()) {
         Snapshot.Header header = ItemSnapshot.getHeader(snap);
         return header == null ? false : ItemSchematicSingle.getSchematicSafe(from) != null && ItemSchematicSingle.getSchematicSafe(to) != null;
      } else {
         return false;
      }
   }

   private Snapshot.Key currentBlueprintKey() {
      if (((ContainerReplacer)this.menu).slots.isEmpty()) {
         return null;
      } else {
         ItemStack snap = ((ContainerReplacer)this.menu).getSlot(0).getItem();
         if (!snap.isEmpty() && snap.getItem() instanceof ItemSnapshot) {
            Snapshot.Header h = ItemSnapshot.getHeader(snap);
            return h == null ? null : h.key;
         } else {
            return null;
         }
      }
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      graphics.blit(TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, 256, 256, 256);
      if (this.replaceAnimTicks > 0) {
         // The server replace is instant, so play a one-shot cosmetic fill sweep over the from->to arrow: source
         // sprite at atlas (176, 0), drawn over the base arrow at GUI (29, 142), revealed left-to-right.
         float t = (REPLACE_ANIM_DURATION - this.replaceAnimTicks) / (float) REPLACE_ANIM_DURATION;
         int progressWidth = Math.min(22, Math.max(1, (int) Math.ceil(22.0F * t)));
         graphics.blit(TEXTURE, this.leftPos + 29, this.topPos + 142, 176.0F, 0.0F, progressWidth, 16, 256, 256);
      }

      Blueprint toRender = this.getPreviewBlueprint();
      if (toRender != null) {
         BlueprintRenderer.renderSnapshot(graphics, toRender, this.leftPos + 8, this.topPos + 18, 160, 95);
      }
   }

   private Blueprint resolveCurrentBlueprint() {
      ItemStack snap = ((ContainerReplacer)this.menu).getSlot(0).getItem();
      if (!snap.isEmpty() && snap.getItem() instanceof ItemSnapshot) {
         Snapshot.Header header = ItemSnapshot.getHeader(snap);
         if (header == null) {
            return null;
         } else {
            return ClientSnapshots.INSTANCE.getSnapshot(header.key) instanceof Blueprint bp ? bp : null;
         }
      } else {
         return null;
      }
   }

   private Blueprint maybeApplyPendingReplacement(Blueprint blueprint) {
      ItemStack fromStack = ((ContainerReplacer)this.menu).getSlot(1).getItem();
      ItemStack toStack = ((ContainerReplacer)this.menu).getSlot(2).getItem();
      if (!fromStack.isEmpty() && !toStack.isEmpty()) {
         ISchematicBlock from = ItemSchematicSingle.getSchematicSafe(fromStack);
         ISchematicBlock to = ItemSchematicSingle.getSchematicSafe(toStack);
         if (from != null && to != null) {
            Blueprint preview = blueprint.copy();
            preview.replace(from, to);
            return preview;
         } else {
            return blueprint;
         }
      } else {
         return blueprint;
      }
   }

   @Override
   protected void drawForegroundLayer() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      if (graphics != null) {
         // Block name, left-anchored at x=8, y=6 (canonical vanilla title anchor); sits in the top band.
         graphics.text(this.font, this.title.getString(), 8, 6, -12566464, false);
         // "Inventory" label at the canonical anchor (12px above the first player row).
         graphics.text(this.font, this.playerInventoryTitle, 8, this.playerInventoryLabelY(), -12566464, false);
         // Replacement summary as a caption bar flush with the bottom of the preview box (it describes the shown
         // blueprint). The preview interior is y 18..113; the translucent bar spans the full width (x 8..168) and
         // sits at the bottom, light text centred over it.
         String summary = this.getSummaryText();
         if (summary != null) {
            graphics.fill(8, 102, 168, 114, -2013265920);
            int sx = 8 + (160 - this.font.width(summary)) / 2;
            graphics.text(this.font, summary, sx, 104, -1, false);
         }
      }
   }

   @Nullable
   private String buildSummaryTextUncached() {
      ItemStack fromStack = ((ContainerReplacer)this.menu).getSlot(1).getItem();
      ItemStack toStack = ((ContainerReplacer)this.menu).getSlot(2).getItem();
      if (!fromStack.isEmpty() && !toStack.isEmpty()) {
         ISchematicBlock from = ItemSchematicSingle.getSchematicSafe(fromStack);
         ISchematicBlock to = ItemSchematicSingle.getSchematicSafe(toStack);
         if (from != null && to != null) {
            Blueprint blueprint = this.resolveCurrentBlueprint();
            if (blueprint == null) {
               return null;
            }

            int count = blueprint.countMatchingCells(from);
            String fromName = schematicDisplayName(from);
            String toName = schematicDisplayName(to);
            return Component.translatable("gui.buildcraft.replacer.summary", new Object[]{count, fromName, toName}).getString();
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static String schematicDisplayName(ISchematicBlock schematic) {
      if (schematic == null) {
         return "?";
      }

      BlockState state = schematic.getBlockStateForRender();
      if (state == null) {
         return "?";
      }

      Block block = state.getBlock();
      return block == null ? "?" : block.getName().getString();
   }

   //? if >= 1.21.10 {
   public boolean keyPressed(KeyEvent event) {
      if (this.nameField != null && this.nameField.isFocused()) {
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
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (this.nameField != null && this.nameField.isFocused() && !this.nameField.isMouseOver(event.x(), event.y())) {
         this.setFocused(null);
      }

      return super.mouseClicked(event, doubleClick);
   }
   //?} else {
   /*public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.nameField != null && this.nameField.isFocused()) {
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
      if (this.nameField != null && this.nameField.isFocused() && !this.nameField.isMouseOver(mouseX, mouseY)) {
         this.setFocused(null);
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }
   *///?}
}
