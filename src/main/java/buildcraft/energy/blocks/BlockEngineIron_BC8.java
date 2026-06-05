/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.energy.blocks;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.lib.engine.BlockEngineBase_BC8;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.SoundUtil;

public class BlockEngineIron_BC8 extends BlockEngineBase_BC8 {
    public BlockEngineIron_BC8(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEngineIron_BC8(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        boolean isWrench = !stack.isEmpty() && stack.getItem() instanceof IToolWrench;
        BlockEntity be = level.getBlockEntity(pos);
        TileEngineIron_BC8 engine = (be instanceof TileEngineIron_BC8 e) ? e : null;

        if (isWrench && engine != null && engine.getPowerStage() == EnumPowerStage.OVERHEAT) {
            if (!level.isClientSide()) {
                engine.clearOverheat(player);
                SoundUtil.playSlideSound(level, pos, state, InteractionResult.SUCCESS);
            }
            player.swing(hand);
            return InteractionResult.CONSUME;
        }

        if (stack.getItem() instanceof IItemPipe pipe) {
            InteractionResult placed = EnginePipeInteraction.tryPlacePipe(
                    pipe, stack, level, player, hand, hitResult, PipeApi.flowFluids, PipeApi.flowPower);
            return placed != null ? placed : openGui(state, level, pos, player);
        }

        if (player.isShiftKeyDown()) {
            return openGui(state, level, pos, player);
        }

        if (isWrench) {
            if (engine != null && engine.hasAlternateReceiver()) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide()) {
                level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.4f, 1.3f);
            }
            player.swing(hand);
            return InteractionResult.CONSUME;
        }

        if (engine != null && FluidUtilBC.onTankActivated(player, pos, hand, engine.combinedFluidHandler)) {
            return InteractionResult.SUCCESS;
        }
        if (FluidUtilBC.isFluidContainerInHand(player, hand)) {
            return InteractionResult.SUCCESS;
        }

        return openGui(state, level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hitResult) {
        return openGui(state, level, pos, player);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state,
            Player player) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileEngineIron_BC8 engine) {
                net.minecraft.core.NonNullList<ItemStack> drops = net.minecraft.core.NonNullList.create();
                buildcraft.api.items.FluidItemDrops.addFluidDrops(drops,
                        engine.tankFuel, engine.tankCoolant, engine.tankResidue);
                for (ItemStack drop : drops) {
                    Block.popResource(level, pos, drop);
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private InteractionResult openGui(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileEngineIron_BC8 engine && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(engine);
        }
        return InteractionResult.SUCCESS;
    }
}
