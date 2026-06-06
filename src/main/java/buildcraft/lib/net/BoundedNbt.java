package buildcraft.lib.net;

import net.minecraft.nbt.NbtAccounter;

public final class BoundedNbt {
   private BoundedNbt() {
   }

   public static NbtAccounter networkQuota() {
      return NbtAccounter.create(2097152L);
   }
}
