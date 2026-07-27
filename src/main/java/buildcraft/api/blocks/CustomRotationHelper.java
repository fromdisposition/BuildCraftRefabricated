/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.blocks;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public enum CustomRotationHelper {
   INSTANCE;

   private static final boolean DEBUG = BCDebugging.shouldDebugLog("api.rotation");
   private final Map<Block, List<ICustomRotationHandler>> handlers = Maps.newIdentityHashMap();

   public void registerHandlerForAll(Class<? extends Block> blockClass, ICustomRotationHandler handler) {
      for (Block block : BuiltInRegistries.BLOCK) {
         Class<? extends Block> foundClass = (Class<? extends Block>)block.getClass();
         if (blockClass.isAssignableFrom(foundClass)) {
            if (DEBUG) {
               BCLog.logger.info("[api.rotation] Found an assignable block " + block.toString() + " (" + foundClass + ") for " + blockClass);
            }

            this.registerHandlerInternal(block, handler);
         }
      }
   }

   public void registerHandler(Block block, ICustomRotationHandler handler) {
      if (this.registerHandlerInternal(block, handler)) {
         if (DEBUG) {
            BCLog.logger.info("[api.rotation] Setting a rotation handler for block " + block.toString());
         }
      } else if (DEBUG) {
         BCLog.logger.info("[api.rotation] Adding another rotation handler for block " + block.toString());
      }
   }

   private boolean registerHandlerInternal(Block block, ICustomRotationHandler handler) {
      if (!this.handlers.containsKey(block)) {
         List<ICustomRotationHandler> forBlock = Lists.newArrayList();
         forBlock.add(handler);
         this.handlers.put(block, forBlock);
         return true;
      } else {
         this.handlers.get(block).add(handler);
         return false;
      }
   }

   public InteractionResult attemptRotateBlock(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
      Block block = state.getBlock();
      if (block instanceof ICustomRotationHandler) {
         return ((ICustomRotationHandler)block).attemptRotation(world, pos, state, sideWrenched);
      }

      if (!this.handlers.containsKey(block)) {
         return InteractionResult.PASS;
      }

      for (ICustomRotationHandler handler : this.handlers.get(block)) {
         InteractionResult result = handler.attemptRotation(world, pos, state, sideWrenched);
         if (result != InteractionResult.PASS) {
            return result;
         }
      }

      return InteractionResult.PASS;
   }
}
