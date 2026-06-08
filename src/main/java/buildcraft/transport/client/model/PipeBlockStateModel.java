/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import java.util.List;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.util.RandomSource;

public class PipeBlockStateModel implements BlockStateModel {
   private final BlockStateModel vanillaDelegate;

   public PipeBlockStateModel(BlockStateModel vanillaDelegate) {
      this.vanillaDelegate = vanillaDelegate;
   }

   public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
      this.vanillaDelegate.collectParts(random, parts);
   }

   public Baked particleMaterial() {
      return this.vanillaDelegate.particleMaterial();
   }

   public int materialFlags() {
      return this.vanillaDelegate.materialFlags();
   }
}
