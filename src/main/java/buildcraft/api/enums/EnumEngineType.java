package buildcraft.api.enums;

import buildcraft.api.core.IEngineType;
import net.minecraft.util.StringRepresentable;

public enum EnumEngineType implements StringRepresentable, IEngineType {
   WOOD("core", "wood"),
   STONE("energy", "stone"),
   IRON("energy", "iron"),
   CREATIVE("energy", "creative"),
   RF("energy", "rf");

   public final String unlocalizedTag;
   public final String resourceLocation;
   public static final EnumEngineType[] VALUES = values();

   EnumEngineType(String mod, String loc) {
      this.unlocalizedTag = loc;
      this.resourceLocation = "buildcraft" + mod + ":block/engine/inv/" + loc;
   }

   @Override
   public String getItemModelLocation() {
      return this.resourceLocation;
   }

   public String getSerializedName() {
      return this.unlocalizedTag;
   }

   public static EnumEngineType fromMeta(int meta) {
      if (meta < 0 || meta >= VALUES.length) {
         meta = 0;
      }

      return VALUES[meta];
   }
}
