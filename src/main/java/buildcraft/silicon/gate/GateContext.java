/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gate;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.IStatement;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.statement.StatementContext;
import java.util.List;

public class GateContext<T extends IStatement> implements StatementContext<T> {
   public final List<GateContext.GateGroup<T>> groups;

   public GateContext(List<GateContext.GateGroup<T>> groups) {
      this.groups = groups;
   }

   @Override
   public List<? extends StatementContext.StatementGroup<T>> getAllPossible() {
      return this.groups;
   }

   public static class GateGroup<T extends IStatement> implements StatementContext.StatementGroup<T> {
      public final EnumPipePart part;
      public final List<T> statements;

      public GateGroup(EnumPipePart part, List<T> statements) {
         this.part = part;
         this.statements = statements;
      }

      @Override
      public List<T> getValues() {
         return this.statements;
      }

      @Override
      public ISimpleDrawable getSourceIcon() {
         return null;
      }

      @Override
      public int getLedgerColour() {
         return this.part == EnumPipePart.CENTER ? 0 : ColourUtil.getColourForSide(this.part.face);
      }
   }
}
