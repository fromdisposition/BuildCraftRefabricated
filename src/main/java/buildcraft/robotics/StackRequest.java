package buildcraft.robotics;

import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.robots.IRobotRegistry;
import buildcraft.api.robots.ResourceId;
import buildcraft.api.robots.ResourceIdRequest;
import buildcraft.api.robots.RobotManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A pending item request from a machine/requester, paired with the station that serves it. */
public class StackRequest {
   private IRequestProvider requester;
   private final int slot;
   private final ItemStack stack;
   private DockingStation station;
   private BlockPos stationIndex;
   private Direction stationSide;

   public StackRequest(IRequestProvider requester, int slot, ItemStack stack) {
      this.requester = requester;
      this.slot = slot;
      this.stack = stack;
   }

   private StackRequest(int slot, ItemStack stack, BlockPos stationIndex, Direction stationSide) {
      this.slot = slot;
      this.stack = stack;
      this.stationIndex = stationIndex;
      this.stationSide = stationSide;
   }

   public IRequestProvider getRequester(Level world) {
      if (this.requester == null) {
         DockingStation dockingStation = this.getStation(world);
         if (dockingStation != null) {
            this.requester = dockingStation.getRequestProvider();
         }
      }

      return this.requester;
   }

   public int getSlot() {
      return this.slot;
   }

   public ItemStack getStack() {
      return this.stack;
   }

   public DockingStation getStation(Level world) {
      if (this.station == null && this.stationIndex != null) {
         IRobotRegistry registry = RobotManager.registryProvider.getRegistry(world);
         this.station = registry.getStation(this.stationIndex, this.stationSide);
      }

      return this.station;
   }

   public void setStation(DockingStation station) {
      this.station = station;
      this.stationIndex = station.index();
      this.stationSide = station.side();
   }

   public ResourceId getResourceId(Level world) {
      return this.getStation(world) != null ? new ResourceIdRequest(this.getStation(world), this.slot) : null;
   }

   public void writeToNBT(CompoundTag nbt) {
      nbt.putInt("slot", this.slot);
      ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, this.stack).result().ifPresent(t -> nbt.put("stack", t));
      if (this.station != null) {
         BlockPos index = this.station.index();
         nbt.putIntArray("stationIndex", new int[]{index.getX(), index.getY(), index.getZ()});
         nbt.putByte("stationSide", (byte)this.station.side().ordinal());
      }
   }

   public static StackRequest loadFromNBT(CompoundTag nbt) {
      int[] arr = nbt.getIntArray("stationIndex").orElse(new int[0]);
      if (arr.length != 3) {
         return null;
      }

      int slot = nbt.getInt("slot").orElse(0);
      ItemStack stack = nbt.contains("stack")
         ? ItemStack.CODEC.parse(NbtOps.INSTANCE, nbt.get("stack")).result().orElse(ItemStack.EMPTY)
         : ItemStack.EMPTY;
      BlockPos stationIndex = new BlockPos(arr[0], arr[1], arr[2]);
      Direction stationSide = Direction.values()[nbt.getByte("stationSide").orElse((byte)0)];
      return new StackRequest(slot, stack, stationIndex, stationSide);
   }
}
