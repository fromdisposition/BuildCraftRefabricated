/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.entity;

import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.item.ItemPackage;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
//? if >= 1.21.10 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?}
import net.minecraft.world.phys.Vec3;
import buildcraft.lib.nbt.BcValueIn;
import buildcraft.lib.nbt.BcValueOut;

public class EntityPackage extends Entity {
   private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(EntityPackage.class, EntityDataSerializers.ITEM_STACK);
   private static final int MAX_LIFETIME = 200;

   public EntityPackage(EntityType<?> type, Level level) {
      super(type, level);
   }

   public void setPackage(ItemStack stack) {
      this.entityData.set(DATA_ITEM, stack.copy());
   }

   public ItemStack getPackage() {
      return this.entityData.get(DATA_ITEM);
   }

   public void shootFrom(LivingEntity shooter, float velocity) {
      Vec3 look = shooter.getLookAngle();
      this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
      this.setDeltaMovement(look.scale(velocity));
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      builder.define(DATA_ITEM, new ItemStack(BCSiliconItems.PACKAGE));
   }

   @Override
   public void tick() {
      super.tick();
      Vec3 motion = this.getDeltaMovement().add(0.0, -0.04, 0.0).scale(0.99);
      this.setDeltaMovement(motion);
      this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());

      if (this.level().isClientSide()) {
         return;
      }

      boolean hitEntity = false;
      List<Entity> nearby = this.level().getEntities(this, this.getBoundingBox().inflate(0.1), e -> e instanceof LivingEntity && e.isAlive());
      if (!nearby.isEmpty()) {
         hitEntity = true;
      }

      if (hitEntity || this.onGround() || this.horizontalCollision || this.verticalCollision || this.tickCount > MAX_LIFETIME) {
         this.impact();
      }
   }

   private void impact() {
      ItemStack pkg = this.getPackage();

      for (int i = 0; i < ItemPackage.SLOTS; i++) {
         ItemStack content = ItemPackage.getStack(pkg, i);
         if (!content.isEmpty()) {
            ItemEntity item = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), content.copy());
            item.setPickUpDelay(10);
            this.level().addFreshEntity(item);
         }
      }

      this.discard();
   }

   //? if >= 1.21.10 {
   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      this.readData(new BcValueIn(input));
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput output) {
      this.writeData(new BcValueOut(output));
   }
   //?} else {
   /*@Override
   public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
      this.readData(new BcValueIn(tag, this.registryAccess()));
   }

   @Override
   public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
      this.writeData(new BcValueOut(tag, this.registryAccess()));
   }
   *///?}

   protected void readData(BcValueIn input) {
      input.read("package", ItemStack.CODEC).ifPresent(this::setPackage);
   }

   protected void writeData(BcValueOut output) {
      ItemStack pkg = this.getPackage();
      if (!pkg.isEmpty()) {
         output.store("package", ItemStack.CODEC, pkg);
      }
   }

   @Override
   public boolean shouldBeSaved() {
      return true;
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
}
