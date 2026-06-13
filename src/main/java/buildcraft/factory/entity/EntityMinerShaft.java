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

/** Invisible collider for miner/pump shafts — same pattern as {@link buildcraft.builders.entity.EntityQuarryRig}. */
public class EntityMinerShaft extends Entity {
   private static final EntityDataAccessor<Float> SIZE_X = SynchedEntityData.defineId(EntityMinerShaft.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> SIZE_Y = SynchedEntityData.defineId(EntityMinerShaft.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> SIZE_Z = SynchedEntityData.defineId(EntityMinerShaft.class, EntityDataSerializers.FLOAT);

   public EntityMinerShaft(EntityType<?> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      builder.define(SIZE_X, 0.0F);
      builder.define(SIZE_Y, 0.0F);
      builder.define(SIZE_Z, 0.0F);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput input) {
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput output) {
   }

   @Override
   public boolean shouldBeSaved() {
      return false;
   }

   @Override
   protected AABB makeBoundingBox(Vec3 position) {
      float halfX = this.entityData.get(SIZE_X) / 2.0F;
      float halfY = this.entityData.get(SIZE_Y) / 2.0F;
      float halfZ = this.entityData.get(SIZE_Z) / 2.0F;
      return halfX <= 0.0F
         ? super.makeBoundingBox(position)
         : new AABB(position.x - halfX, position.y - halfY, position.z - halfZ, position.x + halfX, position.y + halfY, position.z + halfZ);
   }

   @Override
   public boolean canBeCollidedWith(Entity other) {
      return true;
   }

   @Override
   public boolean isPickable() {
      return true;
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      return false;
   }

   public void setShaftBox(AABB box) {
      this.entityData.set(SIZE_X, (float)(box.maxX - box.minX));
      this.entityData.set(SIZE_Y, (float)(box.maxY - box.minY));
      this.entityData.set(SIZE_Z, (float)(box.maxZ - box.minZ));
      this.setPos((box.minX + box.maxX) / 2.0, (box.minY + box.maxY) / 2.0, (box.minZ + box.maxZ) / 2.0);
   }
}
