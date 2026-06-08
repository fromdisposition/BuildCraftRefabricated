/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import buildcraft.api.lists.ListMatchHandler;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public class ListMatchHandlerArmor extends ListMatchHandler {
   @Nullable
   private static EquipmentSlot slotOf(@Nonnull ItemStack stack) {
      Equippable e = (Equippable)stack.get(DataComponents.EQUIPPABLE);
      return e == null ? null : e.slot();
   }

   @Override
   public boolean isValidSource(ListMatchHandler.Type type, @Nonnull ItemStack stack) {
      return !stack.isEmpty() && type == ListMatchHandler.Type.TYPE ? slotOf(stack) != null : false;
   }

   @Override
   public boolean matches(ListMatchHandler.Type type, @Nonnull ItemStack source, @Nonnull ItemStack target, boolean precise) {
      if (type != ListMatchHandler.Type.TYPE) {
         return false;
      }

      EquipmentSlot src = slotOf(source);
      EquipmentSlot tgt = slotOf(target);
      return src != null && src == tgt;
   }

   @Nonnull
   @Override
   public List<String> describeMatch(ListMatchHandler.Type type, @Nonnull ItemStack stack) {
      if (type != ListMatchHandler.Type.TYPE) {
         return List.of();
      }

      EquipmentSlot slot = slotOf(stack);
      return slot == null ? List.of() : List.of("equipment slot: " + slot.getName());
   }

   @Nullable
   @Override
   public NonNullList<ItemStack> getClientExamples(ListMatchHandler.Type type, @Nonnull ItemStack stack) {
      if (type != ListMatchHandler.Type.TYPE) {
         return null;
      }

      EquipmentSlot wanted = slotOf(stack);
      if (wanted == null) {
         return null;
      }

      NonNullList<ItemStack> out = NonNullList.create();

      for (Item item : BuiltInRegistries.ITEM) {
         ItemStack def = item.getDefaultInstance();
         if (!def.isEmpty()) {
            EquipmentSlot s = slotOf(def);
            if (s == wanted) {
               out.add(def);
            }
         }
      }

      return out;
   }
}
