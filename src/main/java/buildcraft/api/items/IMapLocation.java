package buildcraft.api.items;

import buildcraft.api.core.IBox;
import buildcraft.api.core.IZone;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public interface IMapLocation extends INamedItem {
   BlockPos getPoint(@Nonnull ItemStack var1);

   IBox getBox(@Nonnull ItemStack var1);

   IZone getZone(@Nonnull ItemStack var1);

   List<BlockPos> getPath(@Nonnull ItemStack var1);

   Direction getPointSide(@Nonnull ItemStack var1);

   enum MapLocationType {
      CLEAN,
      SPOT,
      AREA,
      PATH,
      ZONE,
      PATH_REPEATING;

      public final int meta = this.ordinal();

      public static IMapLocation.MapLocationType getFromStack(@Nonnull ItemStack stack) {
         int dam = 0;
         return dam >= 0 && dam < values().length ? values()[dam] : CLEAN;
      }

      public void setToStack(@Nonnull ItemStack stack) {
      }
   }
}
