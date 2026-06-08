/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.statement;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.data.IReference;
import buildcraft.lib.statement.FullStatement;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;

public class GuiElementStatementParam extends GuiElementSimple implements IInteractionElement, IReference<IStatementParameter> {
   private final IStatementContainer container;
   private final FullStatement<?> ref;
   private final int paramIndex;
   private final boolean draw;

   public GuiElementStatementParam(BuildCraftGui gui, IGuiArea element, IStatementContainer container, FullStatement<?> ref, int index, boolean draw) {
      super(gui, element);
      this.container = container;
      this.ref = ref;
      this.paramIndex = index;
      this.draw = draw;
   }

   public IStatementParameter get() {
      return this.ref.get(this.paramIndex);
   }

   public void set(IStatementParameter to) {
      this.ref.set(this.paramIndex, to);
      this.ref.postSetFromGui(this.paramIndex);
   }

   public boolean canSet(IStatementParameter value) {
      return this.ref.canSet(this.paramIndex, value);
   }

   @Override
   public Class<IStatementParameter> getHeldType() {
      return IStatementParameter.class;
   }

   @Override
   public void addToolTips(List<ToolTip> tooltips) {
      if (this.contains(this.gui.mouse)) {
         IStatementParameter s = this.get();
         if (s != null) {
            tooltips.add(new ToolTip(s.getTooltip()));
         }
      }
   }

   @Override
   public void drawBackground(float partialTicks) {
      if (this.draw) {
         IStatement slot = this.ref.get();
         int max = slot == null ? 0 : slot.maxParameters();
         double x = this.getX();
         double y = this.getY();
         if (this.paramIndex >= max) {
            GuiElementStatement.SLOT_COLOUR.drawAt(x, y);
            GuiElementStatement.ICON_SLOT_BLOCKED.drawAt(x, y);
            return;
         }

         IStatementParameter statementParameter = this.get();
         GuiElementStatementSource.drawGuiSlot(statementParameter, x, y);
      }
   }

   @Override
   public void onMouseClicked(int button) {
      if (this.ref.canInteract && this.contains(this.gui.mouse) && button == 0) {
         IStatementParameter param = this.get();
         if (param == null) {
            return;
         }

         StatementMouseClick clickEvent = new StatementMouseClick(0, false);
         ItemStack heldStack;
         if (this.gui.gui instanceof AbstractContainerScreen<?> screen) {
            heldStack = screen.getMenu().getCarried();
         } else {
            heldStack = ItemStack.EMPTY;
         }

         IStatementParameter pNew = param.onClick(this.container, this.ref.get(), heldStack, clickEvent);
         if (pNew != null) {
            this.set(pNew);
         } else {
            IStatementParameter[] possible = param.getPossible(this.container);
            if (!param.isPossibleOrdered()) {
               List<IStatementParameter> list = new ArrayList<>();

               for (IStatementParameter p2 : possible) {
                  if (p2 != null) {
                     list.add(p2);
                  }
               }

               possible = list.toArray(new IStatementParameter[0]);
            }

            this.gui.currentMenu = GuiElementStatementVariant.create(this.gui, this, this, possible);
         }
      }
   }
}
