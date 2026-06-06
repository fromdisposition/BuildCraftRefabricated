package buildcraft.lib.expression.api;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.VecDouble;
import buildcraft.lib.expression.VecLong;
import buildcraft.lib.expression.node.cast.NodeCasting;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncLongToDouble;
import buildcraft.lib.expression.node.func.NodeFuncLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncLongToObject;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.expression.node.value.NodeConstantObject;
import buildcraft.lib.expression.node.value.NodeVariable;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableLong;
import buildcraft.lib.expression.node.value.NodeVariableObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class NodeTypes {
   public static final FunctionContext LONG = NodeTypes.LongFunctions.LONG;
   public static final FunctionContext DOUBLE = NodeTypes.DoubleFunctions.DOUBLE;
   public static final FunctionContext BOOLEAN = new FunctionContext("Type: Boolean");
   public static final NodeType<String> STRING = new NodeType<>("String", "");
   public static final NodeType<VecLong> VEC_LONG = new NodeType<>("Long Vector", VecLong.ZERO);
   public static final NodeType<VecDouble> VEC_DOUBLE = new NodeType<>("Double Vector", VecDouble.ZERO);
   public static final NodeType<IExpressionNode.INodeLong> NODE_LONG = new NodeType<>("Long Node", IExpressionNode.INodeLong.class, NodeConstantLong.ZERO);
   public static final NodeType<IExpressionNode.INodeDouble> NODE_DOUBLE = new NodeType<>(
      "Double Node", IExpressionNode.INodeDouble.class, NodeConstantDouble.ZERO
   );
   public static final NodeType<IExpressionNode.INodeBoolean> NODE_BOOLEAN = new NodeType<>(
      "Boolean Node", IExpressionNode.INodeBoolean.class, NodeConstantBoolean.FALSE
   );
   private static final Map<String, Class<?>> typesByName = new HashMap<>();
   private static final Map<Class<?>, String> namesByType = new HashMap<>();
   public static final Map<Class<?>, NodeType<?>> typesByClass = new HashMap<>();

   private static String functionCharAt(String str, long index) {
      return functionSubstringRelative(str, index, 1L);
   }

   private static String functionSubstring(String str, long indexStart, long indexEnd) {
      if (indexStart >= indexEnd) {
         return "";
      } else if (indexStart < 0L) {
         return "";
      } else {
         return indexEnd >= str.length() ? "" : str.substring((int)indexStart, (int)indexEnd);
      }
   }

   private static String functionSubstringRelative(String str, long indexStart, long length) {
      return functionSubstring(str, indexStart, indexStart + length);
   }

   public static Class<?> getType(String name) {
      return typesByName.get(name.toLowerCase(Locale.ROOT));
   }

   public static Class<?> parseType(String type) throws InvalidExpressionException {
      Class<?> clazz = getType(type);
      if (clazz != null) {
         return clazz;
      } else {
         throw new InvalidExpressionException("Unknown type " + type + ", must be one of " + typesByName.keySet());
      }
   }

   @SuppressWarnings("unchecked")
   public static <T> NodeType<T> getType(Class<T> clazz) {
      return (NodeType<T>)typesByClass.get(clazz);
   }

   public static String getName(Class<?> clazz) {
      return namesByType.get(clazz);
   }

   public static Collection<String> getValidTypeNames() {
      return typesByName.keySet();
   }

   public static FunctionContext getContext(Class<?> clazz) {
      if (clazz == long.class) {
         return LONG;
      } else if (clazz == double.class) {
         return DOUBLE;
      } else {
         return clazz == boolean.class ? BOOLEAN : typesByClass.get(clazz);
      }
   }

   public static <T> void addType(NodeType<T> type) {
      addType(type.name, type);
   }

   public static <T> void addType(String key, NodeType<T> type) {
      key = key.toLowerCase(Locale.ROOT);
      namesByType.put(type.type, key);
      typesByName.put(key, type.type);
      typesByClass.put(type.type, type);
   }

   public static Class<?> getType(IExpressionNode node) {
      if (node instanceof IExpressionNode.INodeObject) {
         return ((IExpressionNode.INodeObject)node).getType();
      } else if (node instanceof IExpressionNode.INodeLong) {
         return long.class;
      } else if (node instanceof IExpressionNode.INodeDouble) {
         return double.class;
      } else if (node instanceof IExpressionNode.INodeBoolean) {
         return boolean.class;
      } else {
         throw new IllegalArgumentException("Illegal node " + node.getClass());
      }
   }

   public static Class<?> getType(INodeFunc node) {
      if (node instanceof INodeFunc.INodeFuncObject) {
         return ((INodeFunc.INodeFuncObject)node).getType();
      } else if (node instanceof INodeFunc.INodeFuncLong) {
         return long.class;
      } else if (node instanceof INodeFunc.INodeFuncDouble) {
         return double.class;
      } else if (node instanceof INodeFunc.INodeFuncBoolean) {
         return boolean.class;
      } else {
         throw new IllegalArgumentException("Illegal node " + node.getClass());
      }
   }

   public static NodeVariable makeVariableNode(Class<?> type, String name) {
      if (type == long.class) {
         return new NodeVariableLong(name);
      } else if (type == double.class) {
         return new NodeVariableDouble(name);
      } else {
         return type == boolean.class ? new NodeVariableBoolean(name) : new NodeVariableObject<>(name, type);
      }
   }

   public static IConstantNode createConstantNode(IExpressionNode node) {
      if (node instanceof IExpressionNode.INodeLong) {
         return new NodeConstantLong(((IExpressionNode.INodeLong)node).evaluate());
      } else if (node instanceof IExpressionNode.INodeDouble) {
         return new NodeConstantDouble(((IExpressionNode.INodeDouble)node).evaluate());
      } else if (node instanceof IExpressionNode.INodeBoolean) {
         return NodeConstantBoolean.of(((IExpressionNode.INodeBoolean)node).evaluate());
      } else if (node instanceof IExpressionNode.INodeObject<?> nodeObj) {
         return createConstantObject(nodeObj);
      } else {
         throw new IllegalArgumentException("Illegal node " + node.getClass());
      }
   }

   private static <T> IConstantNode createConstantObject(IExpressionNode.INodeObject<T> nodeObj) {
      return new NodeConstantObject<>(nodeObj.getType(), nodeObj.evaluate());
   }

   public static IExpressionNode cast(IExpressionNode node, Class<?> to) throws InvalidExpressionException {
      if (to == double.class) {
         return NodeCasting.castToDouble(node);
      }

      if (to == String.class) {
         return NodeCasting.castToString(node);
      }

      if (to == long.class) {
         if (node instanceof IExpressionNode.INodeLong) {
            return node;
         } else {
            throw new InvalidExpressionException("Cannot cast " + getType(node) + " to a long");
         }
      } else if (to == boolean.class) {
         if (node instanceof IExpressionNode.INodeBoolean) {
            return node;
         } else {
            throw new InvalidExpressionException("Cannot cast " + getType(node) + " to a boolean");
         }
      } else {
         throw new IllegalStateException("Unknown node type '" + to + "'");
      }
   }

   static {
      typesByName.put("long", long.class);
      typesByName.put("int", long.class);
      typesByName.put("double", double.class);
      typesByName.put("float", double.class);
      typesByName.put("boolean", boolean.class);
      typesByName.put("bool", boolean.class);
      namesByType.put(long.class, "long");
      namesByType.put(double.class, "double");
      namesByType.put(boolean.class, "boolean");
      addType("String", STRING);
      addType("VecLong", VEC_LONG);
      addType("VecDouble", VEC_DOUBLE);
      addType("NodeLong", NODE_LONG);
      addType("NodeDouble", NODE_DOUBLE);
      addType("NodeBoolean", NODE_BOOLEAN);
      BOOLEAN.put_b_b("!", a -> !a, a -> "!" + a);
      BOOLEAN.put_bb_b("^", (a, b) -> a ^ b, (a, b) -> "(" + a + "^" + b + ")");
      BOOLEAN.put_bb_b("&", (a, b) -> a & b, (a, b) -> "(" + a + "&" + b + ")");
      BOOLEAN.put_bb_b("|", (a, b) -> a | b, (a, b) -> "(" + a + "|" + b + ")");
      BOOLEAN.put_bb_b("&&", (a, b) -> a && b, (a, b) -> "(" + a + "&&" + b + ")");
      BOOLEAN.put_bb_b("||", (a, b) -> a || b, (a, b) -> "(" + a + "||" + b + ")");
      BOOLEAN.put_bb_b("==", (a, b) -> a == b, (a, b) -> "(" + a + "==" + b + ")");
      BOOLEAN.put_bb_b("!=", (a, b) -> a != b, (a, b) -> "(" + a + "!=" + b + ")");
      BOOLEAN.put_b_o("(string)", String.class, a -> a + "", a -> "((string) " + a + ")");
      STRING.put_tt_t("+", (a, b) -> a + b, (a, b) -> "(" + a + " + " + b + ")");
      STRING.put_tt_t("&", (a, b) -> a + b, (a, b) -> "(" + a + " + " + b + ")");
      STRING.put_tt_b("<", (a, b) -> a.compareTo(b) < 0, (a, b) -> "(" + a + " < " + b + ")");
      STRING.put_tt_b(">", (a, b) -> a.compareTo(b) > 0, (a, b) -> "(" + a + " > " + b + ")");
      STRING.put_tt_b("==", (a, b) -> Objects.equals(a, b), (a, b) -> "(" + a + " == " + b + ")");
      STRING.put_tt_b("!=", (a, b) -> !Objects.equals(a, b), (a, b) -> "(" + a + " != " + b + ")");
      STRING.put_tt_b("<=", (a, b) -> a.compareTo(b) <= 0, (a, b) -> "(" + a + " <= " + b + ")");
      STRING.put_tt_b(">=", (a, b) -> a.compareTo(b) >= 0, (a, b) -> "(" + a + " >= " + b + ")");
      STRING.put_t_l("length", String::length, a -> a + ".length()");
      STRING.put_t_t("toLowerCase", a -> a.toLowerCase(Locale.ROOT), a -> a + ".toLowerCase()");
      STRING.put_t_t("toUpperCase", a -> a.toUpperCase(Locale.ROOT), a -> a + ".toUpperCase()");
      STRING.put_tl_t("char_at", NodeTypes::functionCharAt);
      STRING.put_tll_t("substring", NodeTypes::functionSubstring);
      STRING.put_tll_t("substring_rel", NodeTypes::functionSubstringRelative);
      VEC_LONG.putConstant("ZERO", VecLong.ZERO);
      VEC_LONG.put_l_t("vec", VecLong::new);
      VEC_LONG.put_ll_t("vec", VecLong::new);
      VEC_LONG.put_lll_t("vec", VecLong::new);
      VEC_LONG.put_llll_t("vec", VecLong::new);
      VEC_LONG.put_t_o("(VecDouble)", VecDouble.class, VecLong::castToDouble);
      VEC_LONG.put_tt_t("+", VecLong::add);
      VEC_LONG.put_tt_t("-", VecLong::sub);
      VEC_LONG.put_tt_t("*", VecLong::scale);
      VEC_LONG.put_tt_t("/", VecLong::div);
      VEC_LONG.put_tt_t("cross", VecLong::crossProduct);
      VEC_LONG.put_tt_d("distanceTo", VecLong::distance);
      VEC_LONG.put_tt_l("dot2", VecLong::dotProduct2);
      VEC_LONG.put_tt_l("dot3", VecLong::dotProduct3);
      VEC_LONG.put_tt_l("dot4", VecLong::dotProduct4);
      VEC_LONG.put_t_d("length", VecLong::length);
      VEC_LONG.put_t_o("(string)", String.class, VecLong::toString);
      VEC_DOUBLE.put_d_t("vec", VecDouble::new);
      VEC_DOUBLE.put_dd_t("vec", VecDouble::new);
      VEC_DOUBLE.put_ddd_t("vec", VecDouble::new);
      VEC_DOUBLE.put_dddd_t("vec", VecDouble::new);
      VEC_DOUBLE.put_tt_t("+", VecDouble::add);
      VEC_DOUBLE.put_tt_t("-", VecDouble::sub);
      VEC_DOUBLE.put_tt_t("*", VecDouble::scale);
      VEC_DOUBLE.put_tt_t("/", VecDouble::div);
      VEC_DOUBLE.put_tt_t("cross", VecDouble::crossProduct);
      VEC_DOUBLE.put_tt_d("distanceTo", VecDouble::distance);
      VEC_DOUBLE.put_tt_d("dot2", VecDouble::dotProduct2);
      VEC_DOUBLE.put_tt_d("dot3", VecDouble::dotProduct3);
      VEC_DOUBLE.put_tt_d("dot4", VecDouble::dotProduct4);
      VEC_DOUBLE.put_t_d("length", VecDouble::length);
      VEC_DOUBLE.put_t_o("(string)", String.class, Object::toString);
   }

   public static class DoubleFunctions {
      public static final FunctionContext DOUBLE = new FunctionContext("Type: Double");
      public static final NodeFuncDoubleToDouble NEGATE = DOUBLE.put_d_d("-", a -> -a, a -> "-(" + a + ")");
      public static final NodeFuncDoubleDoubleToDouble ADD = DOUBLE.put_dd_d("+", (a, b) -> a + b, (a, b) -> "(" + a + " + " + b + ")");
      public static final NodeFuncDoubleDoubleToDouble SUB = DOUBLE.put_dd_d("-", (a, b) -> a - b, (a, b) -> "(" + a + " - " + b + ")");
      public static final NodeFuncDoubleDoubleToDouble MUL = DOUBLE.put_dd_d("*", (a, b) -> a * b, (a, b) -> "(" + a + " * " + b + ")");
      public static final NodeFuncDoubleDoubleToDouble DIV = DOUBLE.put_dd_d("/", (a, b) -> a / b, (a, b) -> "(" + a + " / " + b + ")");
      public static final NodeFuncDoubleDoubleToDouble MOD = DOUBLE.put_dd_d("%", (a, b) -> a % b, (a, b) -> "(" + a + " % " + b + ")");
      public static NodeFuncDoubleDoubleToBoolean LT = DOUBLE.put_dd_b("<", (a, b) -> a < b, (a, b) -> "(" + a + " < " + b + ")");
      public static NodeFuncDoubleDoubleToBoolean GT = DOUBLE.put_dd_b(">", (a, b) -> a > b, (a, b) -> "(" + a + " > " + b + ")");
      public static NodeFuncDoubleDoubleToBoolean LE = DOUBLE.put_dd_b("<=", (a, b) -> a <= b, (a, b) -> "(" + a + " <= " + b + ")");
      public static NodeFuncDoubleDoubleToBoolean GE = DOUBLE.put_dd_b(">=", (a, b) -> a >= b, (a, b) -> "(" + a + " >= " + b + ")");
      public static NodeFuncDoubleDoubleToBoolean EQ = DOUBLE.put_dd_b("==", (a, b) -> a == b, (a, b) -> "(" + a + " == " + b + ")");
      public static NodeFuncDoubleDoubleToBoolean NE = DOUBLE.put_dd_b("!=", (a, b) -> a != b, (a, b) -> "(" + a + " != " + b + ")");
      public static NodeFuncDoubleToObject<String> CVT_STRING = DOUBLE.put_d_o("(string)", String.class, a -> a + "", a -> "((string) " + a + ")");
   }

   public static class LongFunctions {
      public static final FunctionContext LONG = new FunctionContext("Type: Long");
      public static final NodeFuncLongToLong NEGATE = LONG.put_l_l("-", a -> -a, a -> "-(" + a + ")");
      public static final NodeFuncLongToLong BITWISE_INVERT = LONG.put_l_l("~", a -> ~a, a -> "~(" + a + ")");
      public static final NodeFuncLongLongToLong ADD = LONG.put_ll_l("+", (a, b) -> a + b, (a, b) -> "(" + a + " + " + b + ")");
      public static final NodeFuncLongLongToLong SUB = LONG.put_ll_l("-", (a, b) -> a - b, (a, b) -> "(" + a + " - " + b + ")");
      public static final NodeFuncLongLongToLong MUL = LONG.put_ll_l("*", (a, b) -> a * b, (a, b) -> "(" + a + " * " + b + ")");
      public static final NodeFuncLongLongToLong DIV = LONG.put_ll_l("/", (a, b) -> a / b, (a, b) -> "(" + a + " / " + b + ")");
      public static final NodeFuncLongLongToLong MOD = LONG.put_ll_l("%", (a, b) -> a % b, (a, b) -> "(" + a + " % " + b + ")");
      public static final NodeFuncLongLongToLong BITWISE_XOR = LONG.put_ll_l("^", (a, b) -> a ^ b, (a, b) -> "(" + a + " ^ " + b + ")");
      public static final NodeFuncLongLongToLong BITWISE_AND = LONG.put_ll_l("&", (a, b) -> a & b, (a, b) -> "(" + a + " & " + b + ")");
      public static final NodeFuncLongLongToLong BITWISE_OR = LONG.put_ll_l("|", (a, b) -> a | b, (a, b) -> "(" + a + " | " + b + ")");
      public static NodeFuncLongLongToBoolean LT = LONG.put_ll_b("<", (a, b) -> a < b, (a, b) -> "(" + a + " < " + b + ")");
      public static NodeFuncLongLongToBoolean GT = LONG.put_ll_b(">", (a, b) -> a > b, (a, b) -> "(" + a + " > " + b + ")");
      public static NodeFuncLongLongToBoolean LE = LONG.put_ll_b("<=", (a, b) -> a <= b, (a, b) -> "(" + a + " <= " + b + ")");
      public static NodeFuncLongLongToBoolean GE = LONG.put_ll_b(">=", (a, b) -> a >= b, (a, b) -> "(" + a + " >= " + b + ")");
      public static NodeFuncLongLongToBoolean EQ = LONG.put_ll_b("==", (a, b) -> a == b, (a, b) -> "(" + a + " == " + b + ")");
      public static NodeFuncLongLongToBoolean NE = LONG.put_ll_b("!=", (a, b) -> a != b, (a, b) -> "(" + a + " != " + b + ")");
      public static NodeFuncLongLongToLong BITSHIFT_UP = LONG.put_ll_l("<<", (a, b) -> a << (int)b, (a, b) -> "(" + a + " << " + b + ")");
      public static NodeFuncLongLongToLong BITSHIFT_DOWN = LONG.put_ll_l(">>", (a, b) -> a >> (int)b, (a, b) -> "(" + a + " >> " + b + ")");
      public static NodeFuncLongLongToLong BITSHIFT_DOWN_HARD = LONG.put_ll_l(">>>", (a, b) -> a >>> (int)b, (a, b) -> a + " >>> " + b);
      public static NodeFuncLongToDouble CVT_DOUBLE = LONG.put_l_d("(double)", a -> a, a -> "((double) " + a + ")");
      public static NodeFuncLongToObject<String> CVT_STRING = LONG.put_l_o("(string)", String.class, a -> a + "", a -> "((string) " + a + ")");
   }
}
