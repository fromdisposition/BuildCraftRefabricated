/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityMinerShaft extends Entity {
   private static final EntityDataAccessor<Float> SIZE_X = SynchedEntityData.defineId(EntityMinerShaft.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> SIZE_Y = SynchedEntityData.defineId(EntityMinerShaft.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> SIZE_Z = SynchedEntityData.defineId(EntityMinerShaft.class, EntityDataSerializers.FLOAT);

   public EntityMinerShaft(EntityType<?> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(SIZE_X, 0.0F);
      builder.define(SIZE_Y, 0.0F);
      builder.define(SIZE_Z, 0.0F);
   }

   protected void readAdditionalSaveData(ValueInput input) {
   }

   protected void addAdditionalSaveData(ValueOutput output) {
   }

   public boolean shouldBeSaved() {
      return false;
   }

   protected AABB makeBoundingBox(Vec3 position) {
      float halfX = (Float)this.entityData.get(SIZE_X) / 2.0F;
      float halfY = (Float)this.entityData.get(SIZE_Y) / 2.0F;
      float halfZ = (Float)this.entityData.get(SIZE_Z) / 2.0F;
      return halfX <= 0.0F
         ? super.makeBoundingBox(position)
         : new AABB(position.x - halfX, position.y - halfY, position.z - halfZ, position.x + halfX, position.y + halfY, position.z + halfZ);
   }

   public boolean canBeCollidedWith(Entity other) {
      return true;
   }

   public boolean isPickable() {
      return true;
   }

   public boolean isPushable() {
      return false;
   }

   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      return false;
   }

   public void setCollisionBox(AABB aabb) {
      this.entityData.set(SIZE_X, (float)(aabb.maxX - aabb.minX));
      this.entityData.set(SIZE_Y, (float)(aabb.maxY - aabb.minY));
      this.entityData.set(SIZE_Z, (float)(aabb.maxZ - aabb.minZ));
      this.setPos((aabb.minX + aabb.maxX) / 2.0, (aabb.minY + aabb.maxY) / 2.0, (aabb.minZ + aabb.maxZ) / 2.0);
   }
}
