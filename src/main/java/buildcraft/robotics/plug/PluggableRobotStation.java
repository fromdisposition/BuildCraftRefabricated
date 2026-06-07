package buildcraft.robotics.plug;

import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.IDockingStationProvider;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.robotics.BCRoboticsItems;
import buildcraft.robotics.robot.DockingStationPipe;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class PluggableRobotStation extends PipePluggable implements IDockingStationProvider {
   private static final AABB[] BOXES = new AABB[6];

   public enum RobotStationState {
      None,
      Available,
      Reserved,
      Linked;
   }

   private DockingStationPipe station;
   private RobotStationState renderState = RobotStationState.None;

   public PluggableRobotStation(PluggableDefinition definition, IPipeHolder holder, Direction side) {
      super(definition, holder, side);
   }

   @Override
   public AABB getBoundingBox() {
      return BOXES[this.side.ordinal()];
   }

   @Override
   public boolean isBlocking() {
      return true;
   }

   @Override
   public boolean canBeConnected() {
      return false;
   }

   @Override
   public ItemStack getPickStack() {
      return new ItemStack(BCRoboticsItems.ROBOT_STATION);
   }

   @Override
   public DockingStation getStation() {
      Level world = this.holder.getPipeWorld();
      if (this.station == null && world != null && !world.isClientSide()) {
         DockingStation existing = RobotManager.registryProvider.getRegistry(world).getStation(this.holder.getPipePos(), this.side);
         if (existing instanceof DockingStationPipe pipeStation) {
            this.station = pipeStation;
         } else {
            this.station = new DockingStationPipe(this.holder, this.side);
            RobotManager.registryProvider.getRegistry(world).registerStation(this.station);
         }
      }

      return this.station;
   }

   @Override
   public void onPlacedBy(Player player) {
      super.onPlacedBy(player);
      if (!this.holder.getPipeWorld().isClientSide()) {
         this.getStation();
      }
   }

   @Override
   public void onRemove() {
      Level world = this.holder.getPipeWorld();
      if (world != null && !world.isClientSide() && this.station != null) {
         RobotManager.registryProvider.getRegistry(world).removeStation(this.station);
         this.station = null;
      }
   }

   @Override
   public boolean needsTick() {
      return true;
   }

   @Override
   public void onTick() {
      Level world = this.holder.getPipeWorld();
      if (world != null && !world.isClientSide()) {
         RobotStationState newState = this.computeState();
         if (newState != this.renderState) {
            this.renderState = newState;
            this.scheduleNetworkUpdate();
         }
      }
   }

   private RobotStationState computeState() {
      DockingStation s = this.getStation();
      if (s == null) {
         return RobotStationState.None;
      } else if (s.isMainStation()) {
         return RobotStationState.Linked;
      } else {
         return s.isTaken() ? RobotStationState.Reserved : RobotStationState.Available;
      }
   }

   public RobotStationState getRenderState() {
      return this.renderState;
   }

   @Override
   public CompoundTag writeClientUpdateData() {
      CompoundTag nbt = super.writeClientUpdateData();
      nbt.putByte("state", (byte) this.renderState.ordinal());
      return nbt;
   }

   @Override
   public void readClientUpdateData(CompoundTag nbt) {
      super.readClientUpdateData(nbt);
      int ordinal = nbt.getByte("state").orElse((byte) 0);
      RobotStationState[] values = RobotStationState.values();
      this.renderState = ordinal >= 0 && ordinal < values.length ? values[ordinal] : RobotStationState.None;
   }

   @Override
   public void writeCreationPayload(FriendlyByteBuf buffer) {
      super.writeCreationPayload(buffer);
      buffer.writeByte(this.renderState.ordinal());
   }

   static {
      double min = 0.25;
      double max = 0.75;
      double l = 0.0;
      double h = 0.25;
      double ll = 0.75;
      double hh = 1.0;
      BOXES[Direction.DOWN.ordinal()] = new AABB(min, l, min, max, h, max);
      BOXES[Direction.UP.ordinal()] = new AABB(min, ll, min, max, hh, max);
      BOXES[Direction.NORTH.ordinal()] = new AABB(min, min, l, max, max, h);
      BOXES[Direction.SOUTH.ordinal()] = new AABB(min, min, ll, max, max, hh);
      BOXES[Direction.WEST.ordinal()] = new AABB(l, min, min, h, max, max);
      BOXES[Direction.EAST.ordinal()] = new AABB(ll, min, min, hh, max, max);
   }
}
