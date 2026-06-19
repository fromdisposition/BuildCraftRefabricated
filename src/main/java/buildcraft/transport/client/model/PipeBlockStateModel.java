/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import java.util.List;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
//? if >= 26.1 {
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material.Baked;
//?} else {
/*import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
*///?}
import net.minecraft.util.RandomSource;

public class PipeBlockStateModel implements BlockStateModel {
   private final BlockStateModel vanillaDelegate;

   public PipeBlockStateModel(BlockStateModel vanillaDelegate) {
      this.vanillaDelegate = vanillaDelegate;
   }

   //? if >= 26.1 {
   public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
   //?} else {
   /*public void collectParts(RandomSource random, List<BlockModelPart> parts) {
   *///?}
      this.vanillaDelegate.collectParts(random, parts);
   }

   //? if >= 26.1 {
   public Baked particleMaterial() {
      return this.vanillaDelegate.particleMaterial();
   }

   public int materialFlags() {
      return this.vanillaDelegate.materialFlags();
   }
   //?} else {
   /*// 1.21.x BlockStateModel exposes particleIcon() (a sprite) and has no materialFlags().
   public TextureAtlasSprite particleIcon() {
      return this.vanillaDelegate.particleIcon();
   }
   *///?}
}
