/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.statement;

import buildcraft.api.statements.IStatement;
import buildcraft.lib.client.sprite.SpriteNineSliced;
import buildcraft.lib.client.sprite.SpriteRaw;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiElementSimple;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IInteractionElement;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.data.IReference;
import buildcraft.lib.statement.FullStatement;
import buildcraft.lib.statement.StatementContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.resources.Identifier;

public class GuiElementStatement<S extends IStatement> extends GuiElementSimple implements IInteractionElement, IReference<S> {
   public static final Identifier TEXTURE_SELECTOR = Identifier.parse("buildcraftlib:textures/gui/bcr/misc_slots.png");
   public static final GuiIcon SLOT_COLOUR = new GuiIcon(TEXTURE_SELECTOR, 0.0, 0.0, 18.0, 18.0);
   public static final GuiIcon ICON_SLOT_BLOCKED = SLOT_COLOUR.offset(18.0, 0.0);
   public static final GuiIcon ICON_SLOT_NOT_SET = ICON_SLOT_BLOCKED.offset(18.0, 0.0);
   public static final SpriteRaw ICON_SELECT_HOVER = new SpriteRaw(TEXTURE_SELECTOR, 18.0, 18.0, 36.0, 36.0, 256.0);
   public static final SpriteNineSliced SELECTION_HOVER = new SpriteNineSliced(ICON_SELECT_HOVER, 8, 8, 28, 28, 36);
   private final FullStatement<S> ref;
   private final StatementContext<?> ctx;
   private final boolean draw;

   public GuiElementStatement(BuildCraftGui gui, IGuiArea element, FullStatement<S> ref, StatementContext<?> ctx, boolean draw) {
      super(gui, element);
      this.ref = ref;
      this.ctx = ctx;
      this.draw = draw;
   }

   public S get() {
      return this.ref.get();
   }

   public void set(S to) {
      this.ref.set(to);
      this.ref.postSetFromGui(-1);
   }

   public boolean canSet(S value) {
      return this.ref.canInteract && this.ref.canSet(value);
   }

   public S convertToType(Object value) {
      return this.ref.convertToType(value);
   }

   @Override
   public Class<S> getHeldType() {
      return this.ref.getHeldType();
   }

   @Override
   public void addToolTips(List<ToolTip> tooltips) {
      if (this.contains(this.gui.mouse)) {
         S s = this.get();
         if (s != null) {
            tooltips.add(new ToolTip(s.getTooltip()));
         }
      }
   }

   @Override
   public void drawBackground(float partialTicks) {
      if (this.draw) {
         S statement = this.ref.get();
         double x = this.getX();
         double y = this.getY();
         GuiElementStatementSource.drawGuiSlot(statement, x, y);
         if (!this.ref.canInteract) {
         }
      }
   }

   @Override
   public void onMouseClicked(int button) {
      if (this.contains(this.gui.mouse)) {
         if (this.ref.canInteract && button == 0) {
            S s = this.get();
            if (s == null) {
               return;
            }

            List<IStatement> possible = new ArrayList<>();
            Collections.addAll(possible, s.getPossible());
            if (!s.isPossibleOrdered()) {
               List<IStatement> list = new ArrayList<>();
               list.add(null);

               for (IStatement p2 : possible) {
                  if (p2 != null) {
                     list.add(p2);
                  }
               }

               possible = list;
            }

            if (this.ctx != null) {
               possible.removeIf(f -> {
                  for (StatementContext.StatementGroup<?> group : this.ctx.getAllPossible()) {
                     if (group.getValues().contains(f)) {
                        return false;
                     }
                  }

                  return true;
               });
            }

            this.gui.currentMenu = GuiElementStatementVariant.create(this.gui, this, this, possible.toArray(new IStatement[0]));
         }
      }
   }
}
