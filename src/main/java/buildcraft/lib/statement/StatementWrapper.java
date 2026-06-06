package buildcraft.lib.statement;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IAction;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITrigger;
import buildcraft.lib.misc.ColourUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class StatementWrapper implements IStatement, Comparable<StatementWrapper> {
   public final IStatement delegate;
   public final EnumPipePart sourcePart;

   public StatementWrapper(IStatement delegate, EnumPipePart sourcePart) {
      this.delegate = delegate;
      this.sourcePart = sourcePart;
   }

   @Override
   public String getUniqueTag() {
      return this.delegate.getUniqueTag();
   }

   @Override
   public int maxParameters() {
      return this.delegate.maxParameters();
   }

   @Override
   public int minParameters() {
      return this.delegate.minParameters();
   }

   @Override
   public String getDescription() {
      return this.delegate.getDescription();
   }

   @Override
   public IStatementParameter createParameter(int index) {
      return this.delegate.createParameter(index);
   }

   @Override
   public IStatement rotateLeft() {
      return this.delegate.rotateLeft();
   }

   @Override
   public ISprite getSprite() {
      return this.delegate.getSprite();
   }

   public BlockEntity getNeighbourTile(IStatementContainer source) {
      return source.getNeighbourTile(this.sourcePart.face);
   }

   public abstract StatementWrapper[] getPossible();

   @Override
   public boolean isPossibleOrdered() {
      return this.delegate.isPossibleOrdered();
   }

   @Override
   public List<String> getTooltip() {
      List<String> list = this.delegate.getTooltip();
      if (this.sourcePart != EnumPipePart.CENTER) {
         list = new ArrayList<>(list);
         String translated = ColourUtil.getTextFullTooltip(this.sourcePart.face);
         list.add(I18n.get("gate.side", new Object[]{translated}));
      }

      return list;
   }

   @Override
   public <T> T convertTo(Class<T> clazz) {
      T t = this.delegate.convertTo(clazz);
      if (t != null) {
         return t;
      }

      if (clazz.isAssignableFrom(TriggerWrapper.class)) {
         ITrigger trigger = this.delegate.convertTo(ITrigger.class);
         if (trigger != null) {
            return clazz.cast(TriggerWrapper.wrap(trigger, this.sourcePart.face));
         }
      } else if (clazz.isAssignableFrom(ActionWrapper.class)) {
         IAction action = this.delegate.convertTo(IAction.class);
         if (action != null) {
            return clazz.cast(ActionWrapper.wrap(action, this.sourcePart.face));
         }
      }

      return null;
   }

   public int compareTo(StatementWrapper o) {
      if (this.sourcePart != o.sourcePart) {
         return Integer.compare(o.sourcePart.getIndex(), this.sourcePart.getIndex());
      }

      if (this.delegate == o.delegate) {
         return 0;
      }

      if (this.delegate.getClass() == o.delegate.getClass()) {
         IStatement[] poss = this.delegate.getPossible();
         IStatement[] oPoss = o.delegate.getPossible();
         if (Arrays.equals(poss, oPoss)) {
            int idxThis = -1;
            int idxThat = -1;

            for (int i = 0; i < poss.length; i++) {
               if (poss[i] == this.delegate) {
                  idxThis = i;
               }

               if (poss[i] == o.delegate) {
                  idxThat = i;
               }
            }

            if (idxThis != idxThat && idxThis != -1 && idxThat != -1) {
               return Integer.compare(idxThis, idxThat);
            }
         }
      }

      return this.getUniqueTag().compareTo(o.getUniqueTag());
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.sourcePart, this.getUniqueTag());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (obj.getClass() != this.getClass()) {
         return false;
      }

      StatementWrapper other = (StatementWrapper)obj;
      return this.sourcePart == other.sourcePart && this.getUniqueTag().equals(other.getUniqueTag());
   }
}
