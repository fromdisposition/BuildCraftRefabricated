/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.nbt;

import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
//? if >= 1.21.10 {
import net.minecraft.world.entity.EntitySpawnReason;
//?}
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
//? if >= 1.21.10 {
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
//?}
//? if >= 26.2 {
import net.minecraft.world.entity.EntitySpawnRequest;
//?}

/**
 * Version-neutral entity NBT save/load. On 1.21.5+ entities serialize through the ValueInput/ValueOutput
 * system (TagValueOutput.save / EntityType.create(TagValueInput, ...)); on 1.21.1 they serialize straight
 * to a CompoundTag (Entity.save(CompoundTag) / EntityType.create(CompoundTag, Level)). Mirrors the other
 * lib.nbt facades ([[port-1-21-1]] BcValueIn/BcValueOut/BcNbt).
 */
public final class BcEntityNbt {
   private BcEntityNbt() {
   }

   /** Serialize an entity to a fresh CompoundTag. */
   public static CompoundTag save(Entity entity, RegistryAccess registryAccess) {
      //? if >= 1.21.10 {
      TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registryAccess);
      entity.save(output);
      return output.buildResult();
      //?} else {
      /*CompoundTag tag = new CompoundTag();
      entity.save(tag);
      return tag;
      *///?}
   }

   /** Reconstruct an entity from NBT into the given level (COMMAND spawn reason on modern). */
   public static Optional<Entity> create(CompoundTag nbt, Level level) {
      //? if >= 1.21.10 {
      return EntityType.create(
         TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), nbt), level,
         //? if >= 26.2 {
         new EntitySpawnRequest(EntitySpawnReason.COMMAND, false)
         //?} else {
         /*EntitySpawnReason.COMMAND
         *///?}
      );
      //?} else {
      /*return EntityType.create(nbt, level);
      *///?}
   }

   /** Load a CompoundTag (incl. components) into an existing block entity. */
   public static void loadBlockEntity(BlockEntity be, CompoundTag nbt, RegistryAccess registryAccess) {
      //? if >= 1.21.10 {
      be.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, registryAccess, nbt));
      //?} else {
      /*be.loadWithComponents(nbt, registryAccess);
      *///?}
   }
}
