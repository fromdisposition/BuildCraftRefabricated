/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client;

import buildcraft.fabric.client.block.ClientBlockExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

/**
 * 1.21.1 stub (versions/1.21.1). The shared class draws custom pipe break/landing particles textured from the
 * pluggable sprites via the 1.21.5 item render-state pipeline (ItemStackRenderState/ItemModelResolver), which
 * does not exist on 1.21.1. Here the custom particle effects are disabled (vanilla block-break particles still
 * play); the block functions normally.
 */
public class PipeHolderClientExtensions implements ClientBlockExtensions {
   public static final PipeHolderClientExtensions INSTANCE = new PipeHolderClientExtensions();

   /** The client crosshair hit, if it is a block hit. Common return type so the caller stays dist-clean. */
   @Nullable
   public static BlockHitResult clientBlockHit() {
      return Minecraft.getInstance().hitResult instanceof BlockHitResult blockHit ? blockHit : null;
   }

   /** The local client player as the common {@link Player} type (hides the client-only LocalPlayer). */
   @Nullable
   public static Player clientPlayer() {
      return Minecraft.getInstance().player;
   }

   @Override
   public boolean addHitEffects(BlockState state, Level level, @Nullable HitResult target, ParticleEngine manager) {
      return false;
   }

   @Override
   public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
      return false;
   }

   public static boolean spawnRunningParticle(
      Level level, BlockPos pos, double entityX, double entityZ, double entityWidth, double motionX, double motionZ, double minY
   ) {
      return false;
   }

   public static void spawnLandingParticles(Level level, BlockPos pos, double x, double y, double z, int numberOfParticles) {
   }
}
