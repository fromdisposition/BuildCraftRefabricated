/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.item;

import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import buildcraft.lib.misc.AdvancementUtil;

public class ItemGuide extends Item {
    private static final Identifier ADVANCEMENT = Identifier.parse("buildcraftcore:guide");

    private final String bookName;

    public ItemGuide(Item.Properties properties, String bookName) {
        super(properties);
        this.bookName = bookName;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            buildcraft.lib.fabric.BCLibClientBridge.openGuideScreen(bookName);
        } else {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
        }
        return InteractionResult.SUCCESS;
    }
}
