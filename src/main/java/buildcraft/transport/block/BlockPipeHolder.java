/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.block;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.api.blocks.ICustomPaintHandler;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.transport.BCTransportBlockEntities;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.tile.TilePipeHolder;

public class BlockPipeHolder extends Block implements EntityBlock, ICustomPaintHandler {

    private static final net.minecraft.resources.Identifier ADVANCEMENT_LOGIC_TRANSPORTATION
        = net.minecraft.resources.Identifier.parse("buildcrafttransport:logic_transportation");
    private static final net.minecraft.resources.Identifier ADVANCEMENT_COLORFUL_ELECTRICIAN
        = net.minecraft.resources.Identifier.parse("buildcrafttransport:colorful_electrician");

    private static final VoxelShape CENTER = Block.box(4, 4, 4, 12, 12, 12);

    private static final double E = 0.01;
    private static final VoxelShape ARM_DOWN  = Block.box(4, 0, 4, 12, 4 - E, 12);
    private static final VoxelShape ARM_UP    = Block.box(4, 12 + E, 4, 12, 16, 12);
    private static final VoxelShape ARM_NORTH = Block.box(4, 4, 0, 12, 12, 4 - E);
    private static final VoxelShape ARM_SOUTH = Block.box(4, 4, 12 + E, 12, 12, 16);
    private static final VoxelShape ARM_WEST  = Block.box(0, 4, 4, 4 - E, 12, 12);
    private static final VoxelShape ARM_EAST  = Block.box(12 + E, 4, 4, 16, 12, 12);
    private static final VoxelShape[] ARMS = { ARM_DOWN, ARM_UP, ARM_NORTH, ARM_SOUTH, ARM_WEST, ARM_EAST };

    public static final double WIRE_HIT_INFLATE = 1.0 / 16.0;

    public BlockPipeHolder(Properties props) {
        super(props);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TilePipeHolder(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == BCTransportBlockEntities.PIPE_HOLDER) {
            return (lvl, pos, st, be) -> ((TilePipeHolder) be).tick();
        }
        return null;
    }

