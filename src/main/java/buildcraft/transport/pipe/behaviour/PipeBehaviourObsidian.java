/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.IPipeEventBus;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.VecUtil;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PipeBehaviourObsidian extends PipeBehaviour implements IMjRedstoneReceiver {
   private static final long POWER_PER_ITEM = MjAPI.MJ / 2L;
   private static final long POWER_PER_METRE = MjAPI.MJ / 4L;
   private static final double INSERT_SPEED = 0.04;
   private static final int DROP_GAP = 20;
   private final WeakHashMap<ItemEntity, Long> entityDropTime = new WeakHashMap<>();
   private int toWaitTicks = 0;
   private BlockPos lastPos = null;
   private AABB collisionBoxCache = null;
   private Vec3 pipeCenterCache = null;

   public PipeBehaviourObsidian(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourObsidian(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
      this.toWaitTicks = 20;
   }

   @Override
   public boolean hasSimulationWork() {
      return !this.pipe.getHolder().getPipeWorld().isClientSide() && (this.toWaitTicks > 0 || this.getOpenFace() != null);
   }

   @Override
   public void onTick() {
      if (!this.pipe.getHolder().getPipeWorld().isClientSide()) {
         this.toWaitTicks--;
         if (this.toWaitTicks <= 0) {
            this.toWaitTicks = 0;
            Direction openFace = this.getOpenFace();
            if (openFace != null) {
               BlockPos pos = this.pipe.getHolder().getPipePos();
               if (this.lastPos == null || !this.lastPos.equals(pos)) {
                  this.lastPos = pos;
                  this.collisionBoxCache = new AABB(pos);
                  this.pipeCenterCache = Vec3.atCenterOf(pos);
               }

               // Obsidian pipes never sleep (an open face keeps hasSimulationWork true), so back the entity
               // query off to every 8 ticks while it keeps coming up empty; any hit resets to per-tick.
               if (!this.suckScanBackoff || (this.pipe.getHolder().getPipeWorld().getGameTime() & 7L) == 0L) {
                  var found = this.pipe.getHolder().getPipeWorld().getEntitiesOfClass(ItemEntity.class, this.collisionBoxCache, Entity::isAlive);
                  this.suckScanBackoff = found.isEmpty();

                  for (ItemEntity entity : found) {
                     this.trySuckEntity(entity, openFace, Long.MAX_VALUE, false);
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean canConnect(Direction face, PipeBehaviour other) {
      return !(other instanceof PipeBehaviourObsidian);
   }

   private Direction getOpenFace() {
      Direction openFace = null;

      for (Direction face : Direction.values()) {
         if (this.pipe.isConnected(face)) {
            if (openFace != null) {
               return null;
            }

            openFace = face.getOpposite();
         }
      }

      return openFace;
   }

   protected AABB getSuckingBox(Direction openFace, int distance) {
      AABB bb = BoundingBoxUtil.makeAround(VecUtil.convertCenter(this.pipe.getHolder().getPipePos()), 0.4);

      return switch (openFace) {
         case WEST -> bb.move(-distance, 0.0, 0.0).inflate(0.5, distance, distance);
         case EAST -> bb.move(distance, 0.0, 0.0).inflate(0.5, distance, distance);
         case DOWN -> bb.move(0.0, -distance, 0.0).inflate(distance, 0.5, distance);
         case UP -> bb.move(0.0, distance, 0.0).inflate(distance, 0.5, distance);
         case NORTH -> bb.move(0.0, 0.0, -distance).inflate(distance, distance, 0.5);
         case SOUTH -> bb.move(0.0, 0.0, distance).inflate(distance, distance, 0.5);
         default -> throw new MatchException(null, null);
      };
   }

   protected long trySuckEntity(Entity entity, Direction faceFrom, long power, boolean simulate) {
      if (entity.isAlive() && !(entity instanceof LivingEntity)) {
         if (entity instanceof ItemEntity itemEntity) {
            Long tickPickupObj = this.entityDropTime.get(itemEntity);
            if (tickPickupObj != null) {
               long tickNow = this.pipe.getHolder().getPipeWorld().getGameTime();
               if (tickNow < tickPickupObj) {
                  return power;
               }

               this.entityDropTime.remove(itemEntity);
            }
         }

         if (!(this.pipe.getFlow() instanceof IFlowItems flowItems)) {
            return power;
         } else if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty()) {
               return power;
            }

            long powerReqPerItem;
            int max;
            if (power == Long.MAX_VALUE) {
               max = Integer.MAX_VALUE;
               powerReqPerItem = 0L;
            } else {
               BlockPos pos = this.pipe.getHolder().getPipePos();
               if (this.lastPos == null || !this.lastPos.equals(pos)) {
                  this.lastPos = pos;
                  this.collisionBoxCache = new AABB(pos);
                  this.pipeCenterCache = Vec3.atCenterOf(pos);
               }

               double distance = Math.sqrt(entity.distanceToSqr(this.pipeCenterCache));
               powerReqPerItem = (long)(Math.max(1.0, distance) * POWER_PER_METRE + POWER_PER_ITEM);
               max = (int)(power / powerReqPerItem);
            }

            if (max <= 0) {
               return power;
            }

            int toExtract = Math.min(stack.getCount(), max);
            ItemStack extracted = stack.copyWithCount(toExtract);
            if (!simulate) {
               if (toExtract >= stack.getCount()) {
                  itemEntity.discard();
               } else {
                  stack.shrink(toExtract);
                  itemEntity.setItem(stack);
               }

               flowItems.insertItemsForce(extracted, faceFrom, null, 0.04);
            }

            return power - powerReqPerItem * toExtract;
         } else {
            return power;
         }
      } else {
         return power;
      }
   }

   public void onPipeDrop(PipeEventItem.Drop drop) {
      this.entityDropTime.put(drop.getEntity(), this.pipe.getHolder().getPipeWorld().getGameTime() + 20L);
   }

   @Override
   public void registerEventHandlers(IPipeEventBus bus) {
      bus.on(PipeEventItem.Drop.class, this, this::onPipeDrop);
   }

   @Override
   public boolean canConnect(@Nonnull IMjConnector other) {
      return true;
   }

   private boolean suckScanBackoff;
   private long lastRequestedPower;
   private long lastRequestedTick = Long.MIN_VALUE;

   @Override
   public long getPowerRequested() {
      // Engines poll this every tick, and answering honestly runs up to four escalating entity AABB queries.
      // Memoize for 4 ticks; the pipe's own suction cadence hides the difference.
      long now = this.pipe.getHolder().getPipeWorld().getGameTime();
      if (now - this.lastRequestedTick < 4L) {
         return this.lastRequestedPower;
      }

      this.lastRequestedTick = now;
      long power = 512L * MjAPI.MJ;
      this.lastRequestedPower = power - this.receivePower(power, true);
      return this.lastRequestedPower;
   }

   @Override
   public long receivePower(long microJoules, boolean simulate) {
      if (this.toWaitTicks > 0) {
         return microJoules;
      }

      Direction openFace = this.getOpenFace();
      if (openFace == null) {
         return microJoules;
      }

      for (int d = 1; d < 5; d++) {
         AABB aabb = this.getSuckingBox(openFace, d);

         for (ItemEntity entity : this.pipe.getHolder().getPipeWorld().getEntitiesOfClass(ItemEntity.class, aabb, Entity::isAlive)) {
            long leftOver = this.trySuckEntity(entity, openFace, microJoules, simulate);
            if (leftOver < microJoules) {
               return leftOver;
            }
         }
      }

      return microJoules - MjAPI.MJ;
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T> T getCapability(@Nonnull Object capability, Direction facing) {
      return (T)(capability != MjAPI.CAP_RECEIVER && capability != MjAPI.CAP_CONNECTOR && capability != MjAPI.CAP_REDSTONE_RECEIVER
         ? super.getCapability(capability, facing)
         : this);
   }
}
