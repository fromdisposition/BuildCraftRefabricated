package buildcraft.api.robots;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.IInjectable;
import java.util.Arrays;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;

public abstract class DockingStation {
   public Direction side;
   public Level world;
   private long robotTakingId = Long.MAX_VALUE;
   private EntityRobotBase robotTaking;
   private boolean linkIsMain = false;
   private BlockPos pos;

   public DockingStation(BlockPos iIndex, Direction iSide) {
      this.pos = iIndex;
      this.side = iSide;
   }

   public DockingStation() {
   }

   public boolean isMainStation() {
      return this.linkIsMain;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public Direction side() {
      return this.side;
   }

   public EntityRobotBase robotTaking() {
      if (this.robotTakingId == Long.MAX_VALUE) {
         return null;
      }

      if (this.robotTaking == null) {
         this.robotTaking = RobotManager.registryProvider.getRegistry(this.world).getLoadedRobot(this.robotTakingId);
      }

      return this.robotTaking;
   }

   public void invalidateRobotTakingEntity() {
      this.robotTaking = null;
   }

   public long linkedId() {
      return this.robotTakingId;
   }

   public boolean takeAsMain(EntityRobotBase robot) {
      if (this.robotTakingId == Long.MAX_VALUE) {
         IRobotRegistry registry = RobotManager.registryProvider.getRegistry(this.world);
         this.linkIsMain = true;
         this.robotTaking = robot;
         this.robotTakingId = robot.getRobotId();
         registry.registryMarkDirty();
         robot.setMainStation(this);
         registry.take(this, robot.getRobotId());
         return true;
      } else {
         return this.robotTakingId == robot.getRobotId();
      }
   }

   public boolean take(EntityRobotBase robot) {
      if (this.robotTaking == null) {
         IRobotRegistry registry = RobotManager.registryProvider.getRegistry(this.world);
         this.linkIsMain = false;
         this.robotTaking = robot;
         this.robotTakingId = robot.getRobotId();
         registry.registryMarkDirty();
         registry.take(this, robot.getRobotId());
         return true;
      } else {
         return robot.getRobotId() == this.robotTakingId;
      }
   }

   public void release(EntityRobotBase robot) {
      if (this.robotTaking == robot && !this.linkIsMain) {
         IRobotRegistry registry = RobotManager.registryProvider.getRegistry(this.world);
         this.unsafeRelease(robot);
         registry.registryMarkDirty();
         registry.release(this, robot.getRobotId());
      }
   }

   public void unsafeRelease(EntityRobotBase robot) {
      if (this.robotTaking == robot) {
         this.linkIsMain = false;
         this.robotTaking = null;
         this.robotTakingId = Long.MAX_VALUE;
      }
   }

   public void writeToNBT(CompoundTag nbt) {
      nbt.putIntArray("pos", new int[]{this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()});
      nbt.putByte("side", (byte)this.side.ordinal());
      nbt.putBoolean("isMain", this.linkIsMain);
      nbt.putLong("robotId", this.robotTakingId);
   }

   public void readFromNBT(CompoundTag nbt) {
      if (nbt.contains("index")) {
         CompoundTag indexNBT = nbt.getCompound("index").orElse(new CompoundTag());
         int x = indexNBT.getInt("i").orElse(0);
         int y = indexNBT.getInt("j").orElse(0);
         int z = indexNBT.getInt("k").orElse(0);
         this.pos = new BlockPos(x, y, z);
      } else {
         int[] array = nbt.getIntArray("pos").orElse(new int[0]);
         if (array.length == 3) {
            this.pos = new BlockPos(array[0], array[1], array[2]);
         } else if (array.length != 0) {
            BCLog.logger.warn("Found an integer array that was not the right length! (" + Arrays.toString(array) + ")");
         } else {
            BCLog.logger.warn("Did not find any integer positions! This is a bug!");
         }
      }

      this.side = Direction.values()[nbt.getByte("side").orElse((byte)0)];
      this.linkIsMain = nbt.getBoolean("isMain").orElse(false);
      this.robotTakingId = nbt.getLong("robotId").orElse(0L);
   }

   public boolean isTaken() {
      return this.robotTakingId != Long.MAX_VALUE;
   }

   public long robotIdTaking() {
      return this.robotTakingId;
   }

   public BlockPos index() {
      return this.pos;
   }

   @Override
   public String toString() {
      return "{" + this.pos + ", " + this.side + " :" + this.robotTakingId + "}";
   }

   public boolean linkIsDocked() {
      return this.robotTaking() != null ? this.robotTaking().getDockingStation() == this : false;
   }

   public boolean canRelease() {
      return !this.isMainStation() && !this.linkIsDocked();
   }

   public boolean isInitialized() {
      return true;
   }

   public abstract Iterable<StatementSlot> getActiveActions();

   public IInjectable getItemOutput() {
      return null;
   }

   public EnumPipePart getItemOutputSide() {
      return EnumPipePart.CENTER;
   }

   public Container getItemInput() {
      return null;
   }

   public EnumPipePart getItemInputSide() {
      return EnumPipePart.CENTER;
   }

   public Storage<FluidVariant> getFluidOutput() {
      return null;
   }

   public EnumPipePart getFluidOutputSide() {
      return EnumPipePart.CENTER;
   }

   public Storage<FluidVariant> getFluidInput() {
      return null;
   }

   public EnumPipePart getFluidInputSide() {
      return EnumPipePart.CENTER;
   }

   public boolean providesPower() {
      return false;
   }

   public IRequestProvider getRequestProvider() {
      return null;
   }

   public void onChunkUnload() {
   }
}
