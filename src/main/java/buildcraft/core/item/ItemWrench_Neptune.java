/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.item;

import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import buildcraft.api.blocks.CustomRotationHelper;
import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.api.tools.IToolWrench;

import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.SoundUtil;

public class ItemWrench_Neptune extends Item implements IToolWrench {
    private static final Identifier ADVANCEMENT = Identifier.parse("buildcraftcore:wrenched");

    public ItemWrench_Neptune(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean canWrench(Player player, net.minecraft.world.InteractionHand hand, ItemStack wrench,
            HitResult rayTrace) {
        return true;
    }

    @Override
    public void wrenchUsed(Player player, net.minecraft.world.InteractionHand hand, ItemStack wrench,
            HitResult rayTrace) {
        AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
        player.swing(hand);
    }

    public InteractionResult trySneakRotate(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        Level world = context.getLevel();
        net.minecraft.core.BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof ICustomRotationHandler) {
            return InteractionResult.PASS;
        }

        net.minecraft.core.Direction side = context.getClickedFace();
        InteractionResult result = CustomRotationHelper.INSTANCE.attemptRotateBlock(world, pos, state, side);
        if (result == InteractionResult.PASS) {
            return InteractionResult.PASS;
        }

        if (result == InteractionResult.SUCCESS) {
            BlockHitResult hit = new BlockHitResult(context.getClickLocation(), side, pos, context.isInside());
            wrenchUsed(player, context.getHand(), stack, hit);
        }
        SoundUtil.playSlideSound(world, pos, state, result);
        return result;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            InteractionResult sneak = trySneakRotate(context.getItemInHand(), context);
            if (sneak != InteractionResult.PASS) {
                return sneak;
            }
        }
        Level world = context.getLevel();
        net.minecraft.core.BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        net.minecraft.world.InteractionHand hand = context.getHand();
        net.minecraft.core.Direction side = context.getClickedFace();

        BlockState state = world.getBlockState(pos);

        InteractionResult result = CustomRotationHelper.INSTANCE.attemptRotateBlock(world, pos, state, side);

        if (result == InteractionResult.SUCCESS && player != null) {
            BlockHitResult hitResult = new BlockHitResult(context.getClickLocation(), side, pos, context.isInside());
            wrenchUsed(player, hand, context.getItemInHand(), hitResult);
        }

        SoundUtil.playSlideSound(world, pos, state, result);
        return result;
    }
}
