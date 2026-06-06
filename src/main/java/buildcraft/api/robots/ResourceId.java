package buildcraft.api.robots;

import buildcraft.api.core.BCLog;
import net.minecraft.nbt.CompoundTag;

public abstract class ResourceId {
   protected ResourceId() {
   }

   public void writeToNBT(CompoundTag nbt) {
      nbt.putString("resourceName", RobotManager.getResourceIdName((Class<? extends ResourceId>)this.getClass()));
   }

   protected void readFromNBT(CompoundTag nbt) {
   }

   public static ResourceId load(CompoundTag nbt) {
      try {
         Class<?> cls;
         if (nbt.contains("class")) {
            cls = RobotManager.getResourceIdByLegacyClassName(nbt.getString("class").orElse(""));
         } else {
            cls = RobotManager.getResourceIdByName(nbt.getString("resourceName").orElse(""));
         }

         ResourceId id = (ResourceId)cls.getDeclaredConstructor().newInstance();
         id.readFromNBT(nbt);
         return id;
      } catch (Throwable e) {
         BCLog.logger.warn("[robots] Failed to load a ResourceId from NBT", e);
         return null;
      }
   }
}
