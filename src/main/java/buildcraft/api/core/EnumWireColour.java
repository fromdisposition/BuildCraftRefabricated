package buildcraft.api.core;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.world.item.DyeColor;

public enum EnumWireColour {
   WHITE(DyeColor.WHITE, DyeColor.LIGHT_GRAY, DyeColor.GRAY, DyeColor.BLACK),
   ORANGE(DyeColor.ORANGE),
   LIGHT_BLUE(DyeColor.LIGHT_BLUE, DyeColor.CYAN),
   YELLOW(DyeColor.YELLOW),
   LIME(DyeColor.LIME),
   PINK(DyeColor.PINK, DyeColor.MAGENTA),
   PURPLE(DyeColor.PURPLE),
   BLUE(DyeColor.BLUE),
   BROWN(DyeColor.BROWN),
   GREEN(DyeColor.GREEN),
   RED(DyeColor.RED);

   private static final EnumMap<DyeColor, EnumWireColour> DYE_TO_WIRE = new EnumMap<>(DyeColor.class);
   public final DyeColor primaryIdenticalColour;
   public final Set<DyeColor> similarBasedColours;

   EnumWireColour(DyeColor primary, DyeColor... secondary) {
      this.primaryIdenticalColour = primary;
      this.similarBasedColours = EnumSet.of(primary, secondary);
   }

   public static EnumWireColour convertToWire(DyeColor dye) {
      return DYE_TO_WIRE.get(dye);
   }

   static {
      for (EnumWireColour wire : values()) {
         for (DyeColor dye : wire.similarBasedColours) {
            EnumWireColour prev = DYE_TO_WIRE.put(dye, wire);
            if (prev != null) {
               throw new Error(wire + " attempted to override " + prev + " for the dye " + dye + "!");
            }
         }
      }

      for (DyeColor dye : DyeColor.values()) {
         EnumWireColour wire = DYE_TO_WIRE.get(dye);
         if (wire == null) {
            throw new Error(dye + " isn't mapped to a wire colour!");
         }
      }
   }
}
