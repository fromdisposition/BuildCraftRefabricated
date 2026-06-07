package buildcraft.robotics.entity;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.IZone;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRobotRegistry;
import buildcraft.api.robots.RobotManager;
import buildcraft.robotics.BCRoboticsEntities;
import buildcraft.robotics.boards.RedstoneBoardRobotEmptyNBT;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class EntityRobot extends EntityRobotBase {
   public static final int NB_ITEMS_SLOTS = 8;
   private static final EntityDataAccessor<Boolean> DATA_ITEM_ACTIVE = SynchedEntityData.defineId(EntityRobot.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Float> DATA_ENERGY = SynchedEntityData.defineId(EntityRobot.class, EntityDataSerializers.FLOAT);

   private final MjBattery battery = new MjBattery(MAX_POWER);
   private final ItemStack[] inv = new ItemStack[NB_ITEMS_SLOTS];
   private final Set<Integer> unreachableEntities = new HashSet<>();

   private RedstoneBoardRobot board;
   private AIRobot mainAI;
   private DockingStation linkedStation;
   private DockingStation currentDockingStation;
   private DockingStation mainStation;
   private long robotId = NULL_ROBOT_ID;
   private boolean needsInit = true;

   private float aimYaw;
   private float aimPitch;
   private ItemStack itemInUse = ItemStack.EMPTY;

   public Vec3 destination;

   public EntityRobot(EntityType<EntityRobot> type, Level level) {
      super(type, level);
      this.noPhysics = true;
      this.setNoGravity(true);
      for (int i = 0; i < this.inv.length; i++) {
         this.inv[i] = ItemStack.EMPTY;
      }
   }

   public static EntityRobot create(Level level, RedstoneBoardRobotNBT boardNBT) {
      EntityRobot robot = new EntityRobot(BCRoboticsEntities.ROBOT, level);
      robot.setBoard(boardNBT);
      return robot;
   }

   public void setBoard(RedstoneBoardRobotNBT boardNBT) {
      this.board = boardNBT.create(this);
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      builder.define(DATA_ITEM_ACTIVE, false);
      builder.define(DATA_ENERGY, 0.0F);
   }

   public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
      return LivingEntity.createLivingAttributes();
   }

   @Override
   public void tick() {
      super.tick();
      if (this.level().isClientSide()) {
         return;
      }

      if (this.needsInit) {
         this.needsInit = false;
         if (this.getRegistry().getLoadedRobot(this.getRobotId()) != this) {
            this.getRegistry().registerRobot(this);
         }
      }

      this.battery.tick(this.level(), this.position());
      this.entityData.set(DATA_ENERGY, (float) this.battery.getStored() / (float) this.battery.getCapacity());

      if (this.mainAI == null && this.board != null) {
         this.mainAI = new buildcraft.robotics.ai.AIRobotMain(this);
         this.mainAI.start();
      }

      if (this.mainAI != null) {
         this.mainAI.cycle();
      }

      if (this.destination != null) {
         this.moveTowardsDestination();
      }
   }

   private void moveTowardsDestination() {
      Vec3 current = this.position();
      Vec3 diff = this.destination.subtract(current);
      double dist = diff.length();
      if (dist < 0.1) {
         this.setDeltaMovement(Vec3.ZERO);
         this.destination = null;
      } else {
         double speed = Math.min(0.15, dist);
         Vec3 move = diff.normalize().scale(speed);
         this.setDeltaMovement(move);
         this.setPos(current.add(move));
      }
   }

   @Override
   public boolean isMoving() {
      return this.destination != null;
   }

   @Override
   public MjBattery getBattery() {
      return this.battery;
   }

   @Override
   public RedstoneBoardRobot getBoard() {
      return this.board;
   }

   @Override
   public DockingStation getLinkedStation() {
      return this.linkedStation;
   }

   @Override
   public DockingStation getDockingStation() {
      return this.currentDockingStation;
   }

   @Override
   public void dock(DockingStation station) {
      this.currentDockingStation = station;
   }

   @Override
   public void undock() {
      if (this.currentDockingStation != null) {
         DockingStation station = this.currentDockingStation;
         this.currentDockingStation = null;
         station.release(this);
      }
   }

   public void setLinkedStation(DockingStation station) {
      this.linkedStation = station;
   }

   @Override
   public void setMainStation(DockingStation station) {
      this.mainStation = station;
   }

   @Override
   public IZone getZoneToWork() {
      return null;
   }

   @Override
   public IZone getZoneToLoadUnload() {
      return null;
   }

   @Override
   public boolean containsItems() {
      for (ItemStack stack : this.inv) {
         if (!stack.isEmpty()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean hasFreeSlot() {
      for (ItemStack stack : this.inv) {
         if (stack.isEmpty()) {
            return true;
         }
      }

      return false;
   }

   public ItemStack getStackInSlot(int slot) {
      return slot >= 0 && slot < this.inv.length ? this.inv[slot] : ItemStack.EMPTY;
   }

   public void setStackInSlot(int slot, ItemStack stack) {
      if (slot >= 0 && slot < this.inv.length) {
         this.inv[slot] = stack;
      }
   }

   @Override
   public ItemStack receiveItem(BlockEntity tile, ItemStack stack) {
      ItemStack remaining = stack.copy();
      for (int i = 0; i < this.inv.length && !remaining.isEmpty(); i++) {
         if (this.inv[i].isEmpty()) {
            this.inv[i] = remaining;
            remaining = ItemStack.EMPTY;
         } else if (ItemStack.isSameItemSameComponents(this.inv[i], remaining)) {
            int room = this.inv[i].getMaxStackSize() - this.inv[i].getCount();
            int moved = Math.min(room, remaining.getCount());
            this.inv[i].grow(moved);
            remaining.shrink(moved);
         }
      }

      return remaining;
   }

   @Override
   public void unreachableEntityDetected(Entity entity) {
      this.unreachableEntities.add(entity.getId());
   }

   @Override
   public boolean isKnownUnreachable(Entity entity) {
      return this.unreachableEntities.contains(entity.getId());
   }

   @Override
   public long getRobotId() {
      return this.robotId;
   }

   @Override
   public void setUniqueRobotId(long id) {
      this.robotId = id;
   }

   @Override
   public IRobotRegistry getRegistry() {
      return RobotManager.registryProvider.getRegistry(this.level());
   }

   @Override
   public void releaseResources() {
      this.getRegistry().releaseResources(this);
   }

   @Override
   public void onChunkUnload() {
      if (!this.level().isClientSide()) {
         this.getRegistry().unloadRobot(this);
      }
   }

   @Override
   public void setItemInUse(ItemStack stack) {
      this.itemInUse = stack;
   }

   @Override
   public void setItemActive(boolean active) {
      this.entityData.set(DATA_ITEM_ACTIVE, active);
   }

   public boolean isItemActive() {
      return this.entityData.get(DATA_ITEM_ACTIVE);
   }

   public float getEnergyFraction() {
      return this.entityData.get(DATA_ENERGY);
   }

   @Override
   public void aimItemAt(float yaw, float pitch) {
      this.aimYaw = yaw;
      this.aimPitch = pitch;
   }

   @Override
   public void aimItemAt(BlockPos pos) {
      Vec3 diff = Vec3.atCenterOf(pos).subtract(this.position());
      this.aimYaw = (float) (Math.atan2(diff.z, diff.x) * 180.0 / Math.PI);
      this.aimPitch = (float) (Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z)) * 180.0 / Math.PI);
   }

   @Override
   public float getAimYaw() {
      return this.aimYaw;
   }

   @Override
   public float getAimPitch() {
      return this.aimPitch;
   }

   @Override
   public void remove(Entity.RemovalReason reason) {
      if (!this.level().isClientSide() && this.robotId != NULL_ROBOT_ID) {
         this.getRegistry().killRobot(this);
      }

      super.remove(reason);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      output.putLong("robotId", this.robotId);
      output.store("battery", CompoundTag.CODEC, this.battery.serializeNBT());
      if (this.board != null) {
         CompoundTag boardTag = new CompoundTag();
         this.board.writeToNBT(boardTag);
         output.store("board", CompoundTag.CODEC, boardTag);
      }

      CompoundTag invTag = new CompoundTag();
      for (int i = 0; i < this.inv.length; i++) {
         final int slot = i;
         if (!this.inv[i].isEmpty()) {
            ItemStack.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, this.inv[i]).result().ifPresent(t -> invTag.put("slot" + slot, t));
         }
      }

      output.store("inv", CompoundTag.CODEC, invTag);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      this.robotId = input.getLongOr("robotId", NULL_ROBOT_ID);
      input.read("battery", CompoundTag.CODEC).ifPresent(this.battery::deserializeNBT);
      input.read("board", CompoundTag.CODEC).ifPresent(boardTag -> {
         AIRobot loaded = AIRobot.loadAI(boardTag, this);
         if (loaded instanceof RedstoneBoardRobot boardRobot) {
            this.board = boardRobot;
         }
      });
      if (this.board == null) {
         this.board = RedstoneBoardRobotEmptyNBT.INSTANCE.create(this);
      }

      input.read("inv", CompoundTag.CODEC).ifPresent(invTag -> {
         for (int i = 0; i < this.inv.length; i++) {
            final int slot = i;
            if (invTag.contains("slot" + i)) {
               invTag.get("slot" + i);
               ItemStack.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, invTag.get("slot" + i)).result().ifPresent(s -> this.inv[slot] = s);
            }
         }
      });
   }

   @Override
   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      return false;
   }

   @Override
   public ItemStack getItemBySlot(EquipmentSlot slot) {
      return ItemStack.EMPTY;
   }

   @Override
   public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
   }

   @Override
   public HumanoidArm getMainArm() {
      return HumanoidArm.RIGHT;
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   public boolean isPickable() {
      return !this.isRemoved();
   }
}
