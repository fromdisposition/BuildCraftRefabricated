/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.api.schematics.SchematicEntityContext;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.RotationUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import buildcraft.lib.nbt.BcEntityNbt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;

public class SchematicEntityDefault implements ISchematicEntity {
   private CompoundTag entityNbt;
   private Vec3 pos;
   private BlockPos hangingPos;
   private Direction hangingFacing;
   private Rotation entityRotation = Rotation.NONE;

   public static boolean predicate(SchematicEntityContext context) {
      Identifier registryName = BuiltInRegistries.ENTITY_TYPE.getKey(context.entity.getType());
      if (registryName == null) {
         return false;
      }

      if (!RulesLoader.READ_DOMAINS.contains(registryName.getNamespace())) {
         return false;
      }

      CompoundTag entityNbt = BcEntityNbt.save(context.entity, context.world.registryAccess());
      return RulesLoader.getRules(registryName, entityNbt).stream().anyMatch(rule -> rule.capture);
   }

   @Override
   public void init(SchematicEntityContext context) {
      this.entityNbt = BcEntityNbt.save(context.entity, context.world.registryAccess());
      this.pos = context.entity.position().subtract(Vec3.atLowerCornerOf(context.basePos));
      if (context.entity instanceof HangingEntity hangingEntity) {
         this.hangingPos = hangingEntity.getPos().subtract(context.basePos);
         this.hangingFacing = hangingEntity.getDirection();
      } else {
         this.hangingPos = BlockPos.containing(this.pos);
         this.hangingFacing = Direction.NORTH;
      }
   }

   @Override
   public Vec3 getPos() {
      return this.pos;
   }

   @Nonnull
   @Override
   public List<ItemStack> computeRequiredItems() {
      Identifier entityId = Identifier.parse(BcNbt.getString(this.entityNbt, "id", ""));
      Set<JsonRule> rules = RulesLoader.getRules(entityId, this.entityNbt);
      return rules.isEmpty()
         ? Collections.emptyList()
         : rules.stream()
            .map(rule -> rule.requiredExtractors)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .flatMap(extractor -> extractor.extractItemsFromEntity(this.entityNbt).stream())
            .filter(stack -> !stack.isEmpty())
            .collect(Collectors.toList());
   }

   @Nonnull
   @Override
   public List<FluidStack> computeRequiredFluids() {
      Identifier entityId = Identifier.parse(BcNbt.getString(this.entityNbt, "id", ""));
      Set<JsonRule> rules = RulesLoader.getRules(entityId, this.entityNbt);
      return rules.stream()
         .map(rule -> rule.requiredExtractors)
         .filter(Objects::nonNull)
         .flatMap(Collection::stream)
         .flatMap(extractor -> extractor.extractFluidsFromEntity(this.entityNbt).stream())
         .filter(Objects::nonNull)
         .collect(Collectors.toList());
   }

   public SchematicEntityDefault getRotated(Rotation rotation) {
      SchematicEntityDefault schematicEntity = SchematicEntityManager.createCleanCopy(this);
      schematicEntity.entityNbt = this.entityNbt;
      schematicEntity.pos = RotationUtil.rotateVec3(this.pos, rotation);
      schematicEntity.hangingPos = this.hangingPos.rotate(rotation);
      schematicEntity.hangingFacing = rotation.rotate(this.hangingFacing);
      schematicEntity.entityRotation = this.entityRotation.getRotated(rotation);
      return schematicEntity;
   }

   @Nullable
   @Override
   public Entity build(Level level, BlockPos basePos) {
      Vec3 placePos = Vec3.atLowerCornerOf(basePos).add(this.pos);
      BlockPos placeHangingPos = basePos.offset(this.hangingPos);
      CompoundTag newEntityNbt = this.entityNbt.copy();
      newEntityNbt.put("Pos", NBTUtilBC.writeVec3(placePos));
      NBTUtilBC.putUUID(newEntityNbt, "UUID", UUID.randomUUID());
      boolean rotate = true;
      if (newEntityNbt.contains("TileX") && newEntityNbt.contains("TileY") && newEntityNbt.contains("TileZ") && newEntityNbt.contains("Facing")) {
         newEntityNbt.putInt("TileX", placeHangingPos.getX());
         newEntityNbt.putInt("TileY", placeHangingPos.getY());
         newEntityNbt.putInt("TileZ", placeHangingPos.getZ());
         newEntityNbt.putByte("Facing", (byte)this.hangingFacing.get2DDataValue());
         rotate = false;
      }

      Optional<Entity> optEntity = BcEntityNbt.create(newEntityNbt, level);
      if (optEntity.isPresent()) {
         Entity entity = optEntity.get();
         if (rotate && this.entityRotation != Rotation.NONE) {
            float yaw = entity.getYRot();
            float rotatedYaw = entity.rotate(this.entityRotation);
            entity.setYRot(yaw + (yaw - rotatedYaw));
         }

         level.addFreshEntity(entity);
         return entity;
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public Entity buildWithoutChecks(Level level, BlockPos basePos) {
      return this.build(level, basePos);
   }

   @Override
   public CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();
      nbt.put("entityNbt", this.entityNbt);
      nbt.put("pos", NBTUtilBC.writeVec3(this.pos));
      nbt.put("hangingPos", NBTUtilBC.writeBlockPos(this.hangingPos));
      nbt.put("hangingFacing", NBTUtilBC.writeEnum(this.hangingFacing));
      nbt.put("entityRotation", NBTUtilBC.writeEnum(this.entityRotation));
      return nbt;
   }

   @Override
   public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
      this.entityNbt = BcNbt.getCompound(nbt, "entityNbt");
      this.pos = NBTUtilBC.readVec3(nbt.get("pos"));
      if (this.pos == null) {
         this.pos = Vec3.ZERO;
      }

      this.hangingPos = NBTUtilBC.readBlockPos(BcNbt.getCompound(nbt, "hangingPos"));
      this.hangingFacing = NBTUtilBC.readEnum(nbt.get("hangingFacing"), Direction.class);
      if (this.hangingFacing == null) {
         this.hangingFacing = Direction.NORTH;
      }

      this.entityRotation = NBTUtilBC.readEnum(nbt.get("entityRotation"), Rotation.class);
      if (this.entityRotation == null) {
         this.entityRotation = Rotation.NONE;
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SchematicEntityDefault that = (SchematicEntityDefault)o;
         return this.entityNbt.equals(that.entityNbt)
            && this.pos.equals(that.pos)
            && this.hangingPos.equals(that.hangingPos)
            && this.hangingFacing == that.hangingFacing
            && this.entityRotation == that.entityRotation;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.entityNbt.hashCode();
      result = 31 * result + this.pos.hashCode();
      result = 31 * result + this.hangingPos.hashCode();
      result = 31 * result + this.hangingFacing.hashCode();
      return 31 * result + this.entityRotation.hashCode();
   }
}