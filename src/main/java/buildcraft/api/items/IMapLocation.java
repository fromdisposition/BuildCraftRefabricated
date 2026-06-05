package buildcraft.api.items;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import buildcraft.api.core.IBox;
import buildcraft.api.core.IZone;

public interface IMapLocation extends INamedItem {
    enum MapLocationType {
        CLEAN,
        SPOT,
        AREA,
        PATH,
        ZONE,

        PATH_REPEATING;

        public final int meta = ordinal();

        public static MapLocationType getFromStack(@Nonnull ItemStack stack) {
            int dam = 0;
            if (dam < 0 || dam >= values().length) {
                return MapLocationType.CLEAN;
            }
            return values()[dam];
        }

        public void setToStack(@Nonnull ItemStack stack) {
            ;
        }
    }

    BlockPos getPoint(@Nonnull ItemStack stack);

    IBox getBox(@Nonnull ItemStack stack);

    IZone getZone(@Nonnull ItemStack stack);

    List<BlockPos> getPath(@Nonnull ItemStack stack);

    Direction getPointSide(@Nonnull ItemStack stack);
}
