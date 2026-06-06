package buildcraft.builders.snapshot;

public enum EnumContainerContentsMode {
   INCLUDE("gui.buildcraft.builder.contentsmode.include"),
   IGNORE("gui.buildcraft.builder.contentsmode.ignore");

   private final String tooltipKey;

   EnumContainerContentsMode(String tooltipKey) {
      this.tooltipKey = tooltipKey;
   }

   public EnumContainerContentsMode next() {
      return values()[(this.ordinal() + 1) % values().length];
   }

   public String tooltipKey() {
      return this.tooltipKey;
   }

   public static EnumContainerContentsMode fromOrdinal(int ord) {
      return ord >= 0 && ord < values().length ? values()[ord] : INCLUDE;
   }
}
