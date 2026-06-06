package buildcraft.lib.debug;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

public enum BCAdvDebugging {
   INSTANCE;

   @Nullable
   private BlockPos clientTarget = null;

   public void setClientTarget(BlockPos pos) {
      this.clientTarget = pos == null ? null : pos.immutable();
   }

   @Nullable
   public BlockPos getClientTarget() {
      return this.clientTarget;
   }

   public void clear() {
      this.clientTarget = null;
   }
}
