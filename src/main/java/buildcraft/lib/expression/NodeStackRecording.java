package buildcraft.lib.expression;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.expression.node.value.NodeConstantObject;
import java.util.ArrayList;
import java.util.List;

public class NodeStackRecording implements INodeStack {
   public final List<Class<?>> types = new ArrayList<>();

   public Class<?>[] toArray() {
      return this.types.toArray(new Class[0]);
   }

   @Override
   public IExpressionNode.INodeLong popLong() throws InvalidExpressionException {
      this.types.add(long.class);
      return NodeConstantLong.ZERO;
   }

   @Override
   public IExpressionNode.INodeDouble popDouble() throws InvalidExpressionException {
      this.types.add(double.class);
      return NodeConstantDouble.ZERO;
   }

   @Override
   public IExpressionNode.INodeBoolean popBoolean() throws InvalidExpressionException {
      this.types.add(boolean.class);
      return NodeConstantBoolean.FALSE;
   }

   @Override
   public <T> IExpressionNode.INodeObject<T> popObject(Class<T> type) throws InvalidExpressionException {
      this.types.add(type);
      NodeType<T> nodeType = NodeTypes.getType(type);
      if (nodeType == null) {
         throw new IllegalStateException("Unknown " + type);
      } else {
         return new NodeConstantObject<>(type, nodeType.defaultValue);
      }
   }
}
