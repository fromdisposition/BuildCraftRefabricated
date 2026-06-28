/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.list;

import buildcraft.core.item.ItemList_BC8;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.button.BCButton;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.list.ListHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
//? if >= 1.21.10 {
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiList extends BcScreen<ContainerList> {
   private static final Identifier TEXTURE_BASE = Identifier.parse("buildcraftcore:textures/gui/list_new.png");
   private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0.0, 0.0, 176.0, 191.0);
   private static final GuiIcon ICON_ONE_STACK = new GuiIcon(TEXTURE_BASE, 0.0, 191.0, 20.0, 20.0);
   private static final GuiIcon ICON_HIGHLIGHT = new GuiIcon(TEXTURE_BASE, 176.0, 0.0, 16.0, 16.0);
   private GuiList.ToggleButton[][] toggleButtons;
   private EditBox labelField;
   private final Map<Integer, GuiList.GhostCache> ghostCache = new HashMap<>();

   public GuiList(ContainerList menu, Inventory playerInv, Component title) {
      super(menu, playerInv, title, 176, 191);
   }

   @Override
   protected void initGuiElements() {
      this.mainGui.shownElements.add(new LedgerListMatch(this.mainGui, (ContainerList)this.menu));
      this.mainGui
         .shownElements
         .add(
            new DummyHelpElement(
               new GuiRectangle(10.0, 10.0, 156.0, 12.0).offset(this.mainGui.rootElement),
               new ElementHelpInfo("buildcraft.help.list.label.title", -1980113, "buildcraft.help.list.label.desc")
            )
         );

      for (int line = 0; line < ((ContainerList)this.menu).lines.length; line++) {
         int rowY = 32 + line * 34;
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(8.0, rowY, 160.0, 16.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo("buildcraft.help.list.slots.title", -7811960, "buildcraft.help.list.slots.desc1", "buildcraft.help.list.slots.desc2")
               )
            );
         int btnRowY = rowY + 18;
         int bOffX = 127;
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(bOffX, btnRowY, 14.0, 14.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo("buildcraft.help.list.button.precise.title", -7820545, "buildcraft.help.list.button.precise.desc")
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(bOffX + 14, btnRowY, 14.0, 14.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo(
                     "buildcraft.help.list.button.by_type.title",
                     -17579,
                     "buildcraft.help.list.button.by_type.desc1",
                     "buildcraft.help.list.button.by_type.desc2"
                  )
               )
            );
         this.mainGui
            .shownElements
            .add(
               new DummyHelpElement(
                  new GuiRectangle(bOffX + 28, btnRowY, 14.0, 14.0).offset(this.mainGui.rootElement),
                  new ElementHelpInfo(
                     "buildcraft.help.list.button.by_material.title",
                     -3372801,
                     "buildcraft.help.list.button.by_material.desc1",
                     "buildcraft.help.list.button.by_material.desc2"
                  )
               )
            );
      }

      this.labelField = new EditBox(this.font, this.leftPos + 10, this.topPos + 10, 156, 12, Component.empty());
      this.labelField.setMaxLength(32);
      this.labelField.setBordered(true);
      if (((ContainerList)this.menu).getListItemStack().getItem() instanceof ItemList_BC8 listItem) {
         String name = listItem.getLocationName(((ContainerList)this.menu).getListItemStack());
         if (name != null && !name.isEmpty()) {
            this.labelField.setValue(name);
         }
      }

      this.labelField.setFocused(false);
      this.labelField.setResponder(newText -> ((ContainerList)this.menu).setLabel(newText));
      this.addRenderableWidget(this.labelField);
      this.toggleButtons = new GuiList.ToggleButton[((ContainerList)this.menu).lines.length][3];

      for (int line = 0; line < ((ContainerList)this.menu).lines.length; line++) {
         int bOffX = this.leftPos + 8 + 162 - 42 - 1;
         int bOffY = this.topPos + 32 + line * 34 + 18;

         for (int btn = 0; btn < 3; btn++) {
            int lineIdx = line;
            int btnIdx = btn;
            String letter = btn == 0 ? "P" : (btn == 1 ? "T" : "M");
            String tooltipKey = btn == 0 ? "gui.list.nbt" : (btn == 1 ? "gui.list.metadata" : "gui.list.oredict");
            GuiList.ToggleButton button = new GuiList.ToggleButton(bOffX + btn * 14, bOffY, 14, 14, Component.literal(letter), () -> {
               ((ContainerList)this.menu).switchButton(lineIdx, btnIdx);

               for (int i = 0; i < 3; i++) {
                  this.toggleButtons[lineIdx][i].setToggled(((ContainerList)this.menu).lines[lineIdx].getOption(i));
               }
            });
            button.setToggled(((ContainerList)this.menu).lines[lineIdx].getOption(btnIdx));
            button.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
            this.toggleButtons[line][btn] = button;
            this.addRenderableWidget(button);
         }
      }
   }

   @Override
   protected void drawBackgroundTexture(BCGraphics graphics) {
      ICON_GUI.drawAt(this.mainGui.rootElement);

      for (int i = 0; i < ((ContainerList)this.menu).lines.length; i++) {
         ListHandler.Line line = ((ContainerList)this.menu).lines[i];
         if (line.isOneStackMode()) {
            ICON_ONE_STACK.drawAt(this.leftPos + 6, this.topPos + 30 + i * 34);
            List<ItemStack> examples = this.ghostExamplesFor(i);

            for (int slot = 1; slot < 9; slot++) {
               int x = this.leftPos + 8 + slot * 18;
               int y = this.topPos + 32 + i * 34;
               ICON_HIGHLIGHT.drawAt(x, y);
               int exampleIdx = slot - 1;
               if (exampleIdx < examples.size()) {
                  ItemStack ex = examples.get(exampleIdx);
                  if (!ex.isEmpty()) {
                     graphics.fakeItem(ex, x, y);
                  }
               }
            }
         }
      }
   }

   @Override
   protected void drawTooltipLayer(int mouseX, int mouseY, float partialTick) {
      BCGraphics graphics = GuiIcon.getGuiGraphics();

      for (int line = 0; line < ((ContainerList)this.menu).lines.length; line++) {
         if (((ContainerList)this.menu).lines[line].isOneStackMode()) {
            List<ItemStack> examples = this.ghostExamplesFor(line);

            for (int slot = 1; slot < 9; slot++) {
               int idx = slot - 1;
               if (idx >= examples.size()) {
                  break;
               }

               ItemStack ex = examples.get(idx);
               if (!ex.isEmpty()) {
                  int x = this.leftPos + 8 + slot * 18;
                  int y = this.topPos + 32 + line * 34;
                  if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                     graphics.setTooltipForNextFrame(this.font, ex, mouseX, mouseY);
                     return;
                  }
               }
            }
         }
      }
   }

   private List<ItemStack> ghostExamplesFor(int lineIdx) {
      ListHandler.Line line = ((ContainerList)this.menu).lines[lineIdx];
      long sig = ghostSignature(line);
      GuiList.GhostCache cached = this.ghostCache.get(lineIdx);
      if (cached != null && cached.signature == sig) {
         return cached.shuffled;
      }

      List<ItemStack> all = new ArrayList<>(line.getExamples());
      Collections.shuffle(all);
      this.ghostCache.put(lineIdx, new GuiList.GhostCache(sig, all));
      return all;
   }

   private static long ghostSignature(ListHandler.Line line) {
      ItemStack source = line.getStack(0);
      int itemHash = source.isEmpty() ? 0 : System.identityHashCode(source.getItem());
      int flags = (line.byType ? 1 : 0) | (line.byMaterial ? 2 : 0) | (line.precise ? 4 : 0);
      return (long)itemHash << 8 | flags;
   }

   //? if >= 1.21.10 {
   public boolean keyPressed(KeyEvent event) {
      if (this.labelField != null && this.labelField.isFocused()) {
         if (event.key() == 257 || event.key() == 335) {
            this.setFocused(null);
            return true;
         }

         if (this.minecraft.options.keyInventory.matches(event)) {
            return true;
         }
      }

      return super.keyPressed(event);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean entered) {
      if (this.labelField != null && this.labelField.isFocused() && !this.labelField.isMouseOver(event.x(), event.y())) {
         this.setFocused(null);
      }

      return super.mouseClicked(event, entered);
   }
   //?} else {
   /*public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.labelField != null && this.labelField.isFocused()) {
         if (keyCode == 257 || keyCode == 335) {
            this.setFocused(null);
            return true;
         }

         if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            return true;
         }
      }

      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.labelField != null && this.labelField.isFocused() && !this.labelField.isMouseOver(mouseX, mouseY)) {
         this.setFocused(null);
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }
   *///?}

   private static final class GhostCache {
      final long signature;
      final List<ItemStack> shuffled;

      GhostCache(long signature, List<ItemStack> shuffled) {
         this.signature = signature;
         this.shuffled = shuffled;
      }
   }

   private static class ToggleButton extends BCButton {
      private static final Identifier SPRITE_NORMAL = Identifier.withDefaultNamespace("widget/button");
      private static final Identifier SPRITE_DISABLED = Identifier.withDefaultNamespace("widget/button_disabled");
      private static final Identifier SPRITE_HIGHLIGHTED = Identifier.withDefaultNamespace("widget/button_highlighted");
      private final Runnable onPressAction;
      private boolean toggled;

      ToggleButton(int x, int y, int width, int height, Component message, Runnable onPressAction) {
         super(x, y, width, height, message);
         this.onPressAction = onPressAction;
      }

      //? if >= 1.21.10 {
      public void onPress(InputWithModifiers modifiers) {
         this.onPressAction.run();
      }
      //?} else {
      /*public void onPress() {
         this.onPressAction.run();
      }
      *///?}

      void setToggled(boolean toggled) {
         this.toggled = toggled;
      }

      @Override
      protected void drawButtonContent(BCGraphics graphics, int mouseX, int mouseY, float partialTick) {
         Identifier sprite;
         if (this.toggled) {
            sprite = SPRITE_DISABLED;
         } else if (this.isHoveredOrFocused()) {
            sprite = SPRITE_HIGHLIGHTED;
         } else {
            sprite = SPRITE_NORMAL;
         }

         // Opaque-scaled white (ARGB.white(float) is 1.21.5+; inline int-math works on every node).
         int whiteArgb = (Math.round(this.alpha * 255.0F) & 0xFF) << 24 | 0xFFFFFF;
         graphics.blitSprite(sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight(), whiteArgb);
         this.drawDefaultButtonLabel(graphics);
      }

      //? if >= 1.21.10 {
      public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
         if (this.visible && this.isValidClickButton(event.buttonInfo()) && this.isMouseOver(event.x(), event.y())) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onClick(event, doubleClick);
            return true;
         } else {
            return false;
         }
      }
      //?} else {
      /*public boolean mouseClicked(double mouseX, double mouseY, int button) {
         if (this.visible && this.isValidClickButton(button) && this.isMouseOver(mouseX, mouseY)) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onClick(mouseX, mouseY);
            return true;
         } else {
            return false;
         }
      }
      *///?}
   }
}
