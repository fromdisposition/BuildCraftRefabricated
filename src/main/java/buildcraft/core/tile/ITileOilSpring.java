package buildcraft.core.tile;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;

public interface ITileOilSpring {

    void onPumpOil(GameProfile pumpOwner, BlockPos oilPos);
}
