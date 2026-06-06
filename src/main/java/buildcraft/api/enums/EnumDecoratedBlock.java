package buildcraft.api.enums;

import java.util.Locale;
import net.minecraft.util.StringRepresentable;

public enum EnumDecoratedBlock implements StringRepresentable {
   DESTROY(0),
   BLUEPRINT(10),
   TEMPLATE(10),
   PAPER(10),
   LEATHER(10),
   LASER_BACK(0);

   public static final EnumDecoratedBlock[] VALUES = values();
   public final int lightValue;

   EnumDecoratedBlock(int lightValue) {
      this.lightValue = lightValue;
   }

   public String getSerializedName() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public static EnumDecoratedBlock fromMeta(int meta) {
      return meta >= 0 && meta < VALUES.length ? VALUES[meta] : DESTROY;
   }
}
