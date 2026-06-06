package buildcraft.lib.statement;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerExternalOverride;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerInternalSided;
import javax.annotation.Nonnull;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class TriggerWrapper extends StatementWrapper implements ITriggerInternal {
   public TriggerWrapper(IStatement delegate, EnumPipePart sourcePart) {
      super(delegate, sourcePart);
   }

   public static TriggerWrapper wrap(IStatement statement, Direction side) {
      if (statement == null) {
         return null;
      }

      if (statement instanceof TriggerWrapper) {
         return (TriggerWrapper)statement;
      }

      if (statement instanceof ITriggerInternal && side == null) {
         return new TriggerWrapper.TriggerWrapperInternal((ITriggerInternal)statement);
      }

      if (statement instanceof ITriggerInternalSided) {
         if (side == null) {
            throw new NullPointerException("side");
         } else {
            return new TriggerWrapper.TriggerWrapperInternalSided((ITriggerInternalSided)statement, side);
         }
      } else if (statement instanceof ITriggerExternal) {
         if (side == null) {
            throw new NullPointerException("side");
         } else {
            return new TriggerWrapper.TriggerWrapperExternal((ITriggerExternal)statement, side);
         }
      } else {
         throw new IllegalArgumentException("Unknown class or interface " + statement.getClass());
      }
   }

   public TriggerWrapper[] getPossible() {
      IStatement[] possible = this.delegate.getPossible();
      boolean andSides = this.sourcePart != EnumPipePart.CENTER;
      TriggerWrapper[] real = new TriggerWrapper[possible.length + (andSides ? 5 : 0)];

      for (int i = 0; i < possible.length; i++) {
         real[i] = wrap(possible[i], this.sourcePart.face);
      }

      if (andSides) {
         EnumPipePart part = this.sourcePart;

         for (int j = 0; j < 5; j++) {
            int i = j + possible.length;
            part = part.next();
            real[i] = wrap(this.delegate, part.face);
         }
      }

      return real;
   }

   public static class TriggerWrapperExternal extends TriggerWrapper {
      public final ITriggerExternal trigger;

      public TriggerWrapperExternal(ITriggerExternal trigger, @Nonnull Direction side) {
         super(trigger, EnumPipePart.fromFacing(side));
         this.trigger = trigger;
      }

      @Override
      public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
         BlockEntity tile = this.getNeighbourTile(source);
         if (tile == null) {
            return false;
         }

         if (tile instanceof ITriggerExternalOverride override) {
            ITriggerExternalOverride.Result result = override.override(this.sourcePart.face, source, this.trigger, parameters);
            if (result == ITriggerExternalOverride.Result.FALSE) {
               return false;
            }

            if (result == ITriggerExternalOverride.Result.TRUE) {
               return true;
            }
         }

         return this.trigger.isTriggerActive(tile, this.sourcePart.face, source, parameters);
      }
   }

   public static class TriggerWrapperInternal extends TriggerWrapper {
      public final ITriggerInternal trigger;

      public TriggerWrapperInternal(ITriggerInternal trigger) {
         super(trigger, EnumPipePart.CENTER);
         this.trigger = trigger;
      }

      @Override
      public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
         return this.trigger.isTriggerActive(source, parameters);
      }
   }

   public static class TriggerWrapperInternalSided extends TriggerWrapper {
      public final ITriggerInternalSided trigger;

      public TriggerWrapperInternalSided(ITriggerInternalSided trigger, @Nonnull Direction side) {
         super(trigger, EnumPipePart.fromFacing(side));
         this.trigger = trigger;
      }

      @Override
      public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
         return this.trigger.isTriggerActive(this.sourcePart.face, source, parameters);
      }
   }
}
