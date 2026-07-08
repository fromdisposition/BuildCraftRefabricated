/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.entity;

import buildcraft.lib.nbt.BcAuth;
import buildcraft.lib.nbt.BcNbt;
import buildcraft.lib.nbt.BcValueIn;
import buildcraft.lib.nbt.BcValueOut;
import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.robotics.item.ItemRobot;
import buildcraft.api.core.IZone;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRobotRegistry;
import buildcraft.api.robots.RobotManager;
import buildcraft.robotics.BCRoboticsEntities;
import buildcraft.robotics.ai.AIRobotMain;
import buildcraft.robotics.boards.RedstoneBoardRobotEmptyNBT;
import buildcraft.robotics.robot.DockingStationPipe;
import com.mojang.authlib.GameProfile;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
//? if >= 1.21.10 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?}
import net.minecraft.world.phys.Vec3;

public class EntityRobot extends EntityRobotBase {
   public static final int NB_ITEMS_SLOTS = 8;
   private static final EntityDataAccessor<Boolean> DATA_ITEM_ACTIVE = SynchedEntityData.defineId(EntityRobot.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Float> DATA_ENERGY = SynchedEntityData.defineId(EntityRobot.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<String> DATA_TEXTURE = SynchedEntityData.defineId(EntityRobot.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(EntityRobot.class, EntityDataSerializers.ITEM_STACK);
   private static final EntityDataAccessor<Float> DATA_AIM_YAW = SynchedEntityData.defineId(EntityRobot.class, EntityDataSerializers.FLOAT);
   public static final net.minecraft.resources.Identifier DEFAULT_TEXTURE = net.minecraft.resources.Identifier.fromNamespaceAndPath("buildcraftrobotics", "entities/robot_base");

   public static final int MAX_FLUID_MB = 4000;

   private final MjBattery battery = new MjBattery(MAX_POWER);
   private final ItemStack[] inv = new ItemStack[NB_ITEMS_SLOTS];
   private final buildcraft.lib.fabric.transfer.fluid.SingleFluidTank fluidTank = new buildcraft.lib.fabric.transfer.fluid.SingleFluidTank(MAX_FLUID_MB);
   private final Set<Integer> unreachableEntities = new HashSet<>();

   private RedstoneBoardRobot board;
   private AIRobotMain mainAI;
   private DockingStation linkedStation;
   private DockingStation currentDockingStation;
   private DockingStation mainStation;
   private BlockPos linkedStationPos;
   private Direction linkedStationSide;
   private BlockPos currentDockingStationPos;
   private Direction currentDockingStationSide;
   private long robotId = NULL_ROBOT_ID;
   private boolean needsInit = true;

   private float aimYaw;
   private float aimPitch;
   private ItemStack itemInUse = ItemStack.EMPTY;
   /** Last synced robot texture (an Identifier), cached so the per-tick sync only fires when it actually changes. */
   private Object lastRobotTexture;
   private float lastSentEnergy = Float.NaN;
   private float lastSentAimYaw = Float.NaN;

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
      // Must register the inherited LivingEntity/Entity accessors (id 8 = LIVING_ENTITY_FLAGS, health, ...)
      // first, or SynchedEntityData.Builder.build() throws "has not defined synched data value 8".
      super.defineSynchedData(builder);
      builder.define(DATA_ITEM_ACTIVE, false);
      builder.define(DATA_ENERGY, 0.0F);
      builder.define(DATA_TEXTURE, DEFAULT_TEXTURE.toString());
      builder.define(DATA_ITEM, ItemStack.EMPTY);
      builder.define(DATA_AIM_YAW, 0.0F);
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

      try {
         this.tickServer();
      } catch (Exception e) {
         // A legacy/corrupt robot must not crash the whole tick loop -- remove it and keep the world alive.
         buildcraft.api.core.BCLog.logger.warn(
            "[robots] Robot " + this.robotId + " threw while ticking; removing it to keep the world stable", e);
         try {
            this.discard();
         } catch (Exception ignored) {
         }
      }
   }

   private void tickServer() {
      if (this.needsInit) {
         this.needsInit = false;
         if (this.getRegistry().getLoadedRobot(this.getRobotId()) != this) {
            this.getRegistry().registerRobot(this);
         }
      }

      this.resolveStationsFromRegistry();

      if (this.currentDockingStation != null) {
         this.setDeltaMovement(Vec3.ZERO);
         this.destination = null;
         BlockPos dockPos = this.currentDockingStation.getPos();
         Direction dockSide = this.currentDockingStation.side();
         double dockX = dockPos.getX() + 0.5 + dockSide.getStepX() * 0.5;
         double dockY = dockPos.getY() + 0.5 + dockSide.getStepY() * 0.5;
         double dockZ = dockPos.getZ() + 0.5 + dockSide.getStepZ() * 0.5;
         // Docked is the default state: skip the per-tick setPos -> new AABB churn while already in place.
         if (this.getX() != dockX || this.getY() != dockY || this.getZ() != dockZ) {
            this.setPos(dockX, dockY, dockZ);
         }
         if (this.currentDockingStation.providesPower() && this.currentDockingStation instanceof DockingStationPipe pipeStation) {
            pipeStation.tryChargeRobot(this);
         }
      }

      this.battery.tick(this.level(), this.position());
      // The synced energy is display-only (getEnergyFraction -> RenderRobot), so quantize it to 1% steps: the
      // raw ratio changes every tick while charging/discharging, which re-dirtied the data watcher and
      // broadcast a float to every tracking client each tick per robot for an invisible change.
      float energyFraction = Math.round(this.battery.getStored() * 100.0F / this.battery.getCapacity()) / 100.0F;
      if (energyFraction != this.lastSentEnergy) {
         this.lastSentEnergy = energyFraction;
         this.entityData.set(DATA_ENERGY, energyFraction);
      }

      if (this.aimYaw != this.lastSentAimYaw) {
         this.lastSentAimYaw = this.aimYaw;
         this.entityData.set(DATA_AIM_YAW, this.aimYaw);
      }
      this.entityData.set(DATA_ITEM, this.itemInUse == null ? ItemStack.EMPTY : this.itemInUse);
      if (this.board != null && this.board.getNBTHandler() != null) {
         // Only rebuild + sync the texture string when the texture actually changes (it almost never does),
         // instead of allocating a String via toString() and running the data-watcher set every tick.
         Object texture = this.board.getNBTHandler().getRobotTexture();
         if (texture != null && !texture.equals(this.lastRobotTexture)) {
            this.lastRobotTexture = texture;
            this.entityData.set(DATA_TEXTURE, texture.toString());
         }
      }

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
      if (station != null) {
         this.currentDockingStationPos = station.getPos();
         this.currentDockingStationSide = station.side();
      }
   }

   @Override
   public void undock() {
      if (this.currentDockingStation != null) {
         DockingStation station = this.currentDockingStation;
         this.currentDockingStation = null;
         this.currentDockingStationPos = null;
         this.currentDockingStationSide = null;
         station.release(this);
      }
   }

   /** True when this robot's home (linked/main) station is the one at pos/side -- used to destroy the robot when
    *  that station is broken. Matches on the persisted linked-station position, so it works even after a reload. */
   public boolean isHomedAt(BlockPos pos, Direction side) {
      return pos != null && pos.equals(this.linkedStationPos) && side == this.linkedStationSide;
   }

   public void setLinkedStation(DockingStation station) {
      this.linkedStation = station;
      if (station != null) {
         this.linkedStationPos = station.getPos();
         this.linkedStationSide = station.side();
      } else {
         this.linkedStationPos = null;
         this.linkedStationSide = null;
      }
   }

   @Override
   public void setMainStation(DockingStation station) {
      if (this.linkedStation != null && this.linkedStation != station) {
         this.linkedStation.unsafeRelease(this);
      }

      this.mainStation = station;
      this.setLinkedStation(station);
   }

   public AIRobotMain getMainAI() {
      return this.mainAI;
   }

   public void overrideAI(AIRobot ai) {
      if (this.mainAI != null) {
         this.mainAI.setOverridingAI(ai);
      }
   }

   public long receivePower(long maxReceive, boolean simulate) {
      long notAccepted = this.battery.addPower(maxReceive, simulate);
      return maxReceive - notAccepted;
   }

   private void resolveStationsFromRegistry() {
      IRobotRegistry registry = this.getRegistry();

      if (this.linkedStation == null && this.linkedStationPos != null && this.linkedStationSide != null) {
         this.linkedStation = registry.getStation(this.linkedStationPos, this.linkedStationSide);
      }

      if (this.currentDockingStation == null && this.currentDockingStationPos != null && this.currentDockingStationSide != null) {
         this.currentDockingStation = registry.getStation(this.currentDockingStationPos, this.currentDockingStationSide);
      }
   }

   @Override
   public IZone getZoneToWork() {
      return this.getZone("buildcraft:robot.work_in_area");
   }

   @Override
   public IZone getZoneToLoadUnload() {
      IZone zone = this.getZone("buildcraft:robot.load_unload_area");
      return zone != null ? zone : this.getZoneToWork();
   }

   private IZone getZone(String actionTag) {
      if (this.linkedStation == null) {
         return null;
      }

      for (buildcraft.api.statements.StatementSlot slot : this.linkedStation.getActiveActions()) {
         if (slot.statement != null && actionTag.equals(slot.statement.getUniqueTag())
            && slot.parameters.length > 0 && slot.parameters[0] != null) {
            ItemStack stack = slot.parameters[0].getItemStack();
            if (!stack.isEmpty() && stack.getItem() instanceof buildcraft.api.items.IMapLocation map) {
               IZone zone = map.getZone(stack);
               if (zone != null) {
                  return zone;
               }
            }
         }
      }

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

   @Override
   public buildcraft.lib.fabric.transfer.fluid.SingleFluidTank getFluidStorage() {
      return this.fluidTank;
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

   /**
    * Non-mutating twin of {@link #receiveItem}: how many of {@code stack} would fit right now. Simulation paths
    * (station-search probes run {@code AIRobotLoad.load(..., doLoad=false)} against every candidate station) MUST
    * use this -- calling receiveItem there put items into the robot while the chest kept its stack: a pure dupe.
    */
   public int roomFor(ItemStack stack) {
      int room = 0;
      for (ItemStack slot : this.inv) {
         if (slot.isEmpty()) {
            room += stack.getMaxStackSize();
         } else if (ItemStack.isSameItemSameComponents(slot, stack)) {
            room += slot.getMaxStackSize() - slot.getCount();
         }

         if (room >= stack.getCount()) {
            break;
         }
      }

      return Math.min(room, stack.getCount());
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

   public ItemStack getItemInUse() {
      return this.itemInUse;
   }

   @Override
   public ItemStack getHeldItem() {
      return this.itemInUse;
   }

   public void attackTargetEntityWithCurrentItem(Entity target) {
      if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
         return;
      }

      net.minecraft.world.entity.player.Player attacker =
         buildcraft.api.core.BuildCraftAPI.fakePlayerProvider.getFakePlayer(serverLevel, this.getOwner(), target.blockPosition());
      if (!buildcraft.lib.misc.AttackEntityCompat.canAttack(serverLevel, attacker, target)) {
         return;
      }

      float damage = 1.0F;
      ItemStack weapon = this.itemInUse;
      if (!weapon.isEmpty()) {
         damage = weapon.is(net.minecraft.tags.ItemTags.SWORDS) ? 6.0F : 3.0F;
         if (weapon.isDamageableItem()) {
            weapon.hurtAndBreak(1, this, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            if (weapon.isEmpty()) {
               this.setItemInUse(ItemStack.EMPTY);
            }
         }
      }

      //? if >= 1.21.10 {
      target.hurtServer(serverLevel, this.damageSources().mobAttack(this), damage);
      //?} else {
      /*target.hurt(this.damageSources().mobAttack(this), damage);
      *///?}
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

   public net.minecraft.resources.Identifier getTexture() {
      String path = this.entityData.get(DATA_TEXTURE);
      if (path == null || path.isEmpty()) {
         return DEFAULT_TEXTURE;
      }

      net.minecraft.resources.Identifier parsed = net.minecraft.resources.Identifier.tryParse(path);
      return parsed != null ? parsed : DEFAULT_TEXTURE;
   }

   public ItemStack getRenderItem() {
      return this.entityData.get(DATA_ITEM);
   }

   public float getRenderAimYaw() {
      return this.entityData.get(DATA_AIM_YAW);
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

   // Entity#interact gained a Vec3 hit-location parameter in the 26.x line; older nodes are 2-arg.
   //? if >= 26.1 {
   @Override
   public InteractionResult interact(Player player, InteractionHand hand, Vec3 hitPos) {
      return this.wrenchInteract(player, hand);
   }
   //?} else {
   /*@Override
   public InteractionResult interact(Player player, InteractionHand hand) {
      return this.wrenchInteract(player, hand);
   }
   *///?}

   /**
    * Wrench right-click removes the robot. Robots are invulnerable (hurtServer returns false), so without this there
    * is no way to remove a placed one -- breaking its station just leaves it hanging. Rebuild the robot item from
    * its board + stored energy, hand back whatever it was carrying (equipped tool + collected drops), then discard
    * it: remove() routes through the registry (killRobot) so the station it held frees up. Non-wrench = PASS.
    */
   private InteractionResult wrenchInteract(Player player, InteractionHand hand) {
      ItemStack held = player.getItemInHand(hand);
      if (!EntityUtil.isWrench(held)) {
         return InteractionResult.PASS;
      }

      if (this.level().isClientSide()) {
         return InteractionResult.SUCCESS;
      }

      RedstoneBoardRobot boardRobot = this.getBoard();
      RedstoneBoardRobotNBT boardNBT = boardRobot != null ? boardRobot.getNBTHandler() : null;
      if (boardNBT == null) {
         return InteractionResult.PASS;
      }

      this.giveOrDrop(player, ItemRobot.createRobotStack(boardNBT, this.getBattery().getStored()));
      if (!this.itemInUse.isEmpty()) {
         this.giveOrDrop(player, this.itemInUse.copy());
      }

      for (ItemStack stack : this.inv) {
         if (stack != null && !stack.isEmpty()) {
            this.giveOrDrop(player, stack.copy());
         }
      }

      EntityUtil.wrenchUsed(player, hand, held, null);
      this.discard();
      return InteractionResult.SUCCESS;
   }

   private void giveOrDrop(Player player, ItemStack stack) {
      if (!player.getInventory().add(stack)) {
         player.drop(stack, false);
      }
   }

   /**
    * Destroy-the-station removal: drop the robot (its board + stored energy, plus whatever tool/loot it carried) as
    * item entities at its position and discard it. Called when the robot's home (main) station is broken, so that
    * leaves a picked-up-able board on the ground instead of a hanging, homeless robot.
    */
   public void dropAsItemAndDiscard() {
      if (!this.level().isClientSide()) {
         RedstoneBoardRobot boardRobot = this.getBoard();
         RedstoneBoardRobotNBT boardNBT = boardRobot != null ? boardRobot.getNBTHandler() : null;
         if (boardNBT != null) {
            this.dropStack(ItemRobot.createRobotStack(boardNBT, this.getBattery().getStored()));
            if (!this.itemInUse.isEmpty()) {
               this.dropStack(this.itemInUse.copy());
            }

            for (ItemStack stack : this.inv) {
               if (stack != null && !stack.isEmpty()) {
                  this.dropStack(stack.copy());
               }
            }
         }
      }

      this.discard();
   }

   private void dropStack(ItemStack stack) {
      ItemEntity item = new ItemEntity(this.level(), this.getX(), this.getY() + 0.25, this.getZ(), stack);
      item.setDefaultPickUpDelay();
      this.level().addFreshEntity(item);
   }

   @Override
   public void remove(Entity.RemovalReason reason) {
      if (!this.level().isClientSide()) {
         if (reason == Entity.RemovalReason.UNLOADED_TO_CHUNK) {
            this.onChunkUnload();
         } else if (this.robotId != NULL_ROBOT_ID) {
            this.getRegistry().killRobot(this);
         }
      }

      super.remove(reason);
   }

   //? if >= 1.21.10 {
   @Override
   protected void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      this.writeData(new BcValueOut(output));
   }
   //?} else {
   /*@Override
   public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      this.writeData(new BcValueOut(tag, this.registryAccess()));
   }
   *///?}

   protected void writeData(BcValueOut output) {
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
      this.fluidTank.serialize(output.child("fluidTank"));
      if (!this.itemInUse.isEmpty()) {
         output.store("itemInUse", ItemStack.CODEC, this.itemInUse);
      }

      GameProfile owner = this.getOwner();
      if (owner != null && BcAuth.id(owner) != null) {
         output.putString("ownerUUID", BcAuth.id(owner).toString());
         if (BcAuth.name(owner) != null) {
            output.putString("ownerName", BcAuth.name(owner));
         }
      }

      this.writeStationNBT(output, "linkedStation", this.linkedStationPos, this.linkedStationSide);
      this.writeStationNBT(output, "currentStation", this.currentDockingStationPos, this.currentDockingStationSide);

      if (this.mainAI != null) {
         CompoundTag aiTag = new CompoundTag();
         this.mainAI.writeToNBT(aiTag);
         output.store("mainAI", CompoundTag.CODEC, aiTag);
      }
   }

   private void writeStationNBT(BcValueOut output, String key, BlockPos pos, Direction side) {
      if (pos != null && side != null) {
         CompoundTag tag = new CompoundTag();
         tag.putIntArray("pos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
         tag.putByte("side", (byte) side.ordinal());
         output.store(key, CompoundTag.CODEC, tag);
      }
   }

   //? if >= 1.21.10 {
   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      this.readData(new BcValueIn(input));
   }
   //?} else {
   /*@Override
   public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.readData(new BcValueIn(tag, this.registryAccess()));
   }
   *///?}

   protected void readData(BcValueIn input) {
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
      input.child("fluidTank").ifPresent(v -> this.fluidTank.deserialize(v));
      input.read("itemInUse", ItemStack.CODEC).ifPresent(stack -> this.itemInUse = stack);
      String ownerUuid = input.getStringOr("ownerUUID", "");
      if (!ownerUuid.isEmpty()) {
         try {
            this.setOwner(new GameProfile(UUID.fromString(ownerUuid), input.getStringOr("ownerName", "Unknown")));
         } catch (IllegalArgumentException e) {
            this.setOwner(null);
         }
      }
      this.readStationNBT(input, "linkedStation", true);
      this.readStationNBT(input, "currentStation", false);
      input.read("mainAI", CompoundTag.CODEC).ifPresent(tag -> {
         AIRobot loaded = AIRobot.loadAI(tag, this);
         if (loaded instanceof AIRobotMain main) {
            this.mainAI = main;
         }
      });
   }

   private void readStationNBT(BcValueIn input, String key, boolean linked) {
      input.read(key, CompoundTag.CODEC).ifPresent(tag -> {
         int[] pos = BcNbt.getIntArray(tag, "pos");
         int sideOrdinal = BcNbt.getByte(tag, "side", (byte) 0);
         if (pos.length == 3 && sideOrdinal >= 0 && sideOrdinal < Direction.values().length) {
            BlockPos blockPos = new BlockPos(pos[0], pos[1], pos[2]);
            Direction side = Direction.values()[sideOrdinal];
            if (linked) {
               this.linkedStationPos = blockPos;
               this.linkedStationSide = side;
            } else {
               this.currentDockingStationPos = blockPos;
               this.currentDockingStationSide = side;
            }
         }
      });
   }

   //? if >= 1.21.10 {
   @Override
   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      return false;
   }
   //?} else {
   /*@Override
   public boolean hurt(DamageSource source, float amount) {
      return false;
   }
   *///?}

   //? if < 1.21.10 {
   /*@Override
   public Iterable<ItemStack> getArmorSlots() {
      return java.util.Collections.emptyList();
   }
   *///?}

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
