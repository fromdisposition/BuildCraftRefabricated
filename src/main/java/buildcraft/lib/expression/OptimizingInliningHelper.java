/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.func.NodeFuncBase;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleToDouble;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

public class OptimizingInliningHelper {
   public static IExpressionNode tryOptimizedInline(IExpressionNode node) {
      if (isDoubleMultiply(node)) {
         NodeFuncDoubleDoubleToDouble.FuncDoubleDoubleToDouble n = (NodeFuncDoubleDoubleToDouble.FuncDoubleDoubleToDouble)node;
         IExpressionNode.INodeDouble n1 = n.argA.inline();
         IExpressionNode.INodeDouble n2 = n.argB.inline();
         boolean hasConst = false;
         if (n1 instanceof NodeConstantDouble) {
            if (n2 instanceof NodeConstantDouble) {
               return new NodeConstantDouble(n1.evaluate() * n2.evaluate());
            }

            hasConst = true;
         } else if (n2 instanceof NodeConstantDouble) {
            IExpressionNode.INodeDouble t = n1;
            n1 = n2;
            n2 = t;
            hasConst = true;
         }

         if (hasConst) {
            if (!isDoubleMultiply(n2)) {
               return NodeTypes.DoubleFunctions.MUL.create(n1, n2);
            }

            NodeFuncDoubleDoubleToDouble.FuncDoubleDoubleToDouble mul2 = (NodeFuncDoubleDoubleToDouble.FuncDoubleDoubleToDouble)n2;
            if (mul2.argA instanceof NodeConstantDouble) {
               double c1 = n1.evaluate();
               double c2 = mul2.argA.evaluate();
               return NodeTypes.DoubleFunctions.MUL.create(new NodeConstantDouble(c1 * c2), mul2.argB);
            }
         }
      }

      return null;
   }

   public static boolean isDoubleMultiply(IExpressionNode node) {
      if (node instanceof NodeFuncBase.IFunctionNode) {
         NodeFuncBase base = ((NodeFuncBase.IFunctionNode)node).getFunction();
         if (base == NodeTypes.DoubleFunctions.MUL) {
            return true;
         }
      }

      return false;
   }
}
