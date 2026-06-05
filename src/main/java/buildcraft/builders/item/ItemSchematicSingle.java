/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.item;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;

import buildcraft.lib.inventory.InventoryWrapper;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.lib.misc.StackUtil;

import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.snapshot.SchematicBlockManager;
import buildcraft.core.PaperAdvancement;

@SuppressWarnings("deprecation")
public class ItemSchematicSingle extends Item {
    public static final String NBT_KEY = "schematic";

    private final boolean used;

    public ItemSchematicSingle(Item.Properties properties, boolean used) {
        super(properties);
        this.used = used;
    }

    public boolean isUsed() {
        return used;
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        if (world.isClientSide()) {
            return InteractionResult.PASS;
        }
        if (used && player.isShiftKeyDown()) {

            ItemStack stack = player.getItemInHand(hand);
            ItemStack clean = new ItemStack(BCBuildersItems.SCHEMATIC_SINGLE_CLEAN.get(), 1);
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, clean));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (world.isClientSide()) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        InteractionHand hand = context.getHand();
        ItemStack stack = player.getItemInHand(hand);
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();

        if (used && player.isShiftKeyDown()) {
            ItemStack clean = new ItemStack(BCBuildersItems.SCHEMATIC_SINGLE_CLEAN.get(), 1);
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, clean));
            return InteractionResult.SUCCESS;
        }

        if (!used) {

            BlockState state = world.getBlockState(pos);
            ISchematicBlock schematicBlock = SchematicBlockManager.getSchematicBlock(new SchematicBlockContext(
                world, pos, pos, state, state.getBlock()
            ));
            if (schematicBlock.isAir()) {
                return InteractionResult.FAIL;
            }

            ItemStack usedStack = new ItemStack(BCBuildersItems.SCHEMATIC_SINGLE_USED.get(), 1);
            CompoundTag itemData = new CompoundTag();
            itemData.put(NBT_KEY, SchematicBlockManager.writeToNBT(schematicBlock));
            NBTUtilBC.setItemData(usedStack, itemData);
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, usedStack));
            AdvancementUtil.unlockAdvancement(player, PaperAdvancement.ID,
                PaperAdvancement.CAPTURE_WITH_SCHEMATIC);
            return InteractionResult.SUCCESS;
        } else {

            BlockPos placePos = pos;
            BlockState clickedState = world.getBlockState(pos);
            boolean replaceable = clickedState.canBeReplaced();
            if (!replaceable) {
                placePos = placePos.relative(side);
            }
            BlockState placeState = world.getBlockState(placePos);
            if (!replaceable && !placeState.canBeReplaced()) {
                return InteractionResult.FAIL;
            }
            if (replaceable && !world.isEmptyBlock(placePos)) {
                world.removeBlock(placePos, false);
            }
            try {
                ISchematicBlock schematicBlock = getSchematic(stack);
                if (schematicBlock != null) {
                    if (!schematicBlock.isBuilt(world, placePos) && schematicBlock.canBuild(world, placePos)) {
                        List<FluidStack> requiredFluids = schematicBlock.computeRequiredFluids();
                        List<ItemStack> requiredItems = schematicBlock.computeRequiredItems();
                        if (requiredFluids.isEmpty()) {
                            InventoryWrapper itemTransactor = new InventoryWrapper(player.getInventory());
                            if (StackUtil.mergeSameItems(requiredItems).stream().noneMatch(s ->
                                itemTransactor.extract(
                                    extracted -> StackUtil.canMerge(s, extracted),
                                    s.getCount(),
                                    s.getCount(),
                                    true
                                ).isEmpty()
                            )) {
                                if (schematicBlock.build(world, placePos)) {
                                    StackUtil.mergeSameItems(requiredItems).forEach(s ->
                                        itemTransactor.extract(
                                            extracted -> StackUtil.canMerge(s, extracted),
                                            s.getCount(),
                                            s.getCount(),
                                            false
                                        )
                                    );
                                    SoundUtil.playBlockPlace(world, placePos);
                                    player.swing(hand);
                                    return InteractionResult.SUCCESS;
                                }
                            } else {
                                buildcraft.lib.misc.MessageUtil.sendOverlayMessage(player,
                                    Component.literal(
                                        "Not enough items. Total needed: " +
                                            StackUtil.mergeSameItems(requiredItems).stream()
                                                .map(s -> s.getHoverName().getString() + " x " + s.getCount())
                                                .collect(Collectors.joining(", "))
                                    )
                                );
                            }
                        } else {
                            buildcraft.lib.misc.MessageUtil.sendOverlayMessage(player,
                                Component.literal("Schematic requires fluids")
                            );
                        }
                    }
                }
            } catch (InvalidInputDataException e) {
                buildcraft.lib.misc.MessageUtil.sendOverlayMessage(player,
                    Component.literal("Invalid schematic: " + e.getMessage())
                );
                BCLog.logger.warn("[builders.schematic] Player tried to use an invalid schematic", e);
            }
            return InteractionResult.FAIL;
        }
    }

    public static ISchematicBlock getSchematic(@Nonnull ItemStack stack) throws InvalidInputDataException {
        if (stack.getItem() instanceof ItemSchematicSingle) {
            CompoundTag itemData = NBTUtilBC.getItemData(stack);
            if (itemData.contains(NBT_KEY)) {
                return SchematicBlockManager.readFromNBT(itemData.getCompoundOrEmpty(NBT_KEY));
            }
        }
        return null;
    }

    public static ISchematicBlock getSchematicSafe(@Nonnull ItemStack stack) {
        try {
            return getSchematic(stack);
        } catch (InvalidInputDataException e) {
            BCLog.logger.warn("Invalid schematic " + e.getMessage());
            return null;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                TooltipDisplay display, Consumer<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (!used) {
            tooltip.accept(Component.translatable("item.blueprint.blank").withStyle(ChatFormatting.GRAY));
            tooltip.accept(
                Component.translatable("item.schematic_single.use_hint", Component.keybind("key.use"))
                    .withStyle(ChatFormatting.GRAY)
            );
            return;
        }
        ISchematicBlock schematic = getSchematicSafe(stack);
        if (schematic == null) {
            return;
        }
        BlockState state = schematic.getBlockStateForRender();
        if (state == null) {
            return;
        }
        tooltip.accept(state.getBlock().getName().copy().withStyle(ChatFormatting.GRAY));
    }

}
