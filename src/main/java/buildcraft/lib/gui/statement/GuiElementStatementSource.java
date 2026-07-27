/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.statement;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IGuiSlot;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.statement.StatementContext;
import buildcraft.lib.statement.StatementWrapper;
import java.util.List;
import javax.annotation.Nullable;

public class GuiElementStatementSource<S extends IGuiSlot> implements IInteractionElement {
   public final BuildCraftGui gui;
   public final IGuiPosition position;
   public final StatementContext<S> ctx;
   private final boolean left;
   public final GuiElementStatementDrag dragger;

   public GuiElementStatementSource(BuildCraftGui gui, boolean left, StatementContext<S> ctx) {
      this.gui = gui;
      this.left = left;
      this.ctx = ctx;
      if (left) {
         this.position = gui.lowerLeftLedgerPos.offset(() -> -this.getWidth(), 0.0);
         gui.lowerLeftLedgerPos = this.getPosition(1, 1);
      } else {
         this.position = gui.lowerRightLedgerPos;
         gui.lowerRightLedgerPos = this.getPosition(-1, 1);
      }

      GuiElementStatementDrag drag = null;

      for (IGuiElement element : gui.shownElements) {
         if (element instanceof GuiElementStatementDrag) {
            drag = (GuiElementStatementDrag)element;
            break;
         }
      }

      if (drag == null) {
         drag = new GuiElementStatementDrag(gui);
         gui.shownElements.add(drag);
      }

      this.dragger = drag;
   }

   @Override
   public double getX() {
      return this.position.getX();
   }

   @Override
   public double getY() {
      return this.position.getY();
   }

   @Override
   public double getWidth() {
      int width = 0;

      for (StatementContext.StatementGroup<S> group : this.ctx.getAllPossible()) {
         int count = group.getValues().size();
         width = Math.max(width, count);
      }

      return Math.min(4, width) * 18;
   }

   @Override
   public double getHeight() {
      int height = 0;

      for (StatementContext.StatementGroup<S> group : this.ctx.getAllPossible()) {
         int count = group.getValues().size();
         height += (count + 3) / 4;
      }

      return height * 18 + 4;
   }

   private void iterateSlots(GuiElementStatementSource.ISlotIter<S> iter) {
      int dx = this.left ? -1 : 1;
      int sx = this.left ? 3 : 0;
      int ex = sx + dx * 4;
      int x = sx;
      int y = 0;

      for (StatementContext.StatementGroup<S> group : this.ctx.getAllPossible()) {
         int visited = 0;

         for (S slot : group.getValues()) {
            double px = this.getX() + x * 18;
            double py = this.getY() + y * 18;
            iter.iterate(slot, new GuiRectangle(px, py, 18.0, 18.0));
            visited++;
            x += dx;
            if (x == ex) {
               x = sx;
               y++;
            }
         }

         if (visited > 0 && x != sx) {
            x = sx;
            y++;
         }
      }
   }

   @Override
   public void drawBackground(float partialTicks) {
      this.iterateSlots((s, area) -> this.drawAt(s, area.x, area.y));
   }

   private void drawAt(@Nullable S slot, double x, double y) {
      drawGuiSlot(slot, x, y);
   }

   public static void drawGuiSlot(@Nullable IGuiSlot guiSlot, double x, double y) {
      if (guiSlot instanceof IStatementParameter) {
         ParameterRenderer.draw((IStatementParameter)guiSlot, x, y);
      } else {
         GuiIcon background = GuiElementStatement.SLOT_COLOUR;
         if (guiSlot instanceof StatementWrapper) {
            EnumPipePart part = ((StatementWrapper)guiSlot).sourcePart;
            if (part != EnumPipePart.CENTER) {
               background = background.offset(0.0, (1 + part.getIndex()) * 18);
            }
         }

         background.drawAt(x, y);
         if (guiSlot != null) {
            ISprite sprite = guiSlot.getSprite();
            if (sprite != null) {
               GuiIcon.drawAt(sprite, x + 1.0, y + 1.0, 16.0);
            }
         }
      }
   }

   @Override
   public void addToolTips(List<ToolTip> tooltips) {
      this.iterateSlots((slot, area) -> {
         if (slot != null) {
            if (area.contains(this.gui.mouse)) {
               tooltips.add(new ToolTip(slot.getTooltip()));
            }
         }
      });
   }

   @Override
   public void onMouseClicked(int button) {
      if (button == 0) {
         this.iterateSlots((slot, area) -> {
            if (area.contains(this.gui.mouse)) {
               this.dragger.startDragging(slot);
            }
         });
      }
   }

   interface ISlotIter<S extends IGuiSlot> {
      void iterate(@Nullable S var1, GuiRectangle var2);
   }
}
