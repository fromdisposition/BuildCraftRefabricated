/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.api.schematics.SchematicEntityContext;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.RotationUtil;

public class SchematicEntityDefault implements ISchematicEntity {
    private CompoundTag entityNbt;
    private Vec3 pos;
    private BlockPos hangingPos;
    private Direction hangingFacing;
    private Rotation entityRotation = Rotation.NONE;

    public static boolean predicate(SchematicEntityContext context) {
        Identifier registryName = BuiltInRegistries.ENTITY_TYPE.getKey(context.entity.getType());
        if (registryName == null) return false;
        if (!RulesLoader.READ_DOMAINS.contains(registryName.getNamespace())) return false;
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, context.world.registryAccess());
        context.entity.save(output);
        CompoundTag entityNbt = output.buildResult();
        return RulesLoader.getRules(registryName, entityNbt)
            .stream()
            .anyMatch(rule -> rule.capture);
    }

    @Override
    public void init(SchematicEntityContext context) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, context.world.registryAccess());
        context.entity.save(output);
        entityNbt = output.buildResult();
        pos = context.entity.position().subtract(Vec3.atLowerCornerOf(context.basePos));
        if (context.entity instanceof HangingEntity hangingEntity) {
            hangingPos = hangingEntity.getPos().subtract(context.basePos);
            hangingFacing = hangingEntity.getDirection();
        } else {
            hangingPos = BlockPos.containing(pos);
            hangingFacing = Direction.NORTH;
        }
    }

    @Override
    public Vec3 getPos() {
        return pos;
    }

    @Nonnull
    @Override
    public List<ItemStack> computeRequiredItems() {
        Identifier entityId = Identifier.parse(entityNbt.getStringOr("id", ""));
        Set<JsonRule> rules = RulesLoader.getRules(entityId, entityNbt);
        if (rules.isEmpty()) {
            return Collections.emptyList();
        }
        return rules.stream()
            .map(rule -> rule.requiredExtractors)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .flatMap(extractor -> extractor.extractItemsFromEntity(entityNbt).stream())
            .filter(stack -> !stack.isEmpty())
            .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public List<FluidStack> computeRequiredFluids() {
        Identifier entityId = Identifier.parse(entityNbt.getStringOr("id", ""));
        Set<JsonRule> rules = RulesLoader.getRules(entityId, entityNbt);
        return rules.stream()
            .map(rule -> rule.requiredExtractors)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .flatMap(extractor -> extractor.extractFluidsFromEntity(entityNbt).stream())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public SchematicEntityDefault getRotated(Rotation rotation) {
        SchematicEntityDefault schematicEntity = SchematicEntityManager.createCleanCopy(this);
        schematicEntity.entityNbt = entityNbt;
        schematicEntity.pos = RotationUtil.rotateVec3(pos, rotation);
        schematicEntity.hangingPos = hangingPos.rotate(rotation);
        schematicEntity.hangingFacing = rotation.rotate(hangingFacing);
        schematicEntity.entityRotation = entityRotation.getRotated(rotation);
        return schematicEntity;
    }

    @Override
    @Nullable
    public Entity build(Level level, BlockPos basePos) {
        Vec3 placePos = Vec3.atLowerCornerOf(basePos).add(pos);
        BlockPos placeHangingPos = basePos.offset(hangingPos);
        CompoundTag newEntityNbt = entityNbt.copy();
        newEntityNbt.put("Pos", NBTUtilBC.writeVec3(placePos));
        NBTUtilBC.putUUID(newEntityNbt, "UUID", UUID.randomUUID());
        boolean rotate = true;
        if (newEntityNbt.contains("TileX") &&
            newEntityNbt.contains("TileY") &&
            newEntityNbt.contains("TileZ") &&
            newEntityNbt.contains("Facing")) {
            newEntityNbt.putInt("TileX", placeHangingPos.getX());
            newEntityNbt.putInt("TileY", placeHangingPos.getY());
            newEntityNbt.putInt("TileZ", placeHangingPos.getZ());
            newEntityNbt.putByte("Facing", (byte) hangingFacing.get2DDataValue());
            rotate = false;
        }

        Optional<Entity> optEntity = EntityType.create(
            TagValueInput.create(
                ProblemReporter.DISCARDING,
                level.registryAccess(),
                newEntityNbt
            ),
            level,
            EntitySpawnReason.COMMAND
        );
        if (optEntity.isPresent()) {
            Entity entity = optEntity.get();
            if (rotate && entityRotation != Rotation.NONE) {
                float yaw = entity.getYRot();
                float rotatedYaw = entity.rotate(entityRotation);
                entity.setYRot(yaw + (yaw - rotatedYaw));
            }
            level.addFreshEntity(entity);
            return entity;
        }
        return null;
    }

    @Override
    @Nullable
    public Entity buildWithoutChecks(Level level, BlockPos basePos) {
        return build(level, basePos);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("entityNbt", entityNbt);
        nbt.put("pos", NBTUtilBC.writeVec3(pos));
        nbt.put("hangingPos", NBTUtilBC.writeBlockPos(hangingPos));
        nbt.put("hangingFacing", NBTUtilBC.writeEnum(hangingFacing));
        nbt.put("entityRotation", NBTUtilBC.writeEnum(entityRotation));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
        entityNbt = nbt.getCompoundOrEmpty("entityNbt");
        pos = NBTUtilBC.readVec3(nbt.get("pos"));
        if (pos == null) {
            pos = Vec3.ZERO;
        }
        hangingPos = NBTUtilBC.readBlockPos(nbt.getCompoundOrEmpty("hangingPos"));
        hangingFacing = NBTUtilBC.readEnum(nbt.get("hangingFacing"), Direction.class);
        if (hangingFacing == null) hangingFacing = Direction.NORTH;
        entityRotation = NBTUtilBC.readEnum(nbt.get("entityRotation"), Rotation.class);
        if (entityRotation == null) entityRotation = Rotation.NONE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchematicEntityDefault that = (SchematicEntityDefault) o;
        return entityNbt.equals(that.entityNbt) &&
            pos.equals(that.pos) &&
            hangingPos.equals(that.hangingPos) &&
            hangingFacing == that.hangingFacing &&
            entityRotation == that.entityRotation;
    }

    @Override
    public int hashCode() {
        int result = entityNbt.hashCode();
        result = 31 * result + pos.hashCode();
        result = 31 * result + hangingPos.hashCode();
        result = 31 * result + hangingFacing.hashCode();
        result = 31 * result + entityRotation.hashCode();
        return result;
    }
}
