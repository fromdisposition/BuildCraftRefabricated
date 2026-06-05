/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public abstract class ItemAddon extends Item {
    public ItemAddon(Item.Properties properties) {
        super(properties);
    }

    public abstract Addon createAddon();

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) return InteractionResult.PASS;
        return tryAttach(context.getLevel(), context.getPlayer(), context.getHand());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.PASS;
        return tryAttach(level, player, hand);
    }

    private InteractionResult tryAttach(Level level, Player player, InteractionHand hand) {
        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(level);
        Pair<VolumeBox, EnumAddonSlot> targeted =
            EnumAddonSlot.getSelectingVolumeBoxAndSlot(player, volumeBoxes.volumeBoxes);
        VolumeBox volumeBox = targeted.getLeft();
        EnumAddonSlot slot = targeted.getRight();

        if (volumeBox == null || slot == null) return InteractionResult.PASS;
        if (volumeBox.addons.containsKey(slot)) return InteractionResult.PASS;

        Addon addon = createAddon();
        if (!addon.canBePlaceInto(volumeBox)) return InteractionResult.PASS;

        addon.volumeBox = volumeBox;
        volumeBox.addons.put(slot, addon);
        addon.onAdded();
        volumeBoxes.markDirtyAndBroadcast();

        ItemStack held = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
