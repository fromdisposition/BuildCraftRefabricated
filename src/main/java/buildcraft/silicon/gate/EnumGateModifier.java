package buildcraft.silicon.gate;

import java.util.Locale;

public enum EnumGateModifier {
   NO_MODIFIER(0, 0, 1),
   LAPIS(1, 0, 1),
   QUARTZ(1, 1, 2),
   DIAMOND(3, 3, 2);

   public static final EnumGateModifier[] VALUES = values();
   public final int triggerParams;
   public final int actionParams;
   public final int slotDivisor;
   public final String tag = this.name().toLowerCase(Locale.ROOT);

   EnumGateModifier(int triggerParams, int actionParams, int slotDivisor) {
      this.triggerParams = triggerParams;
      this.actionParams = actionParams;
      this.slotDivisor = slotDivisor;
   }

   public static EnumGateModifier getByOrdinal(int ord) {
      return ord >= 0 && ord < VALUES.length ? VALUES[ord] : NO_MODIFIER;
   }
}
