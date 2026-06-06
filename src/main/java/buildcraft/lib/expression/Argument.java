package buildcraft.lib.expression;

import java.util.Locale;

public class Argument {
   public final String name;
   public final Class<?> type;

   public Argument(String name, Class<?> type) {
      this.name = name;
      this.type = type;
   }

   public static Argument argLong(String name) {
      return new Argument(name, long.class);
   }

   public static Argument argDouble(String name) {
      return new Argument(name, double.class);
   }

   public static Argument argBoolean(String name) {
      return new Argument(name, boolean.class);
   }

   public static Argument argString(String name) {
      return new Argument(name, String.class);
   }

   public static Argument argObject(String name, Class<?> type) {
      return new Argument(name, type);
   }

   @Override
   public String toString() {
      return this.type.getSimpleName().toLowerCase(Locale.ROOT) + " '" + this.name + "'";
   }
}
