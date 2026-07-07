/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.block;

import buildcraft.lib.compat.BcInteract;

import buildcraft.api.blocks.ICustomPaintHandler;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.WireNode;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.net.BcPacketDistributor;
import buildcraft.transport.BCTransportAttachments;
import buildcraft.transport.BCTransportBlockEntities;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.client.PipeHolderClientExtensions;
import buildcraft.transport.item.ItemWire;
import buildcraft.transport.net.MessagePipeLandingEffect;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.tile.TilePipeHolder;
import buildcraft.transport.wire.EnumWireBetween;
import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
//? if >= 1.21.10 {
import net.minecraft.world.level.redstone.Orientation;
//?}
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockPipeHolder extends Block implements EntityBlock, ICustomPaintHandler {
   private static final Identifier ADVANCEMENT_LOGIC_TRANSPORTATION = Identifier.parse("buildcrafttransport:logic_transportation");
   private static final Identifier ADVANCEMENT_COLORFUL_ELECTRICIAN = Identifier.parse("buildcrafttransport:colorful_electrician");
   private static final VoxelShape CENTER = Block.box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0);
   private static final double E = 0.01;
   private static final VoxelShape ARM_DOWN = Block.box(4.0, 0.0, 4.0, 12.0, 3.99, 12.0);
   private static final VoxelShape ARM_UP = Block.box(4.0, 12.01, 4.0, 12.0, 16.0, 12.0);
   private static final VoxelShape ARM_NORTH = Block.box(4.0, 4.0, 0.0, 12.0, 12.0, 3.99);
   private static final VoxelShape ARM_SOUTH = Block.box(4.0, 4.0, 12.01, 12.0, 12.0, 16.0);
   private static final VoxelShape ARM_WEST = Block.box(0.0, 4.0, 4.0, 3.99, 12.0, 12.0);
   private static final VoxelShape ARM_EAST = Block.box(12.01, 4.0, 4.0, 16.0, 12.0, 12.0);
   private static final VoxelShape[] ARMS = new VoxelShape[]{ARM_DOWN, ARM_UP, ARM_NORTH, ARM_SOUTH, ARM_WEST, ARM_EAST};
   public static final double WIRE_HIT_INFLATE = 0.0625;

   public BlockPipeHolder(Properties props) {
      super(props);
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TilePipeHolder(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return type == BCTransportBlockEntities.PIPE_HOLDER ? (lvl, pos, st, be) -> ((TilePipeHolder)be).tick() : null;
   }

   private VoxelShape getFullShape(BlockGetter level, BlockPos pos) {
      return level.getBlockEntity(pos) instanceof TilePipeHolder tile && tile.getPipe() != null ? tile.getFullShape() : CENTER;
   }

   /**
    * Composes the full outline/collision shape. Only called by {@link TilePipeHolder#getFullShape()} on cache
    * miss: dynamicShape() disables vanilla's per-state shape cache, and shape queries run hot (every entity
    * collision each tick, neighbour sturdiness checks, interaction rays), so recomposing these Shapes.or
    * merges per query made dense pipe chunks pay for it thousands of times a tick.
    */
   public static VoxelShape buildFullShape(TilePipeHolder tile) {
      Pipe pipe = tile.getPipe();
      if (pipe == null) {
         return CENTER;
      }

      VoxelShape shape = CENTER;

      for (Direction dir : Direction.values()) {
         if (pipe.isConnected(dir)) {
            shape = Shapes.or(shape, ARMS[dir.ordinal()]);
         }

         PipePluggable plug = tile.getPluggable(dir);
         if (plug != null) {
            AABB box = plug.getBoundingBox();
            shape = Shapes.or(shape, Shapes.create(box));
         }
      }

      for (EnumWirePart part : tile.getWireManager().parts.keySet()) {
         shape = Shapes.or(shape, Shapes.create(part.boundingBox));
      }

      for (EnumWireBetween between : tile.getWireManager().betweens.keySet()) {
         shape = Shapes.or(shape, Shapes.create(between.boundingBox));
      }

      return shape;
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      if (level.getBlockEntity(pos) instanceof TilePipeHolder tile
         && tile.getPipe() != null
         && level instanceof Level realLevel
         && realLevel.isClientSide()
         && clientBlockHit() instanceof BlockHitResult blockHit
         && pos.equals(blockHit.getBlockPos())) {
         Pipe pipe = tile.getPipe();
         double lx = blockHit.getLocation().x - pos.getX();
         double ly = blockHit.getLocation().y - pos.getY();
         double lz = blockHit.getLocation().z - pos.getZ();

         for (Direction dir : Direction.values()) {
            PipePluggable plug = tile.getPluggable(dir);
            if (plug != null) {
               AABB box = plug.getBoundingBox();
               if (lx >= box.minX && lx <= box.maxX && ly >= box.minY && ly <= box.maxY && lz >= box.minZ && lz <= box.maxZ) {
                  return Shapes.create(box);
               }
            }
         }

         EnumWirePart hitWire = getHitWire(tile, lx, ly, lz);
         if (hitWire != null) {
            return Shapes.create(hitWire.boundingBox.inflate(0.0625));
         } else {
            EnumWireBetween hitBetween = getHitWireBetween(tile, lx, ly, lz);
            if (hitBetween != null) {
               return Shapes.create(hitBetween.boundingBox.inflate(0.0625));
            } else if (ly < 0.25 && pipe.isConnected(Direction.DOWN)) {
               return ARMS[Direction.DOWN.ordinal()];
            } else if (ly > 0.75 && pipe.isConnected(Direction.UP)) {
               return ARMS[Direction.UP.ordinal()];
            } else if (lz < 0.25 && pipe.isConnected(Direction.NORTH)) {
               return ARMS[Direction.NORTH.ordinal()];
            } else if (lz > 0.75 && pipe.isConnected(Direction.SOUTH)) {
               return ARMS[Direction.SOUTH.ordinal()];
            } else if (lx < 0.25 && pipe.isConnected(Direction.WEST)) {
               return ARMS[Direction.WEST.ordinal()];
            } else {
               return lx > 0.75 && pipe.isConnected(Direction.EAST) ? ARMS[Direction.EAST.ordinal()] : CENTER;
            }
         }
      } else {
         return this.getFullShape(level, pos);
      }
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return this.getFullShape(level, pos);
   }

   public boolean isPathfindable(BlockState state, PathComputationType type) {
      return false;
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   public boolean propagatesSkylightDown(BlockState state) {
      return true;
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (level.getBlockEntity(pos) instanceof TilePipeHolder tile) {
         tile.onPlacedBy(placer, stack);
      }
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      if (level.getBlockEntity(pos) instanceof TilePipeHolder tile && tile.getPipe() != null) {
         Pipe pipe = tile.getPipe();
         EnumPipePart hitPart = getHitPart(tile, hitResult);
         Direction plugDir = getHitPluggable(
            tile, hitResult.getLocation().x - pos.getX(), hitResult.getLocation().y - pos.getY(), hitResult.getLocation().z - pos.getZ()
         );
         if (plugDir != null) {
            PipePluggable existing = tile.getPluggable(plugDir);
            if (existing != null
               && existing.onPluggableActivate(
                  player, hitResult, (float)hitResult.getLocation().x, (float)hitResult.getLocation().y, (float)hitResult.getLocation().z
               )) {
               return InteractionResult.SUCCESS;
            }
         }

         if (pipe.getBehaviour()
            .onPipeActivate(player, hitResult, (float)hitResult.getLocation().x, (float)hitResult.getLocation().y, (float)hitResult.getLocation().z, hitPart)) {
            return InteractionResult.SUCCESS;
         }

         if (pipe.getFlow()
            .onFlowActivate(player, hitResult, (float)hitResult.getLocation().x, (float)hitResult.getLocation().y, (float)hitResult.getLocation().z, hitPart)) {
            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.PASS;
   }

   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      return BcInteract.toItem(bcUseItemOn(stack, state, level, pos, player, hand, hitResult));
   }

   protected InteractionResult bcUseItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      if (stack.isEmpty()) {
         return BcInteract.TRY_WITH_EMPTY_HAND;
      }

      if (level.getBlockEntity(pos) instanceof TilePipeHolder tile && tile.getPipe() != null) {
         Direction realSide = resolveTargetFace(tile, hitResult);
         if (stack.getItem() instanceof IItemPluggable itemPlug) {
            PipePluggable existing = tile.getPluggable(realSide);
            if (existing == null) {
               PipePluggable plug = itemPlug.onPlace(stack, tile, realSide, player, hand);
               if (plug != null) {
                  if (!level.isClientSide()) {
                     tile.replacePluggable(realSide, plug);
                     plug.onPlacedBy(player);
                     if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                     }
                  }

                  return InteractionResult.SUCCESS;
               }
            }
         }

         if (stack.getItem() instanceof ItemWire itemWire) {
            EnumWirePart wirePart = resolveTargetWirePart(hitResult);
            DyeColor wireColour = itemWire.getColor();
            if (tile.getWireManager().addPart(wirePart, wireColour)) {
               if (!level.isClientSide()) {
                  if (!player.getAbilities().instabuild) {
                     stack.shrink(1);
                  }

                  if (isWireConnected(level, pos, tile, wirePart, wireColour)) {
                     AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_LOGIC_TRANSPORTATION);
                  }

                  BCTransportAttachments.WireColoursPlaced placed = BCTransportAttachments.wireColours(player);
                  if (placed.markPlaced(wireColour)) {
                     AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_COLORFUL_ELECTRICIAN, wireColour.getName());
                  }

                  BCTransportAttachments.recordPluggablePlacement(player, BCTransportAttachments.PluggablesPlaced.Kind.WIRE);
                  level.sendBlockUpdated(pos, state, state, 3);
               }

               return InteractionResult.SUCCESS;
            }
         }

         Pipe pipe = tile.getPipe();
         EnumPipePart hitPart = getHitPart(tile, hitResult);
         Direction plugDir = getHitPluggable(
            tile, hitResult.getLocation().x - pos.getX(), hitResult.getLocation().y - pos.getY(), hitResult.getLocation().z - pos.getZ()
         );
         if (plugDir != null) {
            PipePluggable existing = tile.getPluggable(plugDir);
            if (existing != null
               && existing.onPluggableActivate(
                  player, hitResult, (float)hitResult.getLocation().x, (float)hitResult.getLocation().y, (float)hitResult.getLocation().z
               )) {
               return InteractionResult.SUCCESS;
            }
         }

         if (pipe.getBehaviour()
            .onPipeActivate(player, hitResult, (float)hitResult.getLocation().x, (float)hitResult.getLocation().y, (float)hitResult.getLocation().z, hitPart)) {
            return InteractionResult.SUCCESS;
         } else {
            return (InteractionResult)(pipe.getFlow()
                  .onFlowActivate(
                     player, hitResult, (float)hitResult.getLocation().x, (float)hitResult.getLocation().y, (float)hitResult.getLocation().z, hitPart
                  )
               ? InteractionResult.SUCCESS
               : BcInteract.TRY_WITH_EMPTY_HAND);
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   public static Direction resolveTargetFace(TilePipeHolder tile, BlockHitResult hitResult) {
      Direction armFace = getHitFace(tile, hitResult);
      return armFace != null ? armFace : hitResult.getDirection();
   }

   static boolean isWireConnected(Level level, BlockPos pos, TilePipeHolder tile, EnumWirePart wirePart, DyeColor colour) {
      WireNode from = new WireNode(pos, wirePart);

      for (Direction dir : Direction.values()) {
         WireNode to = from.offset(dir);
         if (to.pos == from.pos) {
            if (tile.getWireManager().getColorOfPart(to.part) == colour) {
               return true;
            }
         } else if (level.getBlockEntity(to.pos) instanceof TilePipeHolder other && other.getWireManager().getColorOfPart(to.part) == colour) {
            return true;
         }
      }

      return false;
   }

   public static EnumWirePart resolveTargetWirePart(BlockHitResult hitResult) {
      BlockPos pos = hitResult.getBlockPos();
      double lx = hitResult.getLocation().x - pos.getX();
      double ly = hitResult.getLocation().y - pos.getY();
      double lz = hitResult.getLocation().z - pos.getZ();
      return EnumWirePart.get(lx > 0.5, ly > 0.5, lz > 0.5);
   }

   @Nullable
   private static Direction getHitFace(TilePipeHolder tile, BlockHitResult hitResult) {
      double lx = hitResult.getLocation().x - hitResult.getBlockPos().getX();
      double ly = hitResult.getLocation().y - hitResult.getBlockPos().getY();
      double lz = hitResult.getLocation().z - hitResult.getBlockPos().getZ();
      Pipe pipe = tile.getPipe();
      if (pipe != null) {
         if (ly < 0.25 && pipe.isConnected(Direction.DOWN)) {
            return Direction.DOWN;
         }

         if (ly > 0.75 && pipe.isConnected(Direction.UP)) {
            return Direction.UP;
         }

         if (lz < 0.25 && pipe.isConnected(Direction.NORTH)) {
            return Direction.NORTH;
         }

         if (lz > 0.75 && pipe.isConnected(Direction.SOUTH)) {
            return Direction.SOUTH;
         }

         if (lx < 0.25 && pipe.isConnected(Direction.WEST)) {
            return Direction.WEST;
         }

         if (lx > 0.75 && pipe.isConnected(Direction.EAST)) {
            return Direction.EAST;
         }
      }

      return null;
   }

   private static EnumPipePart getHitPart(TilePipeHolder tile, BlockHitResult hitResult) {
      double lx = hitResult.getLocation().x - hitResult.getBlockPos().getX();
      double ly = hitResult.getLocation().y - hitResult.getBlockPos().getY();
      double lz = hitResult.getLocation().z - hitResult.getBlockPos().getZ();
      Pipe pipe = tile.getPipe();
      if (pipe != null) {
         if (ly < 0.25 && pipe.isConnected(Direction.DOWN)) {
            return EnumPipePart.fromFacing(Direction.DOWN);
         }

         if (ly > 0.75 && pipe.isConnected(Direction.UP)) {
            return EnumPipePart.fromFacing(Direction.UP);
         }

         if (lz < 0.25 && pipe.isConnected(Direction.NORTH)) {
            return EnumPipePart.fromFacing(Direction.NORTH);
         }

         if (lz > 0.75 && pipe.isConnected(Direction.SOUTH)) {
            return EnumPipePart.fromFacing(Direction.SOUTH);
         }

         if (lx < 0.25 && pipe.isConnected(Direction.WEST)) {
            return EnumPipePart.fromFacing(Direction.WEST);
         }

         if (lx > 0.75 && pipe.isConnected(Direction.EAST)) {
            return EnumPipePart.fromFacing(Direction.EAST);
         }
      }

      return EnumPipePart.CENTER;
   }

   @Nullable
   public static Direction getHitPluggable(TilePipeHolder tile, double lx, double ly, double lz) {
      for (Direction dir : Direction.values()) {
         PipePluggable plug = tile.getPluggable(dir);
         if (plug != null) {
            AABB box = plug.getBoundingBox();
            if (lx >= box.minX && lx <= box.maxX && ly >= box.minY && ly <= box.maxY && lz >= box.minZ && lz <= box.maxZ) {
               return dir;
            }
         }
      }

      return null;
   }

   @Nullable
   public static EnumWirePart getHitWire(TilePipeHolder tile, double lx, double ly, double lz) {
      for (EnumWirePart part : tile.getWireManager().parts.keySet()) {
         AABB box = part.boundingBox.inflate(0.0625);
         if (lx >= box.minX && lx <= box.maxX && ly >= box.minY && ly <= box.maxY && lz >= box.minZ && lz <= box.maxZ) {
            return part;
         }
      }

      return null;
   }

   @Nullable
   public static EnumWireBetween getHitWireBetween(TilePipeHolder tile, double lx, double ly, double lz) {
      for (EnumWireBetween between : tile.getWireManager().betweens.keySet()) {
         AABB box = between.boundingBox.inflate(0.0625);
         if (lx >= box.minX && lx <= box.maxX && ly >= box.minY && ly <= box.maxY && lz >= box.minZ && lz <= box.maxZ) {
            return between;
         }
      }

      return null;
   }

   public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack toolStack, boolean willHarvest, FluidState fluid) {
      if (level.getBlockEntity(pos) instanceof TilePipeHolder tile
         && player.pick(5.0, 0.0F, false) instanceof BlockHitResult blockHit
         && pos.equals(blockHit.getBlockPos())) {
         double lx = blockHit.getLocation().x - pos.getX();
         double ly = blockHit.getLocation().y - pos.getY();
         double lz = blockHit.getLocation().z - pos.getZ();
         Direction plugDir = getHitPluggable(tile, lx, ly, lz);
         if (plugDir != null) {
            PipePluggable plug = tile.getPluggable(plugDir);
            if (plug != null) {
               if (!level.isClientSide()) {
                  ItemStack drop = plug.getPickStack();
                  if (!player.isCreative() && !drop.isEmpty()) {
                     Block.popResource(level, pos, drop);
                  }
               }

               tile.replacePluggable(plugDir, null);
               if (!level.isClientSide()) {
                  level.sendBlockUpdated(pos, state, state, 3);
               }
            }

            return false;
         }

         EnumWirePart hitWire = getHitWire(tile, lx, ly, lz);
         if (hitWire != null) {
            if (!level.isClientSide()) {
               DyeColor col = tile.getWireManager().getColorOfPart(hitWire);
               if (col != null) {
                  ItemStack drop = new ItemStack((ItemLike)BCTransportItems.WIRE_ITEMS.get(col));
                  if (!player.isCreative() && !drop.isEmpty()) {
                     Block.popResource(level, pos, drop);
                  }
               }
            }

            tile.getWireManager().removePart(hitWire);
            if (!level.isClientSide()) {
               level.sendBlockUpdated(pos, state, state, 3);
            }

            return false;
         }

         EnumWireBetween hitBetween = getHitWireBetween(tile, lx, ly, lz);
         if (hitBetween != null) {
            if (!level.isClientSide()) {
               DyeColor col = tile.getWireManager().getColorOfPart(hitBetween.parts[0]);
               if (col != null) {
                  int dropCount = hitBetween.to == null ? 2 : 1;
                  ItemStack drop = new ItemStack((ItemLike)BCTransportItems.WIRE_ITEMS.get(col), dropCount);
                  if (!player.isCreative() && !drop.isEmpty()) {
                     Block.popResource(level, pos, drop);
                  }
               }
            }

            if (hitBetween.to == null) {
               tile.getWireManager().removeParts(Arrays.asList(hitBetween.parts));
            } else {
               tile.getWireManager().removePart(hitBetween.parts[0]);
            }

            if (!level.isClientSide()) {
               level.sendBlockUpdated(pos, state, state, 3);
            }

            return false;
         }
      }

      return true;
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (level.getBlockEntity(pos) instanceof TilePipeHolder tile) {
         HitResult hit = player.pick(5.0, 0.0F, false);
         boolean hittingPluggable = false;
         boolean hittingWire = false;
         if (hit instanceof BlockHitResult blockHit && pos.equals(blockHit.getBlockPos())) {
            double lx = blockHit.getLocation().x - pos.getX();
            double ly = blockHit.getLocation().y - pos.getY();
            double lz = blockHit.getLocation().z - pos.getZ();
            hittingPluggable = getHitPluggable(tile, lx, ly, lz) != null;
            hittingWire = getHitWire(tile, lx, ly, lz) != null || getHitWireBetween(tile, lx, ly, lz) != null;
         }

         if (hittingPluggable || hittingWire) {
            return super.playerWillDestroy(level, pos, state, player);
         }

         if (!level.isClientSide()) {
            // Always run cleanup (pluggable onRemove -> station/robot teardown); pop the item drops only in survival.
            tile.dropPipeItems(level, pos, !player.isCreative());
            tile.wireManager.invalidate();
         }
      }

      return super.playerWillDestroy(level, pos, state, player);
   }

   //? if >= 1.21.10 {
   protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
      super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
   //?} else {
   /*protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean movedByPiston) {
      super.neighborChanged(state, level, pos, neighborBlock, fromPos, movedByPiston);
   *///?}
      if (level.getBlockEntity(pos) instanceof TilePipeHolder tile && tile.getPipe() != null) {
         tile.getPipe().scheduleConnectionRecheck();
         tile.wakePipe();
      }
   }

   public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
      if (level.getBlockEntity(pos) instanceof TilePipeHolder tile) {
         if (direction != null) {
            Direction face = direction.getOpposite();
            PipePluggable plug = tile.getPluggable(face);
            if (plug != null && plug.canConnectToRedstone(face)) {
               return true;
            }
         } else {
            for (Direction dir : Direction.values()) {
               PipePluggable plug = tile.getPluggable(dir);
               if (plug != null && plug.canConnectToRedstone(null)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public boolean isSignalSource(BlockState state) {
      return true;
   }

   public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
      return level.getBlockEntity(pos) instanceof TilePipeHolder tile ? tile.getRedstoneOutput(direction.getOpposite()) : 0;
   }

   public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
      return this.getSignal(state, level, pos, direction);
   }

   //? if >= 1.21.10 {
   protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
   //?} else {
   /*public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
   *///?}
      if (level.getBlockEntity(pos) instanceof TilePipeHolder tile) {
         if (!(level instanceof Level realLevel) || !realLevel.isClientSide()) {
            return getDefaultPipePickStack(tile);
         }

         Player player = clientPlayer();
         if (player == null) {
            return getDefaultPipePickStack(tile);
         }

         if (player.pick(5.0, 0.0F, false) instanceof BlockHitResult blockHit && pos.equals(blockHit.getBlockPos())) {
            double lx = blockHit.getLocation().x - pos.getX();
            double ly = blockHit.getLocation().y - pos.getY();
            double lz = blockHit.getLocation().z - pos.getZ();
            Direction plugDir = getHitPluggable(tile, lx, ly, lz);
            if (plugDir != null) {
               PipePluggable plug = tile.getPluggable(plugDir);
               if (plug != null) {
                  return plug.getPickStack();
               }
            }

            EnumWirePart wirePart = getHitWire(tile, lx, ly, lz);
            if (wirePart != null) {
               DyeColor col = tile.getWireManager().getColorOfPart(wirePart);
               if (col != null) {
                  return new ItemStack((ItemLike)BCTransportItems.WIRE_ITEMS.get(col));
               }
            }

            EnumWireBetween wireBetween = getHitWireBetween(tile, lx, ly, lz);
            if (wireBetween != null) {
               DyeColor col = tile.getWireManager().getColorOfPart(wireBetween.parts[0]);
               if (col != null) {
                  return new ItemStack((ItemLike)BCTransportItems.WIRE_ITEMS.get(col));
               }
            }
         }

         ItemStack pipeStack = getDefaultPipePickStack(tile);
         if (!pipeStack.isEmpty()) {
            return pipeStack;
         }
      }

      //? if >= 1.21.10 {
      return super.getCloneItemStack(level, pos, state, includeData);
      //?} else {
      /*return super.getCloneItemStack(level, pos, state);
      *///?}
   }

   // Client-only crosshair queries are delegated to PipeHolderClientExtensions so this common block class
   // never names net.minecraft.client.* in its bytecode (the verifier would otherwise resolve LocalPlayer and
   // crash a dedicated server). Both are only reached behind a Level.isClientSide() guard, so the client
   // extensions class is never loaded server-side. Return types are common (BlockHitResult / Player).
   @Nullable
   private static BlockHitResult clientBlockHit() {
      return PipeHolderClientExtensions.clientBlockHit();
   }

   @Nullable
   private static Player clientPlayer() {
      return PipeHolderClientExtensions.clientPlayer();
   }

   private static ItemStack getDefaultPipePickStack(TilePipeHolder tile) {
      if (tile.getPipe() == null) {
         return ItemStack.EMPTY;
      }

      Pipe pipe = tile.getPipe();
      PipeDefinition def = pipe.getDefinition();
      Item item = (Item)PipeApi.pipeRegistry.getItemForPipe(def);
      if (item == null) {
         return ItemStack.EMPTY;
      }

      ItemStack stack = new ItemStack(item);
      DyeColor col = pipe.getColour();
      if (col != null) {
         stack.set(BCTransportItems.PIPE_COLOUR, col);
      }

      return stack;
   }

   public boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
      return level.isClientSide()
         ? PipeHolderClientExtensions.spawnRunningParticle(
            level,
            pos,
            entity.getX(),
            entity.getZ(),
            entity.getBbWidth(),
            entity.getDeltaMovement().x,
            entity.getDeltaMovement().z,
            entity.getBoundingBox().minY
         )
         : false;
   }

   public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
      BcPacketDistributor.sendToPlayersTrackingChunk(
         level,
         new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4),
         new MessagePipeLandingEffect(pos, entity.getX(), entity.getY(), entity.getZ(), numberOfParticles)
      );
      return true;
   }

   @Override
   public InteractionResult attemptPaint(Level world, BlockPos pos, BlockState state, Vec3 hitPos, @Nullable Direction hitSide, @Nullable DyeColor paintColour) {
      if (world.getBlockEntity(pos) instanceof TilePipeHolder tile) {
         Pipe pipe = tile.getPipe();
         if (pipe == null) {
            return InteractionResult.FAIL;
         } else if (pipe.getColour() != paintColour && pipe.getDefinition().canBeColoured) {
            pipe.setColour(paintColour);
            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.FAIL;
         }
      } else {
         return InteractionResult.PASS;
      }
   }
}
