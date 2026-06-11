/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.lib.fabric.Mc26Compat;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemStackRef {
   private final NbtRef<StringTag> item;
   private final NbtRef<IntTag> amount;
   private final NbtRef<CompoundTag> tagCompound;

   public ItemStackRef(NbtRef<StringTag> item, NbtRef<IntTag> amount, NbtRef<IntTag> meta, NbtRef<CompoundTag> tagCompound) {
      this.item = item;
      this.amount = amount;
      this.tagCompound = tagCompound;
   }

   public ItemStack get(Tag nbt) {
      Identifier itemId = Identifier.parse(((StringTag)this.item.get(nbt).orElseThrow(NullPointerException::new)).value());
      Item itemObj = Mc26Compat.getItem(itemId);
      Objects.requireNonNull(itemObj, "Unknown item: " + itemId);
      int count = Optional.ofNullable(this.amount).flatMap(ref -> ref.get(nbt)).<Integer>map(IntTag::value).orElse(1);
      ItemStack itemStack = new ItemStack(itemObj, count);
      Optional.ofNullable(this.tagCompound).flatMap(ref -> ref.get(nbt)).ifPresent(tag -> {});
      return itemStack;
   }
}
