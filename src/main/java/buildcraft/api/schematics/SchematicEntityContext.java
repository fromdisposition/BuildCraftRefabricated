/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.schematics;

import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class SchematicEntityContext {
   @Nonnull
   public final Level world;
   @Nonnull
   public final BlockPos basePos;
   @Nonnull
   public final Entity entity;

   public SchematicEntityContext(@Nonnull Level world, @Nonnull BlockPos basePos, @Nonnull Entity entity) {
      this.world = world;
      this.basePos = basePos;
      this.entity = entity;
   }
}
