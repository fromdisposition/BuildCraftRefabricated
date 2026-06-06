package buildcraft.lib.expression.info;

import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableLong;
import buildcraft.lib.expression.node.value.NodeVariableObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.DoublePredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

public abstract class VariableInfo<N extends IVariableNode> {
   public final N node;
   public VariableInfo.CacheType cacheType = VariableInfo.CacheType.NEVER;
   public boolean setIsComplete = false;

   public VariableInfo(N node) {
      this.node = node;
   }

   @Override
   public String toString() {
      return this.node.toString();
   }

   public abstract Collection<?> getPossibleValues();

   public abstract boolean shouldCacheCurrentValue();

   public abstract int getCurrentOrdinal();

   public enum CacheType {
      NEVER,
      MATCHES_EXP,
      IN_SET,
      ALWAYS;
   }

   public static class VariableInfoBoolean extends VariableInfo<NodeVariableBoolean> {
      public VariableInfo.VariableInfoBoolean.BooleanPossibilities possibleValues = VariableInfo.VariableInfoBoolean.BooleanPossibilities.FALSE_TRUE;

      public VariableInfoBoolean(NodeVariableBoolean node) {
         super(node);
         this.cacheType = VariableInfo.CacheType.ALWAYS;
         this.setIsComplete = true;
      }

      @Override
      public Collection<Boolean> getPossibleValues() {
         return this.possibleValues.possible;
      }

      @Override
      public boolean shouldCacheCurrentValue() {
         switch (this.cacheType) {
            case NEVER:
               return false;
            case MATCHES_EXP:
            case IN_SET:
               switch (this.possibleValues) {
                  case FALSE:
                     return !this.node.value;
                  case TRUE:
                     return this.node.value;
                  default:
                     return true;
               }
            case ALWAYS:
               return true;
            default:
               throw new IllegalStateException("Unknown CacheType " + this.cacheType);
         }
      }

      @Override
      public int getCurrentOrdinal() {
         boolean current = this.node.value;
         switch (this.possibleValues) {
            case FALSE:
               return current ? -1 : 0;
            case TRUE:
               return current ? 0 : -1;
            case FALSE_TRUE:
            default:
               return current ? 1 : 0;
         }
      }

      public enum BooleanPossibilities {
         FALSE(Boolean.FALSE),
         TRUE(Boolean.TRUE),
         FALSE_TRUE(Boolean.FALSE, Boolean.TRUE);

         public final Collection<Boolean> possible;

         BooleanPossibilities(Boolean... possible) {
            this.possible = Arrays.asList(possible);
         }
      }
   }

   public static class VariableInfoDouble extends VariableInfo<NodeVariableDouble> {
      public final List<Double> possibleValues = new ArrayList<>();
      public DoublePredicate shouldCacheFunc = this.possibleValues::contains;

      public VariableInfoDouble(NodeVariableDouble node) {
         super(node);
      }

      @Override
      public Collection<Double> getPossibleValues() {
         return this.possibleValues;
      }

      @Override
      public boolean shouldCacheCurrentValue() {
         switch (this.cacheType) {
            case NEVER:
               return false;
            case MATCHES_EXP:
               return this.shouldCacheFunc.test(this.node.value);
            case IN_SET:
               return this.possibleValues.contains(this.node.value);
            case ALWAYS:
               return true;
            default:
               throw new IllegalStateException("Unknown CacheType " + this.cacheType);
         }
      }

      @Override
      public int getCurrentOrdinal() {
         return this.possibleValues.indexOf(this.node.value);
      }
   }

   public static class VariableInfoLong extends VariableInfo<NodeVariableLong> {
      public final List<Long> possibleValues = new ArrayList<>();
      public LongPredicate shouldCacheFunc = this.possibleValues::contains;

      public VariableInfoLong(NodeVariableLong node) {
         super(node);
      }

      @Override
      public Collection<Long> getPossibleValues() {
         return this.possibleValues;
      }

      @Override
      public boolean shouldCacheCurrentValue() {
         switch (this.cacheType) {
            case NEVER:
               return false;
            case MATCHES_EXP:
               return this.shouldCacheFunc.test(this.node.value);
            case IN_SET:
               return this.possibleValues.contains(this.node.value);
            case ALWAYS:
               return true;
            default:
               throw new IllegalStateException("Unknown CacheType " + this.cacheType);
         }
      }

      @Override
      public int getCurrentOrdinal() {
         return this.possibleValues.indexOf(this.node.value);
      }
   }

   public static class VariableInfoObject<T> extends VariableInfo<NodeVariableObject<T>> {
      public final List<T> possibleValues = new ArrayList<>();
      public Predicate<T> shouldCacheFunc = this.possibleValues::contains;

      public VariableInfoObject(NodeVariableObject<T> node) {
         super(node);
      }

      @Override
      public Collection<?> getPossibleValues() {
         return this.possibleValues;
      }

      @Override
      public boolean shouldCacheCurrentValue() {
         switch (this.cacheType) {
            case NEVER:
               return false;
            case MATCHES_EXP:
               return this.shouldCacheFunc.test(this.node.value);
            case IN_SET:
               return this.possibleValues.contains(this.node.value);
            case ALWAYS:
               return true;
            default:
               throw new IllegalStateException("Unknown CacheType " + this.cacheType);
         }
      }

      @Override
      public int getCurrentOrdinal() {
         return this.possibleValues.indexOf(this.node.value);
      }
   }
}
