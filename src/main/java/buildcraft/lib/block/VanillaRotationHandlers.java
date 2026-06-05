/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.block;

import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import buildcraft.api.blocks.CustomRotationHelper;
import buildcraft.api.blocks.ICustomRotationHandler;

import buildcraft.lib.misc.collect.OrderedEnumMap;

public class VanillaRotationHandlers {

    public static final OrderedEnumMap<Direction> ROTATE_HORIZONTAL;
    public static final OrderedEnumMap<Direction> ROTATE_FACING;
    public static final OrderedEnumMap<Direction> ROTATE_HOPPER;

    static {
        Direction e = Direction.EAST, w = Direction.WEST;
        Direction u = Direction.UP, d = Direction.DOWN;
        Direction n = Direction.NORTH, s = Direction.SOUTH;
        ROTATE_HORIZONTAL = new OrderedEnumMap<>(Direction.class, e, s, w, n);
        ROTATE_FACING = new OrderedEnumMap<>(Direction.class, e, s, d, w, n, u);
        ROTATE_HOPPER = new OrderedEnumMap<>(Direction.class, e, s, w, n, d);
    }

    public static void init() {

        CustomRotationHelper.INSTANCE.registerHandlerForAll(DispenserBlock.class, VanillaRotationHandlers::rotateFreeFacing);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(ObserverBlock.class, VanillaRotationHandlers::rotateFreeFacing);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(EndRodBlock.class, VanillaRotationHandlers::rotateFreeFacing);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(ShulkerBoxBlock.class, VanillaRotationHandlers::rotateFreeFacing);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(PistonBaseBlock.class, VanillaRotationHandlers::rotatePiston);

        CustomRotationHelper.INSTANCE.registerHandlerForAll(TripWireHookBlock.class, VanillaRotationHandlers::rotateHorizontalFreely);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(FenceGateBlock.class, VanillaRotationHandlers::rotateHorizontalFreely);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(DiodeBlock.class, VanillaRotationHandlers::rotateHorizontalFreely);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(CarvedPumpkinBlock.class, VanillaRotationHandlers::rotateHorizontalFreely);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(GlazedTerracottaBlock.class, VanillaRotationHandlers::rotateHorizontalFreely);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(AnvilBlock.class, VanillaRotationHandlers::rotateHorizontalFreely);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(EnderChestBlock.class, VanillaRotationHandlers::rotateHorizontalFreely);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(AbstractFurnaceBlock.class, VanillaRotationHandlers::rotateHorizontalFreely);

        CustomRotationHelper.INSTANCE.registerHandlerForAll(CocoaBlock.class, VanillaRotationHandlers::rotateCocoa);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(LadderBlock.class, VanillaRotationHandlers::rotateLadder);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(WallTorchBlock.class, VanillaRotationHandlers::rotateWallTorch);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(WallBannerBlock.class, VanillaRotationHandlers::rotateWallBanner);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(WallSignBlock.class, VanillaRotationHandlers::rotateWallSign);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(WallHangingSignBlock.class, VanillaRotationHandlers::rotateWallHangingSign);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(WallSkullBlock.class, VanillaRotationHandlers::rotateWallSkull);

        CustomRotationHelper.INSTANCE.registerHandlerForAll(ButtonBlock.class, VanillaRotationHandlers::rotateFaceAttached);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(LeverBlock.class, VanillaRotationHandlers::rotateFaceAttached);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(DoorBlock.class, VanillaRotationHandlers::rotateDoor);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(TrapDoorBlock.class, VanillaRotationHandlers::rotateTrapDoor);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(StairBlock.class, VanillaRotationHandlers::rotateStairs);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(ChestBlock.class, VanillaRotationHandlers::rotateChest);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(HopperBlock.class, VanillaRotationHandlers::rotateHopper);

        CustomRotationHelper.INSTANCE.registerHandlerForAll(BannerBlock.class, VanillaRotationHandlers::rotate16);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(StandingSignBlock.class, VanillaRotationHandlers::rotate16);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(CeilingHangingSignBlock.class, VanillaRotationHandlers::rotate16);
        CustomRotationHelper.INSTANCE.registerHandlerForAll(SkullBlock.class, VanillaRotationHandlers::rotate16);
    }

    private static <E extends Enum<E> & Comparable<E>> InteractionResult rotateOnce(
            Level world, BlockPos pos, BlockState state, Property<E> prop, OrderedEnumMap<E> order
    ) {
        E next = order.next(state.getValue(prop));
        world.setBlockAndUpdate(pos, state.setValue(prop, next));
        return InteractionResult.SUCCESS;
    }

