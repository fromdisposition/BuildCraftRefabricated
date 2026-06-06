package buildcraft.lib.expression;

import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import java.util.function.BiFunction;
import java.util.function.Function;

public class NodeInliningHelper {
   public static <F extends IExpressionNode, T extends IExpressionNode> T tryInline(T node, F subNode, Function<F, T> changer, Function<F, T> inlinedChanger) {
      T inlined = (T)OptimizingInliningHelper.tryOptimizedInline(node);
      if (inlined != null) {
         return inlined;
      } else {
         ExpressionDebugManager.debugStart("Inlining " + node);
         inlined = (T)subNode.inline();
         if (inlined instanceof IConstantNode) {
            T to = (T)inlinedChanger.apply((F)inlined);
            ExpressionDebugManager.debugEnd("Fully inlined to " + to);
            return to;
         } else if (inlined == subNode) {
            ExpressionDebugManager.debugEnd("Unable to inline at all!");
            return node;
         } else {
            T to = (T)changer.apply((F)inlined);
            ExpressionDebugManager.debugEnd("Partially inlined to " + to);
            return to;
         }
      }
   }

   public static <L extends IExpressionNode, R extends IExpressionNode, T extends IExpressionNode> T tryInline(
      T node, L subNodeLeft, R subNodeRight, BiFunction<L, R, T> changer, BiFunction<L, R, T> inlinedChanger
   ) {
      T inlined = (T)OptimizingInliningHelper.tryOptimizedInline(node);
      if (inlined != null) {
         return inlined;
      } else {
         ExpressionDebugManager.debugStart("Inlining " + node);
         inlined = (T)subNodeLeft.inline();
         R rightInlined = (R)subNodeRight.inline();
         if (inlined instanceof IConstantNode && rightInlined instanceof IConstantNode) {
            T to = (T)inlinedChanger.apply((L)inlined, rightInlined);
            ExpressionDebugManager.debugEnd("Fully inlined to " + to);
            return to;
         } else if (inlined == subNodeLeft && rightInlined == subNodeRight) {
            ExpressionDebugManager.debugEnd("Unable to inline at all!");
            return node;
         } else {
            T to = (T)changer.apply((L)inlined, rightInlined);
            ExpressionDebugManager.debugEnd("Partially inlined to " + to);
            return to;
         }
      }
   }

   public static <A extends IExpressionNode, B extends IExpressionNode, C extends IExpressionNode, R extends IExpressionNode> R tryInline(
      R node, A nodeA, B nodeB, C nodeC, NodeInliningHelper.TriFunction<A, B, C, R> changer, NodeInliningHelper.TriFunction<A, B, C, R> inlinedChanger
   ) {
      R inlined = (R)OptimizingInliningHelper.tryOptimizedInline(node);
      if (inlined != null) {
         return inlined;
      } else {
         ExpressionDebugManager.debugStart("Inlining " + node);
         inlined = (R)nodeA.inline();
         B inlinedB = (B)nodeB.inline();
         C inlinedC = (C)nodeC.inline();
         if (inlined instanceof IConstantNode && inlinedB instanceof IConstantNode && inlinedC instanceof IConstantNode) {
            R to = (R)inlinedChanger.apply((A)inlined, inlinedB, inlinedC);
            ExpressionDebugManager.debugEnd("Fully inlined to " + to);
            return to;
         } else if (inlined == nodeA && inlinedB == nodeB && inlinedC == nodeC) {
            ExpressionDebugManager.debugEnd("Unable to inline at all!");
            return node;
         } else {
            R to = (R)changer.apply((A)inlined, inlinedB, inlinedC);
            ExpressionDebugManager.debugEnd("Partially inlined to " + to);
            return to;
         }
      }
   }

   public static <A extends IExpressionNode, B extends IExpressionNode, C extends IExpressionNode, D extends IExpressionNode, R extends IExpressionNode> R tryInline(
      R node,
      A nodeA,
      B nodeB,
      C nodeC,
      D nodeD,
      NodeInliningHelper.QuadFunction<A, B, C, D, R> changer,
      NodeInliningHelper.QuadFunction<A, B, C, D, R> inlinedChanger
   ) {
      R inlined = (R)OptimizingInliningHelper.tryOptimizedInline(node);
      if (inlined != null) {
         return inlined;
      } else {
         ExpressionDebugManager.debugStart("Inlining " + node);
         inlined = (R)nodeA.inline();
         B inlinedB = (B)nodeB.inline();
         C inlinedC = (C)nodeC.inline();
         D inlinedD = (D)nodeD.inline();
         if (inlined instanceof IConstantNode && inlinedB instanceof IConstantNode && inlinedC instanceof IConstantNode && inlinedD instanceof IConstantNode) {
            R to = (R)inlinedChanger.apply((A)inlined, inlinedB, inlinedC, inlinedD);
            ExpressionDebugManager.debugEnd("Fully inlined to " + to);
            return to;
         } else if (inlined == nodeA && inlinedB == nodeB && inlinedC == nodeC && inlinedD == nodeD) {
            ExpressionDebugManager.debugEnd("Unable to inline at all!");
            return node;
         } else {
            R to = (R)changer.apply((A)inlined, inlinedB, inlinedC, inlinedD);
            ExpressionDebugManager.debugEnd("Partially inlined to " + to);
            return to;
         }
      }
   }

   public static <A extends IExpressionNode, B extends IExpressionNode, C extends IExpressionNode, D extends IExpressionNode, E extends IExpressionNode, R extends IExpressionNode> R tryInline(
      R node,
      A nodeA,
      B nodeB,
      C nodeC,
      D nodeD,
      E nodeE,
      NodeInliningHelper.PentaFunction<A, B, C, D, E, R> changer,
      NodeInliningHelper.PentaFunction<A, B, C, D, E, R> inlinedChanger
   ) {
      R inlined = (R)OptimizingInliningHelper.tryOptimizedInline(node);
      if (inlined != null) {
         return inlined;
      } else {
         ExpressionDebugManager.debugStart("Inlining " + node);
         inlined = (R)nodeA.inline();
         B inlinedB = (B)nodeB.inline();
         C inlinedC = (C)nodeC.inline();
         D inlinedD = (D)nodeD.inline();
         E inlinedE = (E)nodeE.inline();
         if (inlined instanceof IConstantNode
            && inlinedB instanceof IConstantNode
            && inlinedC instanceof IConstantNode
            && inlinedD instanceof IConstantNode
            && inlinedE instanceof IConstantNode) {
            R to = (R)inlinedChanger.apply((A)inlined, inlinedB, inlinedC, inlinedD, inlinedE);
            ExpressionDebugManager.debugEnd("Fully inlined to " + to);
            return to;
         } else if (inlined == nodeA && inlinedB == nodeB && inlinedC == nodeC && inlinedD == nodeD && inlinedE == nodeE) {
            ExpressionDebugManager.debugEnd("Unable to inline at all!");
            return node;
         } else {
            R to = (R)changer.apply((A)inlined, inlinedB, inlinedC, inlinedD, inlinedE);
            ExpressionDebugManager.debugEnd("Partially inlined to " + to);
            return to;
         }
      }
   }

   public interface PentaFunction<A, B, C, D, E, R> {
      R apply(A var1, B var2, C var3, D var4, E var5);
   }

   public interface QuadFunction<A, B, C, D, R> {
      R apply(A var1, B var2, C var3, D var4);
   }

   public interface TriFunction<A, B, C, R> {
      R apply(A var1, B var2, C var3);
   }
}
