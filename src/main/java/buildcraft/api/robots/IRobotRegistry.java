package buildcraft.api.robots;

import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public interface IRobotRegistry {
   long getNextRobotId();

   void registerRobot(EntityRobotBase var1);

   void killRobot(EntityRobotBase var1);

   void unloadRobot(EntityRobotBase var1);

   EntityRobotBase getLoadedRobot(long var1);

   boolean isTaken(ResourceId var1);

   long robotIdTaking(ResourceId var1);

   EntityRobotBase robotTaking(ResourceId var1);

   boolean take(ResourceId var1, EntityRobotBase var2);

   boolean take(ResourceId var1, long var2);

   void release(ResourceId var1);

   void releaseResources(EntityRobotBase var1);

   DockingStation getStation(BlockPos var1, Direction var2);

   Collection<DockingStation> getStations();

   void registerStation(DockingStation var1);

   void removeStation(DockingStation var1);

   void take(DockingStation var1, long var2);

   void release(DockingStation var1, long var2);

   void writeToNBT(CompoundTag var1);

   void readFromNBT(CompoundTag var1);

   void registryMarkDirty();
}
