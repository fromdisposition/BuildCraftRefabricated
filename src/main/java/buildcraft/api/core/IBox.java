package buildcraft.api.core;

import net.minecraft.core.BlockPos;

public interface IBox extends IZone {
   IBox expand(int var1);

   IBox contract(int var1);

   BlockPos min();

   BlockPos max();

   default BlockPos size() {
      return this.max().subtract(this.min());
   }
}
