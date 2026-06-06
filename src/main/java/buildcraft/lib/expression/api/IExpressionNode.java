package buildcraft.lib.expression.api;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public interface IExpressionNode {
   default IExpressionNode inline() {
      return this;
   }

   String evaluateAsString();

   @FunctionalInterface
   interface INodeBoolean extends IExpressionNode, BooleanSupplier {
      boolean evaluate();

      default IExpressionNode.INodeBoolean inline() {
         return this;
      }

      @Override
      default String evaluateAsString() {
         return Boolean.toString(this.evaluate());
      }

      @Deprecated
      @Override
      default boolean getAsBoolean() {
         return this.evaluate();
      }
   }

   @FunctionalInterface
   interface INodeDouble extends IExpressionNode, DoubleSupplier {
      double evaluate();

      default IExpressionNode.INodeDouble inline() {
         return this;
      }

      @Override
      default String evaluateAsString() {
         return Double.toString(this.evaluate());
      }

      @Deprecated
      @Override
      default double getAsDouble() {
         return this.evaluate();
      }
   }

   @FunctionalInterface
   interface INodeLong extends IExpressionNode, LongSupplier, IntSupplier {
      long evaluate();

      default IExpressionNode.INodeLong inline() {
         return this;
      }

      @Override
      default String evaluateAsString() {
         return Long.toString(this.evaluate());
      }

      @Deprecated
      @Override
      default long getAsLong() {
         return this.evaluate();
      }

      @Deprecated
      @Override
      default int getAsInt() {
         return (int)this.evaluate();
      }
   }

   interface INodeObject<T> extends IExpressionNode, Supplier<T> {
      T evaluate();

      Class<T> getType();

      default IExpressionNode.INodeObject<T> inline() {
         return this;
      }

      @Override
      default String evaluateAsString() {
         return this.evaluate().toString();
      }

      @Deprecated
      @Override
      default T get() {
         return this.evaluate();
      }

      static <T> IExpressionNode.INodeObject<T> create(final Class<T> clazz, final Supplier<T> supplier) {
         return new IExpressionNode.INodeObject<T>() {
            @Override
            public T evaluate() {
               return supplier.get();
            }

            @Override
            public Class<T> getType() {
               return clazz;
            }
         };
      }
   }

   @FunctionalInterface
   interface INodeString extends IExpressionNode.INodeObject<String> {
      @Override
      default Class<String> getType() {
         return String.class;
      }
   }
}
