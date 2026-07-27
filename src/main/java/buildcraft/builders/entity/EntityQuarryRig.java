/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
//? if >= 1.21.10 {
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?}
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityQuarryRig extends Entity {
   private static final EntityDataAccessor<Boolean> PHASING = SynchedEntityData.defineId(EntityQuarryRig.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Float> SIZE_X = SynchedEntityData.defineId(EntityQuarryRig.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> SIZE_Y = SynchedEntityData.defineId(EntityQuarryRig.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> SIZE_Z = SynchedEntityData.defineId(EntityQuarryRig.class, EntityDataSerializers.FLOAT);
   public boolean phasing = false;

   public EntityQuarryRig(EntityType<?> type, Level level) {
      super(type, level);
      this.noPhysics = true;
      // Never frustum-cull this invisible collision rig. After the section-split its boxes can be small, and the
      // camera leaving one would otherwise drop its F3+B debug hitbox (collision is unaffected — it is server-side
      // + a section query, never frustum-based). On >= 1.21.10 the renderer's affectedByCulling()=false does this
      // (set at registration in BCBuildersFabricClient); 1.21.1 has no such renderer hook, so use Entity.noCulling.
      //? if < 1.21.10 {
      /*this.noCulling = true;
      *///?}
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(PHASING, false);
      builder.define(SIZE_X, 0.0F);
      builder.define(SIZE_Y, 0.0F);
      builder.define(SIZE_Z, 0.0F);
   }

   //? if >= 1.21.10 {
   protected void readAdditionalSaveData(ValueInput input) {
   }

   protected void addAdditionalSaveData(ValueOutput output) {
   }
   //?} else {
   /*public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
   }

   public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
   }
   *///?}

   public boolean shouldBeSaved() {
      return false;
   }

   //? if >= 1.21.10 {
   protected AABB makeBoundingBox(Vec3 position) {
      float halfX = (Float)this.entityData.get(SIZE_X) / 2.0F;
      float halfY = (Float)this.entityData.get(SIZE_Y) / 2.0F;
      float halfZ = (Float)this.entityData.get(SIZE_Z) / 2.0F;
      return halfX <= 0.0F
         ? super.makeBoundingBox(position)
         : new AABB(position.x - halfX, position.y - halfY, position.z - halfZ, position.x + halfX, position.y + halfY, position.z + halfZ);
   }
   //?} else {
   /*protected AABB makeBoundingBox() {
      Vec3 position = this.position();
      float halfX = (Float)this.entityData.get(SIZE_X) / 2.0F;
      float halfY = (Float)this.entityData.get(SIZE_Y) / 2.0F;
      float halfZ = (Float)this.entityData.get(SIZE_Z) / 2.0F;
      return halfX <= 0.0F
         ? super.makeBoundingBox()
         : new AABB(position.x - halfX, position.y - halfY, position.z - halfZ, position.x + halfX, position.y + halfY, position.z + halfZ);
   }
   *///?}

   //? if >= 1.21.10 {
   public boolean canBeCollidedWith(Entity other) {
      return !this.phasing;
   }
   //?} else {
   /*public boolean canBeCollidedWith() {
      return !this.phasing;
   }
   *///?}

   public boolean isPickable() {
      return !this.phasing;
   }

   public boolean isPushable() {
      return false;
   }

   // The tile repositions this collision rig every server tick to follow the moving drill/frame. By default the
   // client interpolates an entity's synced position over DEFAULT_INTERPOLATION_STEPS (3) ticks, so the collision
   // box LAGGED ~3 ticks behind the smoothly rendered gantry — the player walking on the moving part fell through
   // where the box had not caught up yet. Snap the rig straight to each synced position so collision tracks tight.
   //? if >= 1.21.10 {
   private final InterpolationHandler interpolation = new InterpolationHandler(this, 0);

   public InterpolationHandler getInterpolation() {
      return this.interpolation;
   }
   //?} else {
   /*public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
      this.setPos(x, y, z);
   }
   *///?}

   //? if >= 1.21.10 {
   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      return false;
   }
   //?} else {
   /*public boolean hurt(DamageSource source, float amount) {
      return false;
   }
   *///?}

   public void tick() {
      super.tick();
      if (this.level().isClientSide()) {
         this.phasing = (Boolean)this.entityData.get(PHASING);
      }
   }

   public void setPhasing(boolean phase) {
      this.phasing = phase;
      this.entityData.set(PHASING, phase);
   }

   public void setRiggingBox(AABB aabb) {
      this.entityData.set(SIZE_X, (float)(aabb.maxX - aabb.minX));
      this.entityData.set(SIZE_Y, (float)(aabb.maxY - aabb.minY));
      this.entityData.set(SIZE_Z, (float)(aabb.maxZ - aabb.minZ));
      this.setPos((aabb.minX + aabb.maxX) / 2.0, (aabb.minY + aabb.maxY) / 2.0, (aabb.minZ + aabb.maxZ) / 2.0);
   }
}
