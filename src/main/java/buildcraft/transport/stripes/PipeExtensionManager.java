/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeExtensionManager;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStripes;
import buildcraft.transport.tile.TilePipeHolder;
import buildcraft.transport.wire.SavedDataWireSystems;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public enum PipeExtensionManager implements IPipeExtensionManager {
   INSTANCE;

   private final Map<ResourceKey<Level>, List<PipeExtensionManager.PipeExtensionRequest>> requests = new HashMap<>();
   private final Set<PipeDefinition> retractionPipeDefs = new HashSet<>();

   @Override
   public boolean requestPipeExtension(Level world, BlockPos pos, Direction dir, IStripesActivator stripes, ItemStack stack) {
      if (world.isClientSide() || stack.isEmpty() || !(stack.getItem() instanceof IItemPipe itemPipe)) {
         return false;
      }

      List<PipeExtensionManager.PipeExtensionRequest> list = this.requests.computeIfAbsent(world.dimension(), key -> new ArrayList<>());
      return list.add(new PipeExtensionManager.PipeExtensionRequest(pos, dir, stripes, itemPipe.getDefinition(), stack.copy()));
   }

   @Override
   public void registerRetractionPipe(PipeDefinition pipeDefinition) {
      if (pipeDefinition != null) {
         this.retractionPipeDefs.add(pipeDefinition);
      }
   }

   public void tick(MinecraftServer server) {
      for (ServerLevel level : server.getAllLevels()) {
         List<PipeExtensionManager.PipeExtensionRequest> list = this.requests.get(level.dimension());
         if (list == null || list.isEmpty()) {
            continue;
         }

         for (PipeExtensionManager.PipeExtensionRequest request : list) {
            if (this.retractionPipeDefs.contains(request.pipeDef)) {
               this.retract(level, request);
            } else {
               this.extend(level, request);
            }
         }

         list.clear();
      }
   }

   private void retract(ServerLevel level, PipeExtensionManager.PipeExtensionRequest request) {
      Direction retractDir = request.dir.getOpposite();
      if (!this.isValidRetractionPath(level, request, retractDir)) {
         List<Direction> possible = new ArrayList<>();

         for (Direction facing : Direction.values()) {
            if (facing.getAxis() != request.dir.getAxis() && this.isValidRetractionPath(level, request, facing)) {
               possible.add(facing);
            }
         }

         if (possible.isEmpty()) {
            request.stripes.sendItem(request.stack.copy(), request.dir);
            return;
         }

         retractDir = possible.get(level.getRandom().nextInt(possible.size()));
      }

      BlockPos targetPos = request.pos.relative(retractDir);
      List<ItemStack> stacksToSendBack = new ArrayList<>();
      stacksToSendBack.add(request.stack.copy());
      TilePipeHolder stripesHolder = this.getHolder(level, request.pos);
      if (stripesHolder == null || stripesHolder.getPipe() == null) {
         BCLog.logger.warn("[transport.stripes] Invalid pipe extension request at {}", request.pos);
         return;
      }

      GameProfile owner = stripesHolder.getOwner();
      BlockState stripesState = level.getBlockState(request.pos);
      CompoundTag stripesNbt = stripesHolder.saveCustomOnly(level.registryAccess());
      PipeBehaviour behaviour = stripesHolder.getPipe().getBehaviour();
      if (behaviour instanceof PipeBehaviourStripes stripes) {
         stripes.direction = retractDir.getOpposite();
      }

      List<ItemStack> brokenDrops = BlockUtil.breakBlockAndGetDrops(level, targetPos, ItemStack.EMPTY, owner).orElse(List.of());
      if (brokenDrops.isEmpty() && !level.getBlockState(targetPos).isAir()) {
         request.stripes.sendItem(request.stack.copy(), request.dir);
         return;
      }

      BlockPos newStripesPos = targetPos;
      if (!this.restoreStripesPipe(level, newStripesPos, stripesState, stripesNbt, owner)) {
         request.stripes.sendItem(request.stack.copy(), request.dir);
         return;
      }

      if (!BlockUtil.breakBlockAndGetDrops(level, request.pos, ItemStack.EMPTY, owner).isPresent()) {
         this.restoreStripesPipe(level, request.pos, stripesState, stripesNbt, owner);
         request.stripes.sendItem(request.stack.copy(), request.dir);
         return;
      }

      stacksToSendBack.addAll(brokenDrops);
      this.cleanup(level, request, newStripesPos, stacksToSendBack, false, stripesNbt);
   }

   private void extend(ServerLevel level, PipeExtensionManager.PipeExtensionRequest request) {
      BlockPos targetPos = request.pos.relative(request.dir);
      BlockState targetState = level.getBlockState(targetPos);
      if (!level.isEmptyBlock(targetPos) && !targetState.canBeReplaced()) {
         request.stripes.sendItem(request.stack.copy(), request.dir);
         return;
      }

      TilePipeHolder stripesHolder = this.getHolder(level, request.pos);
      if (stripesHolder == null || stripesHolder.getPipe() == null) {
         BCLog.logger.warn("[transport.stripes] Invalid pipe extension request at {}", request.pos);
         return;
      }

      GameProfile owner = stripesHolder.getOwner();
      BlockState stripesState = level.getBlockState(request.pos);
      CompoundTag stripesNbt = stripesHolder.saveCustomOnly(level.registryAccess());
      if (!BlockUtil.breakBlockAndGetDrops(level, request.pos, ItemStack.EMPTY, owner).isPresent()) {
         request.stripes.sendItem(request.stack.copy(), request.dir);
         return;
      }

      List<ItemStack> placedLeftovers = new ArrayList<>();
      if (!this.placeExtensionPipe(level, request, owner, placedLeftovers)) {
         this.restoreStripesPipe(level, request.pos, stripesState, stripesNbt, owner);
         request.stripes.sendItem(request.stack.copy(), request.dir);
         return;
      }

      BlockPos newStripesPos = targetPos;
      List<ItemStack> stacksToSendBack = new ArrayList<>(placedLeftovers);
      if (!this.restoreStripesPipe(level, newStripesPos, stripesState, stripesNbt, owner)) {
         this.restoreStripesPipe(level, request.pos, stripesState, stripesNbt, owner);
         stacksToSendBack.add(request.stack.copy());
         this.cleanup(level, request, request.pos, stacksToSendBack, true, stripesNbt);
         return;
      }

      this.cleanup(level, request, newStripesPos, stacksToSendBack, false, stripesNbt);
   }

   private boolean placeExtensionPipe(ServerLevel level, PipeExtensionManager.PipeExtensionRequest request, GameProfile owner, List<ItemStack> leftovers) {
      ServerPlayer player = BuildCraftAPI.fakePlayerProvider.getFakePlayer(level, owner, request.pos);
      player.getInventory().clearContent();
      ItemStack placeStack = request.stack.copy();
      player.getInventory().setItem(player.getInventory().getSelectedSlot(), placeStack);
      BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(request.pos), request.dir.getOpposite(), request.pos, false);
      UseOnContext ctx = new UseOnContext(level, player, InteractionHand.MAIN_HAND, placeStack, hit);
      InteractionResult result = placeStack.useOn(ctx);

      for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
         ItemStack stack = player.getInventory().removeItemNoUpdate(i);
         if (!stack.isEmpty()) {
            leftovers.add(stack);
         }
      }

      return result.consumesAction();
   }

   private boolean restoreStripesPipe(ServerLevel level, BlockPos pos, BlockState state, CompoundTag stripesNbt, GameProfile owner) {
      if (!BlockUtil.machineSetBlock(level, pos, BCTransportBlocks.PIPE_HOLDER.defaultBlockState(), 3, owner, pos)) {
         return false;
      }

      if (!(level.getBlockEntity(pos) instanceof TilePipeHolder holder)) {
         return false;
      }

      holder.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), stripesNbt));
      holder.setChanged();
      level.sendBlockUpdated(pos, holder.getBlockState(), holder.getBlockState(), 3);
      return holder.getPipe() != null;
   }

   private void cleanup(
      ServerLevel level,
      PipeExtensionManager.PipeExtensionRequest request,
      BlockPos stripesPos,
      List<ItemStack> stacksToSendBack,
      boolean canceled,
      CompoundTag stripesNbt
   ) {
      if (!(level.getBlockEntity(stripesPos) instanceof TilePipeHolder holder)) {
         for (ItemStack stack : stacksToSendBack) {
            InventoryUtil.drop(level, stripesPos, stack);
         }

         return;
      }

      if (!canceled) {
         holder.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), stripesNbt));
         holder.setChanged();
      SavedDataWireSystems.get(level).rebuildWireSystemsAround(holder);
      }

      PipeBehaviour behaviour = holder.getPipe() != null ? holder.getPipe().getBehaviour() : null;
      if (behaviour instanceof IStripesActivator stripes) {
         for (ItemStack stack : stacksToSendBack) {
            ItemStack copy = stack.copy();
            if (!stripes.sendItem(copy, request.dir)) {
               stripes.dropItem(copy, request.dir);
            }
         }
      } else {
         for (ItemStack stack : stacksToSendBack) {
            InventoryUtil.drop(level, stripesPos, stack);
         }
      }
   }

   private boolean isValidRetractionPath(Level level, PipeExtensionManager.PipeExtensionRequest request, Direction retractDir) {
      if (!(level.getBlockEntity(request.pos.relative(retractDir)) instanceof TilePipeHolder tile) || tile.getPipe() == null) {
         return false;
      }

      IPipe pipe = tile.getPipe();
      boolean connected = false;

      for (Direction facing : Direction.values()) {
         if (pipe.getConnectedType(facing) == IPipe.ConnectedType.TILE) {
            return false;
         }

         if (facing == retractDir.getOpposite() && pipe.getConnectedType(facing) != IPipe.ConnectedType.PIPE) {
            return false;
         }

         if (facing != retractDir.getOpposite() && connected && pipe.getConnectedType(facing) != null) {
            return false;
         }

         if (facing != retractDir.getOpposite() && !connected && pipe.getConnectedType(facing) != null) {
            connected = true;
         }
      }

      return true;
   }

   private TilePipeHolder getHolder(Level level, BlockPos pos) {
      return level.getBlockEntity(pos) instanceof TilePipeHolder holder ? holder : null;
   }

   private record PipeExtensionRequest(BlockPos pos, Direction dir, IStripesActivator stripes, PipeDefinition pipeDef, ItemStack stack) {
   }
}