    private static <E extends Enum<E> & Comparable<E>> InteractionResult rotateUntilValid(
            Level world, BlockPos pos, BlockState state, Property<E> prop, OrderedEnumMap<E> order, Predicate<E> isValid
    ) {
        E current = state.getValue(prop);
        for (int i = order.getOrderLength(); i > 1; i--) {
            current = order.next(current);
            if (isValid.test(current)) {
                world.setBlockAndUpdate(pos, state.setValue(prop, current));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    private static InteractionResult rotateFreeFacing(Level world, BlockPos pos, BlockState state, Direction side) {
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return rotateOnce(world, pos, state, BlockStateProperties.FACING, ROTATE_FACING);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateHorizontalFreely(Level world, BlockPos pos, BlockState state, Direction side) {
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return rotateOnce(world, pos, state, BlockStateProperties.HORIZONTAL_FACING, ROTATE_HORIZONTAL);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotatePiston(Level world, BlockPos pos, BlockState state, Direction side) {
        if (state.hasProperty(PistonBaseBlock.EXTENDED) && state.getValue(PistonBaseBlock.EXTENDED)) {
            return InteractionResult.FAIL;
        }
        return rotateFreeFacing(world, pos, state, side);
    }

    private static InteractionResult rotateHopper(Level world, BlockPos pos, BlockState state, Direction side) {
        if (state.hasProperty(HopperBlock.FACING)) {
            return rotateOnce(world, pos, state, HopperBlock.FACING, ROTATE_HOPPER);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult rotateFaceAttached(Level world, BlockPos pos, BlockState state, Direction side) {
        if (!state.hasProperty(BlockStateProperties.ATTACH_FACE)
                || !state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return InteractionResult.PASS;
        }

        AttachFace currentFace = state.getValue(BlockStateProperties.ATTACH_FACE);
        Direction currentFacing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int currentIdx = faceAttachedIndex(currentFace, currentFacing);

        for (int step = 1; step <= FACE_ATTACHED_CYCLE_LENGTH; step++) {
            int idx = (currentIdx + step) % FACE_ATTACHED_CYCLE_LENGTH;
            AttachFace nextFace = FACE_ATTACHED_FACE_ORDER[idx / 4];
            Direction nextFacing = FACE_ATTACHED_FACING_ORDER[idx % 4];
            BlockState candidate = state.setValue(BlockStateProperties.ATTACH_FACE, nextFace)
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, nextFacing);
            if (candidate.canSurvive(world, pos)) {
                world.setBlockAndUpdate(pos, candidate);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    private static final AttachFace[] FACE_ATTACHED_FACE_ORDER = { AttachFace.WALL, AttachFace.FLOOR, AttachFace.CEILING };
    private static final Direction[] FACE_ATTACHED_FACING_ORDER = { Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH };
    private static final int FACE_ATTACHED_CYCLE_LENGTH = FACE_ATTACHED_FACE_ORDER.length * FACE_ATTACHED_FACING_ORDER.length;

    private static int faceAttachedIndex(AttachFace face, Direction facing) {
        int faceIdx = 0;
        for (int i = 0; i < FACE_ATTACHED_FACE_ORDER.length; i++) {
            if (FACE_ATTACHED_FACE_ORDER[i] == face) { faceIdx = i; break; }
        }
        int facingIdx = 0;
        for (int i = 0; i < FACE_ATTACHED_FACING_ORDER.length; i++) {
            if (FACE_ATTACHED_FACING_ORDER[i] == facing) { facingIdx = i; break; }
        }
        return faceIdx * FACE_ATTACHED_FACING_ORDER.length + facingIdx;
    }

    private static InteractionResult rotateCocoa(Level world, BlockPos pos, BlockState state, Direction side) {
        return rotateUntilValid(world, pos, state, CocoaBlock.FACING, ROTATE_HORIZONTAL,
                f -> state.setValue(CocoaBlock.FACING, f).canSurvive(world, pos));
    }

    private static InteractionResult rotateLadder(Level world, BlockPos pos, BlockState state, Direction side) {
        return rotateUntilValid(world, pos, state, LadderBlock.FACING, ROTATE_HORIZONTAL,
                f -> state.setValue(LadderBlock.FACING, f).canSurvive(world, pos));
    }

    private static InteractionResult rotateWallTorch(Level world, BlockPos pos, BlockState state, Direction side) {
        return rotateUntilValid(world, pos, state, WallTorchBlock.FACING, ROTATE_HORIZONTAL,
                f -> state.setValue(WallTorchBlock.FACING, f).canSurvive(world, pos));
    }

    private static InteractionResult rotateWallBanner(Level world, BlockPos pos, BlockState state, Direction side) {
        return rotateUntilValid(world, pos, state, WallBannerBlock.FACING, ROTATE_HORIZONTAL,
                f -> state.setValue(WallBannerBlock.FACING, f).canSurvive(world, pos));
    }

    private static InteractionResult rotateWallSign(Level world, BlockPos pos, BlockState state, Direction side) {
        return rotateUntilValid(world, pos, state, WallSignBlock.FACING, ROTATE_HORIZONTAL,
                f -> state.setValue(WallSignBlock.FACING, f).canSurvive(world, pos));
    }

    private static InteractionResult rotateWallHangingSign(Level world, BlockPos pos, BlockState state, Direction side) {
        return rotateUntilValid(world, pos, state, WallHangingSignBlock.FACING, ROTATE_HORIZONTAL,
                f -> state.setValue(WallHangingSignBlock.FACING, f).canSurvive(world, pos));
    }

    private static InteractionResult rotateWallSkull(Level world, BlockPos pos, BlockState state, Direction side) {
        return rotateUntilValid(world, pos, state, WallSkullBlock.FACING, ROTATE_HORIZONTAL,
                f -> state.setValue(WallSkullBlock.FACING, f).canSurvive(world, pos));
    }

    private static InteractionResult rotateDoor(Level world, BlockPos pos, BlockState state, Direction side) {
        BlockPos upperPos, lowerPos;
        BlockState upperState, lowerState;

        if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            upperPos = pos;
            upperState = state;
            lowerPos = upperPos.below();
            lowerState = world.getBlockState(lowerPos);
            if (!(lowerState.getBlock() instanceof DoorBlock)) {
                return InteractionResult.PASS;
            }
        } else {
            lowerPos = pos;
            lowerState = state;
            upperPos = lowerPos.above();
            upperState = world.getBlockState(upperPos);
            if (!(upperState.getBlock() instanceof DoorBlock)) {
                return InteractionResult.PASS;
            }
        }

        Direction newFacing = ROTATE_HORIZONTAL.next(lowerState.getValue(DoorBlock.FACING));

        boolean wrap = lowerState.getValue(DoorBlock.FACING) == ROTATE_HORIZONTAL.get(0);
        DoorHingeSide newHinge = wrap
                ? (lowerState.getValue(DoorBlock.HINGE) == DoorHingeSide.LEFT ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT)
                : lowerState.getValue(DoorBlock.HINGE);

        BlockState newLower = lowerState.setValue(DoorBlock.FACING, newFacing).setValue(DoorBlock.HINGE, newHinge);
        BlockState newUpper = upperState.setValue(DoorBlock.FACING, newFacing).setValue(DoorBlock.HINGE, newHinge);

        world.setBlock(lowerPos, newLower, Block.UPDATE_CLIENTS);
        world.setBlock(upperPos, newUpper, Block.UPDATE_CLIENTS);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult rotateTrapDoor(Level world, BlockPos pos, BlockState state, Direction side) {
        if (state.getValue(TrapDoorBlock.FACING) == ROTATE_HORIZONTAL.get(0)) {
            Half half = state.getValue(TrapDoorBlock.HALF);
            state = state.setValue(TrapDoorBlock.HALF, half == Half.TOP ? Half.BOTTOM : Half.TOP);
        }
        return rotateOnce(world, pos, state, TrapDoorBlock.FACING, ROTATE_HORIZONTAL);
    }

    private static InteractionResult rotateStairs(Level world, BlockPos pos, BlockState state, Direction side) {
        if (state.getValue(StairBlock.FACING) == ROTATE_HORIZONTAL.get(0)) {
            Half half = state.getValue(StairBlock.HALF);
            half = (half == Half.TOP) ? Half.BOTTOM : Half.TOP;
            state = state.setValue(StairBlock.HALF, half);
        }

        BlockState next = state.setValue(StairBlock.FACING, ROTATE_HORIZONTAL.next(state.getValue(StairBlock.FACING)));
        world.setBlockAndUpdate(pos, next);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult rotateChest(Level world, BlockPos pos, BlockState state, Direction side) {
        if (state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {

            for (Direction d : Direction.Plane.HORIZONTAL) {
                BlockPos otherPos = pos.relative(d);
                BlockState otherState = world.getBlockState(otherPos);
                if (otherState.getBlock() == state.getBlock()
                        && otherState.getValue(ChestBlock.FACING) == state.getValue(ChestBlock.FACING)
                        && otherState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                    Direction newFacing = state.getValue(ChestBlock.FACING).getOpposite();

                    ChestType selfType = state.getValue(ChestBlock.TYPE);
                    ChestType otherType = otherState.getValue(ChestBlock.TYPE);
                    world.setBlockAndUpdate(pos, state
                            .setValue(ChestBlock.FACING, newFacing)
                            .setValue(ChestBlock.TYPE, otherType));
                    world.setBlockAndUpdate(otherPos, otherState
                            .setValue(ChestBlock.FACING, newFacing)
                            .setValue(ChestBlock.TYPE, selfType));
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return rotateOnce(world, pos, state, ChestBlock.FACING, ROTATE_HORIZONTAL);
    }

    private static InteractionResult rotate16(Level world, BlockPos pos, BlockState state, Direction side) {
        IntegerProperty prop = BlockStateProperties.ROTATION_16;
        if (state.hasProperty(prop)) {
            int next = (state.getValue(prop) + 1) & 0xF;
            world.setBlockAndUpdate(pos, state.setValue(prop, next));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

}
