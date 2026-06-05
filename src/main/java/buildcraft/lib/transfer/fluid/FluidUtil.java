/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.fluid;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import buildcraft.lib.attachments.Attachments;
import buildcraft.lib.common.SoundActions;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.fluids.FluidConstants;
import buildcraft.lib.fluids.FluidType;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.ResourceHandlerUtil;
import buildcraft.lib.transfer.access.ItemAccess;
import buildcraft.lib.transfer.resource.ResourceStack;
import buildcraft.lib.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public final class FluidUtil {
    private static final Logger LOGGER = LogUtils.getLogger();

    private FluidUtil() {}

    public static FluidStack getStack(ResourceHandler<FluidResource> handler, int index) {
        var resource = handler.getResource(index);
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        return resource.toStack(handler.getAmountAsInt(index));
    }

    public static FluidStack getFirstStackContained(ItemStack stack) {
        if (stack.isEmpty()) {
            return FluidStack.EMPTY;
        }
        var handler = ItemAccess.forStack(stack).oneByOne().getCapability(Attachments.Fluid.ITEM);
        if (handler == null) {
            return FluidStack.EMPTY;
        }
        int size = handler.size();
        for (int index = 0; index < size; ++index) {
            var fluidStack = getStack(handler, index);
            if (!fluidStack.isEmpty()) {
                return fluidStack;
            }
        }
        return FluidStack.EMPTY;
    }

    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, Level level, BlockPos pos, @Nullable Direction side) {
        Preconditions.checkNotNull(level);
        Preconditions.checkNotNull(pos);

        var fluidHandler = buildcraft.lib.attachments.AttachmentQueries.getBlock(level, Attachments.Fluid.BLOCK, pos, side);
        return fluidHandler != null && interactWithFluidHandler(player, hand, pos, fluidHandler);
    }

    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, @Nullable BlockPos pos, ResourceHandler<FluidResource> handler) {
        var itemAccess = ItemAccess.forPlayerInteraction(player, hand).oneByOne();
        var handHandler = itemAccess.getCapability(Attachments.Fluid.ITEM);
        if (handHandler == null) {
            return false;
        }

        return moveWithSound(handler, handHandler, player.level(), pos, player, true) != null
                || moveWithSound(handHandler, handler, player.level(), pos, player, false) != null;
    }

    @Nullable
    private static ResourceStack<FluidResource> moveWithSound(ResourceHandler<FluidResource> from, ResourceHandler<FluidResource> to, Level level, @Nullable BlockPos pos, @Nullable Player player, boolean pickup) {
        if (player == null && pos == null) {
            throw new IllegalArgumentException("Either player or pos must be provided.");
        }

        var moved = ResourceHandlerUtil.moveFirst(from, to, fr -> true, Integer.MAX_VALUE, null);
        if (moved != null) {
            playSoundAndGameEvent(moved.resource(), level, pos, player, pickup);
        }
        return moved;
    }

    private static void playSoundAndGameEvent(FluidResource resource, Level level, @Nullable BlockPos blockPos, @Nullable Player player, boolean pickup) {
        if (player == null && blockPos == null) {
            throw new IllegalArgumentException("Either player or blockPos must be provided.");
        }

        Vec3 position = blockPos != null ? Vec3.atCenterOf(blockPos) : new Vec3(player.getX(), player.getY() + 0.5, player.getZ());

        triggerSoundAndGameEvent(resource, level, position, player, pickup);
    }

    public static void triggerSoundAndGameEvent(FluidResource resource, Level level, Vec3 position, @Nullable Player player, boolean pickup) {
        var fluidType = resource.getFluidType();
        var soundEvent = fluidType.getSound(pickup ? SoundActions.BUCKET_FILL : SoundActions.BUCKET_EMPTY);
        if (soundEvent != null) {
            level.playSound(null, position.x, position.y, position.z, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        level.gameEvent(player, pickup ? GameEvent.FLUID_PICKUP : GameEvent.FLUID_PLACE, position);
    }

    public static FluidStack tryPickupFluid(@Nullable ResourceHandler<FluidResource> destination, @Nullable Player player, Level level, BlockPos pos, @Nullable Direction side) {
        if (destination == null) {
            return FluidStack.EMPTY;
        }

        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof BucketPickup bucketPickup) {

            Fluid fluid = level.getFluidState(pos).getType();
            if (fluid == Fluids.EMPTY) {
                return FluidStack.EMPTY;
            }

            try (var tx = Transaction.openRoot()) {
                var resource = buildcraft.lib.misc.FluidUtilBC.canonicalFluidResource(FluidResource.of(fluid));
                int inserted = destination.insert(resource, FluidConstants.BUCKET_VOLUME, tx);
                if (inserted != FluidConstants.BUCKET_VOLUME) {
                    return FluidStack.EMPTY;
                }

                if (level.getFluidState(pos).getType() != fluid) {

                    return FluidStack.EMPTY;
                }
                ItemStack pickedUpStack = bucketPickup.pickupBlock(player, level, pos, level.getBlockState(pos));
                if (!(pickedUpStack.getItem() instanceof BucketItem bucket)) {

                    if (!pickedUpStack.isEmpty()) {

                        LOGGER.warn("Picked up stack is not a bucket. Fluid {} at {} in {} picked up as {}.",
                                BuiltInRegistries.FLUID.getKey(fluid), pos, level.dimension().identifier(), pickedUpStack);
                    }
                    return FluidStack.EMPTY;
                }
                Fluid bucketFluid = buildcraft.lib.fabric.Mc26Compat.bucketFluid(bucket);
                FluidStack extracted = new FluidStack(bucketFluid, FluidConstants.BUCKET_VOLUME);
                if (!buildcraft.lib.misc.FluidUtilBC.areEquivalentFluidResources(resource, FluidResource.of(extracted))) {

                    LOGGER.warn("Fluid removed without successfully being picked up. Fluid {} at {} in {} matched requested type, but after performing pickup was {}.",
                            BuiltInRegistries.FLUID.getKey(fluid), pos, level.dimension().identifier(), BuiltInRegistries.FLUID.getKey(bucketFluid));
                    return FluidStack.EMPTY;
                }
                tx.commit();
                playSoundAndGameEvent(resource, level, pos, player, true);
                return extracted;
            }
        } else {
            var fluidHandler = buildcraft.lib.attachments.AttachmentQueries.getBlock(level, Attachments.Fluid.BLOCK, pos, state, null, side);
            if (fluidHandler == null) {
                return FluidStack.EMPTY;
            }
            var moved = moveWithSound(fluidHandler, destination, level, pos, player, true);
            return moved != null ? moved.resource().toStack(moved.amount()) : FluidStack.EMPTY;
        }
    }

    public static FluidStack tryPlaceFluid(@Nullable ResourceHandler<FluidResource> source, @Nullable Player player, Level level, InteractionHand hand, BlockPos pos) {
        if (source == null) {
            return FluidStack.EMPTY;
        }
        int size = source.size();
        for (int index = 0; index < size; ++index) {
            var resource = source.getResource(index);
            if (resource.isEmpty()) {
                continue;
            }
            try (var tx = Transaction.openRoot()) {
                int amount = source.extract(index, resource, FluidConstants.BUCKET_VOLUME, tx);
                if (amount != FluidConstants.BUCKET_VOLUME) {
                    continue;
                }

                if (tryPlaceFluid(resource, player, level, hand, pos)) {
                    tx.commit();
                    return resource.toStack(FluidConstants.BUCKET_VOLUME);
                }
            }
        }
        return FluidStack.EMPTY;
    }

    public static boolean tryPlaceFluid(FluidResource resource, @Nullable Player player, Level level, InteractionHand hand, BlockPos pos) {
        var stack = resource.toStack(FluidConstants.BUCKET_VOLUME);
        var fluidType = resource.getFluidType();
        if (stack.isEmpty() || !fluidType.canBePlacedInLevel(level, pos, stack)) {
            return false;
        }

        var handItem = player == null ? ItemStack.EMPTY : player.getItemInHand(hand);
        BlockPlaceContext context = new BlockPlaceContext(level, player, hand, handItem, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));

        BlockState destBlockState = level.getBlockState(pos);
        boolean isDestReplaceable = destBlockState.canBeReplaced(context);
        boolean canDestContainFluid = destBlockState.getBlock() instanceof LiquidBlockContainer lbc
                && lbc.canPlaceLiquid(player, level, pos, destBlockState, resource.getFluid());
        if (!destBlockState.isAir() && !isDestReplaceable && !canDestContainFluid) {
            return false;
        }

        if (fluidType.isVaporizedOnPlacement(level, pos, stack)) {
            fluidType.onVaporize(player, level, pos, stack);
            return true;
        } else {
            if (canDestContainFluid) {
                LiquidBlockContainer lbc = (LiquidBlockContainer) destBlockState.getBlock();
                lbc.placeLiquid(level, pos, destBlockState, resource.getFluid().defaultFluidState());
            } else {

                if (!level.isClientSide() && isDestReplaceable && !destBlockState.liquid()) {
                    level.destroyBlock(pos, true);
                }
                var state = fluidType.getBlockForFluidState(level, pos, resource.getFluid().defaultFluidState());
                level.setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE);
            }
            playSoundAndGameEvent(resource, level, pos, player, false);
            return true;
        }
    }
}
