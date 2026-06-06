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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(PHASING, false);
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
      return !this.phasing;
   }

   public boolean isPickable() {
      return !this.phasing;
   }

   public boolean isPushable() {
      return false;
   }

   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      return false;
   }

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