    private VoxelShape getFullShape(BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TilePipeHolder tile && tile.getPipe() != null) {
            VoxelShape shape = CENTER;
            var pipe = tile.getPipe();
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

            for (buildcraft.api.transport.EnumWirePart part : tile.getWireManager().parts.keySet()) {
                shape = Shapes.or(shape, Shapes.create(part.boundingBox));
            }
            for (buildcraft.transport.wire.EnumWireBetween between : tile.getWireManager().betweens.keySet()) {
                shape = Shapes.or(shape, Shapes.create(between.boundingBox));
            }
            return shape;
        }
        return CENTER;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TilePipeHolder tile && tile.getPipe() != null) {

            if (level instanceof net.minecraft.world.level.Level realLevel && realLevel.isClientSide()) {
                net.minecraft.world.phys.HitResult hit = net.minecraft.client.Minecraft.getInstance().hitResult;
                if (hit instanceof BlockHitResult blockHit && pos.equals(blockHit.getBlockPos())) {
                    var pipe = tile.getPipe();
                    double lx = blockHit.getLocation().x - pos.getX();
                    double ly = blockHit.getLocation().y - pos.getY();
                    double lz = blockHit.getLocation().z - pos.getZ();

                    for (Direction dir : Direction.values()) {
                        PipePluggable plug = tile.getPluggable(dir);
                        if (plug != null) {
                            AABB box = plug.getBoundingBox();
                            if (lx >= box.minX && lx <= box.maxX
                                && ly >= box.minY && ly <= box.maxY
                                && lz >= box.minZ && lz <= box.maxZ) {
                                return Shapes.create(box);
                            }
                        }
                    }

                    buildcraft.api.transport.EnumWirePart hitWire = getHitWire(tile, lx, ly, lz);
                    if (hitWire != null) {
                        return Shapes.create(hitWire.boundingBox.inflate(WIRE_HIT_INFLATE));
                    }

                    buildcraft.transport.wire.EnumWireBetween hitBetween = getHitWireBetween(tile, lx, ly, lz);
                    if (hitBetween != null) {
                        return Shapes.create(hitBetween.boundingBox.inflate(WIRE_HIT_INFLATE));
                    }

                    if (ly < 0.25 && pipe.isConnected(Direction.DOWN))  return ARMS[Direction.DOWN.ordinal()];
                    if (ly > 0.75 && pipe.isConnected(Direction.UP))    return ARMS[Direction.UP.ordinal()];
                    if (lz < 0.25 && pipe.isConnected(Direction.NORTH)) return ARMS[Direction.NORTH.ordinal()];
                    if (lz > 0.75 && pipe.isConnected(Direction.SOUTH)) return ARMS[Direction.SOUTH.ordinal()];
                    if (lx < 0.25 && pipe.isConnected(Direction.WEST))  return ARMS[Direction.WEST.ordinal()];
                    if (lx > 0.75 && pipe.isConnected(Direction.EAST))  return ARMS[Direction.EAST.ordinal()];

                    return CENTER;
                }
            }
        }

        return getFullShape(level, pos);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return getFullShape(level, pos);
    }

    @Override
    public boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TilePipeHolder tile) {
            tile.onPlacedBy(placer, stack);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TilePipeHolder tile && tile.getPipe() != null) {
            var pipe = tile.getPipe();
            buildcraft.api.core.EnumPipePart hitPart = getHitPart(tile, hitResult);

            Direction plugDir = getHitPluggable(tile,
                    hitResult.getLocation().x - pos.getX(),
                    hitResult.getLocation().y - pos.getY(),
                    hitResult.getLocation().z - pos.getZ());
            if (plugDir != null) {
                buildcraft.api.transport.pluggable.PipePluggable existing = tile.getPluggable(plugDir);
                if (existing != null) {
                    if (existing.onPluggableActivate(player, hitResult,
                            (float) hitResult.getLocation().x, (float) hitResult.getLocation().y,
                            (float) hitResult.getLocation().z)) {
                        return InteractionResult.SUCCESS;
                    }
                }
            }

            if (pipe.getBehaviour().onPipeActivate(player, hitResult,
                    (float) hitResult.getLocation().x, (float) hitResult.getLocation().y,
                    (float) hitResult.getLocation().z,
                    hitPart)) {
                return InteractionResult.SUCCESS;
            }
            if (pipe.getFlow().onFlowActivate(player, hitResult,
                    (float) hitResult.getLocation().x, (float) hitResult.getLocation().y,
                    (float) hitResult.getLocation().z,
                    hitPart)) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                           Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TilePipeHolder tile) || tile.getPipe() == null) {
            return InteractionResult.PASS;
        }

        Direction realSide = resolveTargetFace(tile, hitResult);

        if (stack.getItem() instanceof buildcraft.api.transport.IItemPluggable itemPlug) {
            buildcraft.api.transport.pluggable.PipePluggable existing = tile.getPluggable(realSide);
            if (existing == null) {
                buildcraft.api.transport.pluggable.PipePluggable plug =
                        itemPlug.onPlace(stack, tile, realSide, player, hand);
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

        if (stack.getItem() instanceof buildcraft.transport.item.ItemWire itemWire) {
            buildcraft.api.transport.EnumWirePart wirePart = resolveTargetWirePart(hitResult);
            DyeColor wireColour = itemWire.getColor();
            if (tile.getWireManager().addPart(wirePart, wireColour)) {
                if (!level.isClientSide()) {
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    if (isWireConnected(level, pos, tile, wirePart, wireColour)) {
                        buildcraft.lib.misc.AdvancementUtil.unlockAdvancement(
                            player, ADVANCEMENT_LOGIC_TRANSPORTATION);
                    }
                    buildcraft.transport.BCTransportAttachments.WireColoursPlaced placed =
                        buildcraft.transport.BCTransportAttachments.wireColours(player);
                    if (placed.markPlaced(wireColour)) {

                        buildcraft.lib.misc.AdvancementUtil.unlockAdvancement(
                            player, ADVANCEMENT_COLORFUL_ELECTRICIAN, wireColour.getName());
                    }
                    buildcraft.transport.BCTransportAttachments.recordPluggablePlacement(
                        player, buildcraft.transport.BCTransportAttachments.PluggablesPlaced.Kind.WIRE);
                    level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                }
                return InteractionResult.SUCCESS;
            }
        }

        var pipe = tile.getPipe();
        buildcraft.api.core.EnumPipePart hitPart = getHitPart(tile, hitResult);

        Direction plugDir = getHitPluggable(tile,
                hitResult.getLocation().x - pos.getX(),
                hitResult.getLocation().y - pos.getY(),
                hitResult.getLocation().z - pos.getZ());
        if (plugDir != null) {
            buildcraft.api.transport.pluggable.PipePluggable existing = tile.getPluggable(plugDir);
            if (existing != null) {
                if (existing.onPluggableActivate(player, hitResult,
                        (float) hitResult.getLocation().x, (float) hitResult.getLocation().y,
                        (float) hitResult.getLocation().z)) {
                    return InteractionResult.SUCCESS;
                }
            }
        }

        if (pipe.getBehaviour().onPipeActivate(player, hitResult,
                (float) hitResult.getLocation().x, (float) hitResult.getLocation().y,
                (float) hitResult.getLocation().z, hitPart)) {
            return InteractionResult.SUCCESS;
        }
        if (pipe.getFlow().onFlowActivate(player, hitResult,
                (float) hitResult.getLocation().x, (float) hitResult.getLocation().y,
                (float) hitResult.getLocation().z, hitPart)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    public static Direction resolveTargetFace(TilePipeHolder tile, BlockHitResult hitResult) {
        Direction armFace = getHitFace(tile, hitResult);
        return armFace != null ? armFace : hitResult.getDirection();
    }

    static boolean isWireConnected(Level level, BlockPos pos, TilePipeHolder tile,
                                   buildcraft.api.transport.EnumWirePart wirePart,
                                   DyeColor colour) {
        buildcraft.api.transport.WireNode from = new buildcraft.api.transport.WireNode(pos, wirePart);
        for (Direction dir : Direction.values()) {
            buildcraft.api.transport.WireNode to = from.offset(dir);
            if (to.pos == from.pos) {
                if (tile.getWireManager().getColorOfPart(to.part) == colour) {
                    return true;
                }
            } else {
                BlockEntity neighbour = level.getBlockEntity(to.pos);
                if (neighbour instanceof TilePipeHolder other
                    && other.getWireManager().getColorOfPart(to.part) == colour) {
                    return true;
                }
            }
        }
        return false;
    }

    public static buildcraft.api.transport.EnumWirePart resolveTargetWirePart(BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        double lx = hitResult.getLocation().x - pos.getX();
        double ly = hitResult.getLocation().y - pos.getY();
        double lz = hitResult.getLocation().z - pos.getZ();
        return buildcraft.api.transport.EnumWirePart.get(lx > 0.5, ly > 0.5, lz > 0.5);
    }

    @Nullable
    private static Direction getHitFace(TilePipeHolder tile, BlockHitResult hitResult) {
        double lx = hitResult.getLocation().x - hitResult.getBlockPos().getX();
        double ly = hitResult.getLocation().y - hitResult.getBlockPos().getY();
        double lz = hitResult.getLocation().z - hitResult.getBlockPos().getZ();
        var pipe = tile.getPipe();
        if (pipe != null) {
            if (ly < 0.25 && pipe.isConnected(Direction.DOWN))  return Direction.DOWN;
            if (ly > 0.75 && pipe.isConnected(Direction.UP))    return Direction.UP;
            if (lz < 0.25 && pipe.isConnected(Direction.NORTH)) return Direction.NORTH;
            if (lz > 0.75 && pipe.isConnected(Direction.SOUTH)) return Direction.SOUTH;
            if (lx < 0.25 && pipe.isConnected(Direction.WEST))  return Direction.WEST;
            if (lx > 0.75 && pipe.isConnected(Direction.EAST))  return Direction.EAST;
        }
        return null;
    }

    private static buildcraft.api.core.EnumPipePart getHitPart(TilePipeHolder tile, BlockHitResult hitResult) {
        double lx = hitResult.getLocation().x - hitResult.getBlockPos().getX();
        double ly = hitResult.getLocation().y - hitResult.getBlockPos().getY();
        double lz = hitResult.getLocation().z - hitResult.getBlockPos().getZ();

        var pipe = tile.getPipe();
        if (pipe != null) {
            if (ly < 0.25 && pipe.isConnected(Direction.DOWN))  return buildcraft.api.core.EnumPipePart.fromFacing(Direction.DOWN);
            if (ly > 0.75 && pipe.isConnected(Direction.UP))    return buildcraft.api.core.EnumPipePart.fromFacing(Direction.UP);
            if (lz < 0.25 && pipe.isConnected(Direction.NORTH)) return buildcraft.api.core.EnumPipePart.fromFacing(Direction.NORTH);
            if (lz > 0.75 && pipe.isConnected(Direction.SOUTH)) return buildcraft.api.core.EnumPipePart.fromFacing(Direction.SOUTH);
            if (lx < 0.25 && pipe.isConnected(Direction.WEST))  return buildcraft.api.core.EnumPipePart.fromFacing(Direction.WEST);
            if (lx > 0.75 && pipe.isConnected(Direction.EAST))  return buildcraft.api.core.EnumPipePart.fromFacing(Direction.EAST);
        }
        return buildcraft.api.core.EnumPipePart.CENTER;
    }

    @Nullable
    public static Direction getHitPluggable(TilePipeHolder tile, double lx, double ly, double lz) {
        for (Direction dir : Direction.values()) {
            PipePluggable plug = tile.getPluggable(dir);
            if (plug != null) {
                AABB box = plug.getBoundingBox();
                if (lx >= box.minX && lx <= box.maxX
                    && ly >= box.minY && ly <= box.maxY
                    && lz >= box.minZ && lz <= box.maxZ) {
                    return dir;
                }
            }
        }
        return null;
    }

    @Nullable
    public static buildcraft.api.transport.EnumWirePart getHitWire(TilePipeHolder tile, double lx, double ly, double lz) {
        for (buildcraft.api.transport.EnumWirePart part : tile.getWireManager().parts.keySet()) {
            AABB box = part.boundingBox.inflate(WIRE_HIT_INFLATE);
            if (lx >= box.minX && lx <= box.maxX
                && ly >= box.minY && ly <= box.maxY
                && lz >= box.minZ && lz <= box.maxZ) {
                return part;
            }
        }
        return null;
    }

    @Nullable
    public static buildcraft.transport.wire.EnumWireBetween getHitWireBetween(TilePipeHolder tile, double lx, double ly, double lz) {
        for (buildcraft.transport.wire.EnumWireBetween between : tile.getWireManager().betweens.keySet()) {
            AABB box = between.boundingBox.inflate(WIRE_HIT_INFLATE);
            if (lx >= box.minX && lx <= box.maxX
                && ly >= box.minY && ly <= box.maxY
                && lz >= box.minZ && lz <= box.maxZ) {
                return between;
            }
        }
        return null;
    }

    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
                                       ItemStack toolStack, boolean willHarvest, net.minecraft.world.level.material.FluidState fluid) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TilePipeHolder tile) {
            net.minecraft.world.phys.HitResult hit = player.pick(5.0, 0.0f, false);
            if (hit instanceof BlockHitResult blockHit && pos.equals(blockHit.getBlockPos())) {
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
                            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                        }
                    }
                    return false;
                }

                buildcraft.api.transport.EnumWirePart hitWire = getHitWire(tile, lx, ly, lz);
                if (hitWire != null) {
                    if (!level.isClientSide()) {
                        net.minecraft.world.item.DyeColor col = tile.getWireManager().getColorOfPart(hitWire);
                        if (col != null) {
                            ItemStack drop = new ItemStack(buildcraft.transport.BCTransportItems.WIRE_ITEMS.get(col).get());
                            if (!player.isCreative() && !drop.isEmpty()) {
                                Block.popResource(level, pos, drop);
                            }
                        }
                    }
                    tile.getWireManager().removePart(hitWire);
                    if (!level.isClientSide()) {
                        level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                    }
                    return false;
                }

                buildcraft.transport.wire.EnumWireBetween hitBetween = getHitWireBetween(tile, lx, ly, lz);
                if (hitBetween != null) {
                    if (!level.isClientSide()) {
                        net.minecraft.world.item.DyeColor col = tile.getWireManager().getColorOfPart(hitBetween.parts[0]);
                        if (col != null) {

                            int dropCount = hitBetween.to == null ? 2 : 1;
                            ItemStack drop = new ItemStack(buildcraft.transport.BCTransportItems.WIRE_ITEMS.get(col).get(), dropCount);
                            if (!player.isCreative() && !drop.isEmpty()) {
                                Block.popResource(level, pos, drop);
                            }
                        }
                    }
                    if (hitBetween.to == null) {
                        tile.getWireManager().removeParts(java.util.Arrays.asList(hitBetween.parts));
                    } else {
                        tile.getWireManager().removePart(hitBetween.parts[0]);
                    }
                    if (!level.isClientSide()) {
                        level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TilePipeHolder tile) {
            net.minecraft.world.phys.HitResult hit = player.pick(5.0, 0.0f, false);
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
            if (!level.isClientSide() && !player.isCreative()) {

                tile.dropPipeItems(level, pos);
            }

            if (!level.isClientSide()) {
                tile.wireManager.invalidate();
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                @Nullable net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TilePipeHolder tile && tile.getPipe() != null) {
            tile.getPipe().markForUpdate();
        }
    }

    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TilePipeHolder tile) {
            if (direction != null) {
                Direction face = direction.getOpposite();
                buildcraft.api.transport.pluggable.PipePluggable plug = tile.getPluggable(face);
                if (plug != null && plug.canConnectToRedstone(face)) {
                    return true;
                }
            } else {
                for (Direction dir : Direction.values()) {
                    buildcraft.api.transport.pluggable.PipePluggable plug = tile.getPluggable(dir);
                    if (plug != null && plug.canConnectToRedstone(null)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TilePipeHolder tile) {

            return tile.getRedstoneOutput(direction.getOpposite());
        }
        return 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TilePipeHolder tile) {
            if (!(level instanceof net.minecraft.world.level.Level realLevel) || !realLevel.isClientSide()) {
                return getDefaultPipePickStack(tile);
            }
            net.minecraft.world.entity.player.Player player = net.minecraft.client.Minecraft.getInstance().player;
            if (player == null) {
                return getDefaultPipePickStack(tile);
            }
            net.minecraft.world.phys.HitResult hit = player.pick(5.0, 0.0f, false);
            if (hit instanceof BlockHitResult blockHit && pos.equals(blockHit.getBlockPos())) {
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
                buildcraft.api.transport.EnumWirePart wirePart = getHitWire(tile, lx, ly, lz);
                if (wirePart != null) {
                    net.minecraft.world.item.DyeColor col = tile.getWireManager().getColorOfPart(wirePart);
                    if (col != null) {
                        return new ItemStack(buildcraft.transport.BCTransportItems.WIRE_ITEMS.get(col).get());
                    }
                }
                buildcraft.transport.wire.EnumWireBetween wireBetween = getHitWireBetween(tile, lx, ly, lz);
                if (wireBetween != null) {
                    net.minecraft.world.item.DyeColor col = tile.getWireManager().getColorOfPart(wireBetween.parts[0]);
                    if (col != null) {
                        return new ItemStack(buildcraft.transport.BCTransportItems.WIRE_ITEMS.get(col).get());
                    }
                }
            }

            ItemStack pipeStack = getDefaultPipePickStack(tile);
            if (!pipeStack.isEmpty()) {
                return pipeStack;
            }
        }
        return super.getCloneItemStack(level, pos, state, includeData);
    }

    private static ItemStack getDefaultPipePickStack(TilePipeHolder tile) {
        if (tile.getPipe() == null) {
            return ItemStack.EMPTY;
        }
        Pipe pipe = tile.getPipe();
        PipeDefinition def = pipe.getDefinition();
        Item item = (Item) PipeApi.pipeRegistry.getItemForPipe(def);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        DyeColor col = pipe.getColour();
        if (col != null) {
            stack.set(BCTransportItems.PIPE_COLOUR.get(), col);
        }
        return stack;
    }

    public boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide()) {
            return buildcraft.transport.client.PipeHolderClientExtensions.spawnRunningParticle(
                level, pos,
                entity.getX(), entity.getZ(), entity.getBbWidth(),
                entity.getDeltaMovement().x, entity.getDeltaMovement().z,
                entity.getBoundingBox().minY
            );
        }
        return false;
    }

    public boolean addLandingEffects(BlockState state1, net.minecraft.server.level.ServerLevel level, BlockPos pos, BlockState state2, net.minecraft.world.entity.LivingEntity entity, int numberOfParticles) {
        buildcraft.lib.fabric.PacketDistributor.sendToPlayersTrackingChunk(
            level, new net.minecraft.world.level.ChunkPos(pos.getX() >> 4, pos.getZ() >> 4),
            new buildcraft.transport.net.MessagePipeLandingEffect(pos, entity.getX(), entity.getY(), entity.getZ(), numberOfParticles)
        );
        return true;
    }

    @Override
    public InteractionResult attemptPaint(Level world, BlockPos pos, BlockState state, Vec3 hitPos,
                                          @Nullable Direction hitSide, @Nullable DyeColor paintColour) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof TilePipeHolder tile)) {
            return InteractionResult.PASS;
        }
        Pipe pipe = tile.getPipe();
        if (pipe == null) {
            return InteractionResult.FAIL;
        }
        if (pipe.getColour() == paintColour || !pipe.getDefinition().canBeColoured) {
            return InteractionResult.FAIL;
        }
        pipe.setColour(paintColour);
        return InteractionResult.SUCCESS;
    }
}
