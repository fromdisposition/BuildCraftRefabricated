/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.statement;

import buildcraft.api.statements.IGuiSlot;
import buildcraft.lib.client.sprite.SpriteNineSliced;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IMenuElement;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.data.IReference;
import java.util.Arrays;
import java.util.List;

public class GuiElementStatementVariant extends GuiElementSimple implements IMenuElement {
   public static final SpriteNineSliced SELECTION_HOVER = GuiElementStatement.SELECTION_HOVER;
   private static final int[][] OFFSET_HOVER = new int[][]{
      {0, 0},
      {-1, -1},
      {0, -1},
      {1, -1},
      {1, 0},
      {1, 1},
      {0, 1},
      {-1, 1},
      {-1, 0},
      {-2, -2},
      {-1, -2},
      {0, -2},
      {1, -2},
      {2, -2},
      {2, -1},
      {2, 0},
      {2, 1},
      {2, 2},
      {1, 2},
      {0, 2},
      {-1, 2},
      {-2, 2},
      {-2, 1},
      {-2, 0},
      {-2, -1}
   };
   private final IReference<? extends IGuiSlot> ref;
   private final IGuiSlot[] possible;
   private final IGuiArea[] posPossible;

   public GuiElementStatementVariant(BuildCraftGui gui, IGuiArea element, IReference<? extends IGuiSlot> ref, IGuiSlot[] possible, IGuiArea[] posPossible) {
      super(gui, element);
      this.ref = ref;
      this.possible = possible;
      this.posPossible = posPossible;
   }

   public static GuiElementStatementVariant create(BuildCraftGui gui, IGuiArea parent, IReference<? extends IGuiSlot> ref, IGuiSlot[] possible) {
      int count = Math.min(OFFSET_HOVER.length, possible.length);
      possible = possible.length == count ? possible : Arrays.copyOf(possible, count);
      IGuiArea[] posPossible = new IGuiArea[count];
      IGuiArea base = new GuiRectangle(18.0, 18.0).offset(parent);

      for (int i = 0; i < count; i++) {
         posPossible[i] = base.offset(OFFSET_HOVER[i][0] * 18, OFFSET_HOVER[i][1] * 18);
      }

      int sub = 18 * (count > 9 ? 2 : 1);
      int add = 18 * (count > 9 ? 3 : 1);
      int offset = -sub - 4;
      int size = 8 + add + 36;
      IGuiArea area = new GuiRectangle(offset, offset, size, size).offset(parent);
      return new GuiElementStatementVariant(gui, area, ref, possible, posPossible);
   }

   private void iteratePossible(GuiElementStatementVariant.ISlotIter iter) {
      for (int p = 0; p < this.possible.length; p++) {
         IGuiSlot slot = this.possible[p];
         if (slot != null) {
            iter.iterate(this.posPossible[p], slot);
         }
      }
   }

   @Override
   public void drawBackground(float partialTicks) {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      SELECTION_HOVER.draw(this);
      this.iteratePossible((pos, slot) -> {
         double x = pos.getX();
         double y = pos.getY();
         GuiElementStatementSource.drawGuiSlot(slot, x, y);
      });
   }

   @Override
   public void drawForeground(float partialTicks) {
   }

   @Override
   public void addToolTips(List<ToolTip> tooltips) {
      this.iteratePossible((pos, slot) -> {
         if (pos.contains(this.gui.mouse)) {
            tooltips.add(new ToolTip(slot.getTooltip()));
         }
      });
   }

   @Override
   public void onMouseReleased(int button) {
      this.gui.currentMenu = null;
      this.iteratePossible((pos, slot) -> {
         if (pos.contains(this.gui.mouse)) {
            this.ref.setIfCan(slot);
         }
      });
   }

   interface ISlotIter {
      void iterate(IGuiArea var1, IGuiSlot var2);
   }
}
