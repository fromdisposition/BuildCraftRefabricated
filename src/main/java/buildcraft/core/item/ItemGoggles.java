/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemGoggles extends Item {
   public ItemGoggles(Properties properties) {
      super(properties);
   }

   public static boolean isWearing(LivingEntity entity) {
      return entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ItemGoggles;
   }
}
