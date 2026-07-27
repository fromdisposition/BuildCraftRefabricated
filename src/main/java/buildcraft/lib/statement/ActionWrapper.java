/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.statement;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.IAction;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionInternalSided;
import buildcraft.api.statements.IActionReceptor;
import buildcraft.api.statements.IActionSingle;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class ActionWrapper extends StatementWrapper implements IActionInternal {
   protected boolean isActive = false;

   public ActionWrapper(IAction delegate, EnumPipePart sourcePart) {
      super(delegate, sourcePart);
   }

   public IAction getDelegate() {
      return (IAction)this.delegate;
   }

   public static ActionWrapper wrap(IStatement statement, Direction side) {
      if (statement == null) {
         return null;
      }

      if (statement instanceof ActionWrapper) {
         return (ActionWrapper)statement;
      }

      if (statement instanceof IActionInternal && side == null) {
         return new ActionWrapper.ActionWrapperInternal((IActionInternal)statement);
      }

      if (statement instanceof IActionInternalSided) {
         if (side == null) {
            throw new NullPointerException("side");
         } else {
            return new ActionWrapper.ActionWrapperInternalSided((IActionInternalSided)statement, side);
         }
      } else if (statement instanceof IActionExternal) {
         if (side == null) {
            throw new NullPointerException("side");
         } else {
            return new ActionWrapper.ActionWrapperExternal((IActionExternal)statement, side);
         }
      } else {
         throw new IllegalArgumentException("Unknown class or interface " + statement.getClass());
      }
   }

   public ActionWrapper[] getPossible() {
      IStatement[] possible = this.delegate.getPossible();
      boolean andSides = this.sourcePart != EnumPipePart.CENTER;
      List<ActionWrapper> list = new ArrayList<>(possible.length + 5);

      for (int i = 0; i < possible.length; i++) {
         list.add(wrap(possible[i], this.sourcePart.face));
      }

      if (andSides) {
         EnumPipePart part = this.sourcePart;

         for (int j = 0; j < 5; j++) {
            int i = j + possible.length;
            part = part.next();
            ActionWrapper action = wrap(this.delegate, part.face);
            list.add(action);
         }
      }

      return list.toArray(new ActionWrapper[0]);
   }

   public void actionDeactivated(IStatementContainer source, IStatementParameter[] parameters) {
      this.isActive = false;
   }

   public static class ActionWrapperExternal extends ActionWrapper {
      public final IActionExternal action;

      public ActionWrapperExternal(IActionExternal action, @Nonnull Direction side) {
         super(action, EnumPipePart.fromFacing(side));
         this.action = action;
      }

      @Override
      public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
         if (!this.isActive || !(this.action instanceof IActionSingle) || !((IActionSingle)this.action).singleActionTick()) {
            BlockEntity neighbourTile = this.getNeighbourTile(source);
            if (neighbourTile != null) {
               this.action.actionActivate(neighbourTile, this.sourcePart.face, source, parameters);
               if (neighbourTile instanceof IActionReceptor receptor) {
                  receptor.actionActivated(this.action, parameters);
               }

               this.isActive = true;
            }
         }
      }
   }

   public static class ActionWrapperInternal extends ActionWrapper {
      public final IActionInternal action;

      public ActionWrapperInternal(IActionInternal action) {
         super(action, EnumPipePart.CENTER);
         this.action = action;
      }

      @Override
      public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
         if (!this.isActive || !(this.action instanceof IActionSingle) || !((IActionSingle)this.action).singleActionTick()) {
            this.action.actionActivate(source, parameters);
            this.isActive = true;
         }
      }
   }

   public static class ActionWrapperInternalSided extends ActionWrapper {
      public final IActionInternalSided action;

      public ActionWrapperInternalSided(IActionInternalSided action, @Nonnull Direction side) {
         super(action, EnumPipePart.fromFacing(side));
         this.action = action;
      }

      @Override
      public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
         if (!this.isActive || !(this.action instanceof IActionSingle) || !((IActionSingle)this.action).singleActionTick()) {
            this.action.actionActivate(this.sourcePart.face, source, parameters);
            this.isActive = true;
         }
      }
   }
}
