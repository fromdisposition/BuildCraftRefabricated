package buildcraft.api.core;

public enum EnumHandlerPriority implements Comparable<EnumHandlerPriority> {
   HIGHEST,
   HIGH,
   NORMAL,
   LOW,
   LOWEST;

   public static final EnumHandlerPriority[] VALUES = values();
}
