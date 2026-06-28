/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.list;

import buildcraft.api.lists.ListMatchHandler;
import buildcraft.api.lists.ListRegistry;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.ledger.Ledger_Neptune;
import buildcraft.lib.list.ListHandler;
import buildcraft.lib.misc.LocaleUtil;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class LedgerListMatch extends Ledger_Neptune {
   private static final Identifier ICON = Identifier.parse("buildcraftlib:textures/icons/help.png");
   private static final int SLOT_X0 = 8;
   private static final int SLOT_Y0 = 32;
   private static final int SLOT_PITCH_X = 18;
   private static final int SLOT_PITCH_Y = 34;
   private static final int SLOT_SIZE = 16;
   private final ContainerList container;
   private int cachedLine = -2;
   private String cachedSig = "";

   public LedgerListMatch(BuildCraftGui gui, ContainerList container) {
      super(gui, -6697831, false);
      this.container = container;
      this.title = "gui.ledger.list_match";
      this.appendIdleText();
      this.calculateMaxSize();
   }

   @Override
   protected void drawIcon(double x, double y, BCGraphics graphics) {
      graphics.blit(ICON, (int)x, (int)y, 0.0F, 0.0F, 16, 16, 16, 16);
   }

   @Override
   public void drawBackground(float partialTicks) {
      if (this.shouldDrawOpen()) {
         this.updateHoverContent();
      }

      super.drawBackground(partialTicks);
   }

   private void updateHoverContent() {
      int hoveredLine = this.getHoveredLineForSlotZero();
      if (hoveredLine >= 0 && hoveredLine < this.container.lines.length) {
         ListHandler.Line line = this.container.lines[hoveredLine];
         if (!line.isOneStackMode()) {
            this.setIdleIfChanged();
         } else {
            ItemStack exemplar = line.getStack(0);
            if (exemplar.isEmpty()) {
               this.setIdleIfChanged();
            } else {
               String sig = hoveredLine + ":" + System.identityHashCode(exemplar.getItem()) + ":" + line.byType + ":" + line.byMaterial + ":" + line.precise;
               if (!sig.equals(this.cachedSig) || this.cachedLine != hoveredLine) {
                  this.cachedSig = sig;
                  this.cachedLine = hoveredLine;
                  this.clearTextEntries();
                  boolean any = false;
                  if (line.byType) {
                     this.appendText(LocaleUtil.localize("gui.list.match.mode_type"), 16777215).setDropShadow(true);
                     any |= this.appendHandlerDescriptions(ListMatchHandler.Type.TYPE, exemplar);
                  }

                  if (line.byMaterial) {
                     this.appendText(LocaleUtil.localize("gui.list.match.mode_material"), 16777215).setDropShadow(true);
                     any |= this.appendHandlerDescriptions(ListMatchHandler.Type.MATERIAL, exemplar);
                  }

                  if (!any) {
                     this.appendText(LocaleUtil.localize("gui.list.match.no_handlers"), 16755370);
                     this.appendText(LocaleUtil.localize("gui.list.match.no_handlers_hint"), 13421772);
                  }

                  if (line.precise) {
                     this.appendText(LocaleUtil.localize("gui.list.match.precise_inactive"), 16755370);
                  }

                  this.calculateMaxSize();
               }
            }
         }
      } else {
         this.setIdleIfChanged();
      }
   }

   private boolean appendHandlerDescriptions(ListMatchHandler.Type mode, ItemStack exemplar) {
      boolean any = false;

      for (ListMatchHandler handler : ListRegistry.getHandlers()) {
         if (handler.isValidSource(mode, exemplar)) {
            List<String> descriptions = handler.describeMatch(mode, exemplar);
            if (descriptions.isEmpty()) {
               this.appendText("- " + handler.getClass().getSimpleName(), 13421772);
               any = true;
            } else {
               for (String desc : descriptions) {
                  this.appendText("- " + desc, 13421772);
               }

               any = true;
            }
         }
      }

      return any;
   }

   private void setIdleIfChanged() {
      if (this.cachedLine != -1 || !"idle".equals(this.cachedSig)) {
         this.cachedLine = -1;
         this.cachedSig = "idle";
         this.clearTextEntries();
         this.appendIdleText();
         this.calculateMaxSize();
      }
   }

   private void appendIdleText() {
      this.appendText(LocaleUtil.localize("gui.list.match.idle"), 13421772);
   }

   private int getHoveredLineForSlotZero() {
      double mx = this.gui.mouse.getX() - this.gui.rootElement.getX();
      double my = this.gui.mouse.getY() - this.gui.rootElement.getY();
      if (!(mx < 8.0) && !(mx >= 24.0)) {
         for (int line = 0; line < this.container.lines.length; line++) {
            int y = 32 + line * 34;
            if (my >= y && my < y + 16) {
               return line;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }
}
