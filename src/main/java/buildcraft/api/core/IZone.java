package buildcraft.api.core;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public interface IZone {
   double distanceTo(BlockPos var1);

   double distanceToSquared(BlockPos var1);

   boolean contains(Vec3 var1);

   BlockPos getRandomBlockPos(Random var1);
}
