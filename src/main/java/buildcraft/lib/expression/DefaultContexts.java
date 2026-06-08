/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression;

import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.value.NodeVariableDouble;

public class DefaultContexts {
   public static final FunctionContext MATH_SCALAR = new FunctionContext("Math: Scalar");
   public static final FunctionContext MATH_VECTOR = new FunctionContext("Math: Vector", NodeTypes.VEC_LONG, NodeTypes.VEC_DOUBLE);
   public static final FunctionContext RENDERING = new FunctionContext("Rendering");
   public static final FunctionContext LOCALIZATION = new FunctionContext("Localization");
   public static final FunctionContext CONFIG = new FunctionContext("Config");
   public static final NodeVariableDouble RENDER_PARTIAL_TICKS = RENDERING.putVariableDouble("partial_ticks");
   private static final FunctionContext[] CTX_ARRAY_ALL = new FunctionContext[]{NodeTypes.STRING, MATH_SCALAR, MATH_VECTOR, RENDERING, LOCALIZATION};

   public static FunctionContext createWithAll() {
      return createWithAll("all");
   }

   public static FunctionContext createWithAll(String name) {
      return new FunctionContext(name, CTX_ARRAY_ALL);
   }

   static {
      MATH_SCALAR.putConstantDouble("pi", Math.PI);
      MATH_SCALAR.putConstantDouble("e", Math.E);
      MATH_SCALAR.put_l_l("abs", Math::abs, a -> "abs( " + a + ")");
      MATH_SCALAR.put_d_d("abs", Math::abs, a -> "abs( " + a + ")");
      MATH_SCALAR.put_d_l("round", Math::round, a -> "round( " + a + ")");
      MATH_SCALAR.put_d_l("floor", a -> (long)Math.floor(a), a -> "floor( " + a + ")");
      MATH_SCALAR.put_d_l("ceil", a -> (long)Math.ceil(a), a -> "ceil( " + a + ")");
      MATH_SCALAR.put_d_l("sign", a -> a == 0.0 ? 0L : (a < 0.0 ? -1L : 1L), a -> "sign( " + a + ")");
      MATH_SCALAR.put_d_d("log", Math::log);
      MATH_SCALAR.put_d_d("log10", Math::log10);
      MATH_SCALAR.put_d_d("sqrt", Math::sqrt);
      MATH_SCALAR.put_d_d("cbrt", Math::cbrt);
      MATH_SCALAR.put_d_d("degrees", Math::toDegrees);
      MATH_SCALAR.put_d_d("radians", Math::toRadians);
      MATH_SCALAR.put_d_d("round_float", a -> Math.round(a * 1.0E10) / 1.0E10);
      MATH_SCALAR.put_d_d("sin", Math::sin);
      MATH_SCALAR.put_d_d("cos", Math::cos);
      MATH_SCALAR.put_d_d("tan", Math::tan);
      MATH_SCALAR.put_d_d("asin", Math::asin);
      MATH_SCALAR.put_d_d("acos", Math::acos);
      MATH_SCALAR.put_d_d("atan", Math::atan);
      MATH_SCALAR.put_dd_d("atan2", Math::atan2);
      MATH_SCALAR.put_d_d("sinh", Math::sinh);
      MATH_SCALAR.put_d_d("cosh", Math::cosh);
      MATH_SCALAR.put_d_d("tanh", Math::tanh);
      MATH_SCALAR.put_ll_l("min", Math::min);
      MATH_SCALAR.put_ll_l("max", Math::max);
      MATH_SCALAR.put_dd_d("min", Math::min);
      MATH_SCALAR.put_dd_d("max", Math::max);
      MATH_SCALAR.put_dd_d("pow", Math::pow);
      MATH_SCALAR.put_ddd_d("clamp", (c, min, max) -> Math.max(Math.min(c, max), min));
      MATH_SCALAR.put_lll_l("clamp", (c, min, max) -> Math.max(Math.min(c, max), min));
   }
}
