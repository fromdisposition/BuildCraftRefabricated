package buildcraft.silicon.gate;

import java.util.Locale;

public enum EnumGateLogic {
   AND,
   OR;

   public static final EnumGateLogic[] VALUES = values();
   public final String tag = this.name().toLowerCase(Locale.ROOT);

   public static EnumGateLogic getByOrdinal(int ord) {
      return ord >= 0 && ord < VALUES.length ? VALUES[ord] : AND;
   }
}
