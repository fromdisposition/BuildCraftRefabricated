/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.lib.fabric.BcRegistryUtil;
import buildcraft.api.lists.ListMatchHandler;
import buildcraft.api.lists.ListRegistry;
import buildcraft.lib.misc.NBTUtilBC;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

public final class ListHandler {
   public static final int WIDTH = 9;
   public static final int HEIGHT = 2;

   private ListHandler() {
   }

   public static boolean hasItems(@Nonnull ItemStack stack) {
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (customData == null) {
         return false;
      }

      CompoundTag data = customData.copyTag();
      if (!data.contains("written")) {
         return false;
      }

      for (ListHandler.Line l : getLines(stack)) {
         if (l.hasItems()) {
            return true;
         }
      }

      return false;
   }

   public static boolean isDefault(@Nonnull ItemStack stack) {
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (customData == null) {
         return true;
      }

      for (ListHandler.Line l : getLines(stack)) {
         if (!l.isDefault()) {
            return false;
         }
      }

      return true;
   }

   public static ListHandler.Line[] getLines(@Nonnull ItemStack item) {
      CustomData customData = (CustomData)item.get(DataComponents.CUSTOM_DATA);
      if (customData != null) {
         CompoundTag data = customData.copyTag();
         if (data.contains("written") && data.contains("lines")) {
            ListTag list = (ListTag)BcNbt.getList(data, "lines");
            if (list != null) {
               ListHandler.Line[] lines = new ListHandler.Line[list.size()];

               for (int i = 0; i < lines.length; i++) {
                  CompoundTag lineTag = (CompoundTag)BcNbt.getCompound(list, i);
                  lines[i] = lineTag != null ? ListHandler.Line.fromTag(lineTag) : new ListHandler.Line();
               }

               return lines;
            }
         }
      }

      ListHandler.Line[] lines = new ListHandler.Line[2];

      for (int i = 0; i < lines.length; i++) {
         lines[i] = new ListHandler.Line();
      }

      return lines;
   }

   public static void saveLines(@Nonnull ItemStack stackList, ListHandler.Line[] lines) {
      boolean hasLine = false;

      for (ListHandler.Line l : lines) {
         if (!l.isDefault()) {
            hasLine = true;
            break;
         }
      }

      if (hasLine) {
         stackList.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, customData -> {
            CompoundTag data = customData.copyTag();
            data.putBoolean("written", true);
            ListTag lineList = new ListTag();

            for (ListHandler.Line saving : lines) {
               lineList.add(saving.toTag());
            }

            data.put("lines", lineList);
            return CustomData.of(data);
         });
      } else {
         CustomData customData = (CustomData)stackList.get(DataComponents.CUSTOM_DATA);
         if (customData != null) {
            CompoundTag data = customData.copyTag();
            data.remove("written");
            data.remove("lines");
            if (data.isEmpty()) {
               stackList.remove(DataComponents.CUSTOM_DATA);
            } else {
               stackList.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            }
         }
      }
   }

   public static boolean matches(@Nonnull ItemStack stackList, @Nonnull ItemStack item) {
      CustomData customData = (CustomData)stackList.get(DataComponents.CUSTOM_DATA);
      if (customData == null) {
         return false;
      }

      CompoundTag data = customData.copyTag();
      if (data.contains("written") && data.contains("lines")) {
         ListTag list = (ListTag)BcNbt.getList(data, "lines");
         if (list != null) {
            for (int i = 0; i < list.size(); i++) {
               CompoundTag lineTag = (CompoundTag)BcNbt.getCompound(list, i);
               if (lineTag != null) {
                  ListHandler.Line line = ListHandler.Line.fromTag(lineTag);
                  if (line.matches(item)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   public static class Line {
      public final NonNullList<ItemStack> stacks = NonNullList.withSize(9, ItemStack.EMPTY);
      public boolean precise;
      public boolean byType;
      public boolean byMaterial;

      public boolean isDefault() {
         return !this.precise && !this.byType && !this.byMaterial ? !this.hasItems() : false;
      }

      public boolean hasItems() {
         for (ItemStack stack : this.stacks) {
            if (!stack.isEmpty()) {
               return true;
            }
         }

         return false;
      }

      public boolean isOneStackMode() {
         return this.byType || this.byMaterial;
      }

      public boolean getOption(int id) {
         return id == 0 ? this.precise : (id == 1 ? this.byType : this.byMaterial);
      }

      public void toggleOption(int id) {
         if (!this.byType && !this.byMaterial && (id == 1 || id == 2)) {
            for (int i = 1; i < this.stacks.size(); i++) {
               this.stacks.set(i, ItemStack.EMPTY);
            }
         }

         switch (id) {
            case 0:
               this.precise = !this.precise;
               if (this.precise) {
                  this.byType = false;
                  this.byMaterial = false;
               }
               break;
            case 1:
               this.byType = !this.byType;
               if (this.byType) {
                  this.precise = false;
               }
               break;
            case 2:
               this.byMaterial = !this.byMaterial;
               if (this.byMaterial) {
                  this.precise = false;
               }
         }
      }

      public boolean matches(@Nonnull ItemStack target) {
         if (target.isEmpty()) {
            return false;
         }

         if (!this.byType && !this.byMaterial) {
            for (ItemStack s : this.stacks) {
               if (!s.isEmpty() && ItemStack.isSameItem(s, target) && (!this.precise || ItemStack.isSameItemSameComponents(s, target))) {
                  return true;
               }
            }

            return false;
         } else {
            ItemStack source = (ItemStack)this.stacks.get(0);
            if (source.isEmpty()) {
               return false;
            }

            boolean anyClaimed = false;
            if (this.byType) {
               for (ListMatchHandler h : ListRegistry.getHandlers()) {
                  if (h.isValidSource(ListMatchHandler.Type.TYPE, source)) {
                     anyClaimed = true;
                     if (h.matches(ListMatchHandler.Type.TYPE, source, target, this.precise)) {
                        return true;
                     }
                  }
               }
            }

            if (this.byMaterial) {
               for (ListMatchHandler h : ListRegistry.getHandlers()) {
                  if (h.isValidSource(ListMatchHandler.Type.MATERIAL, source)) {
                     anyClaimed = true;
                     if (h.matches(ListMatchHandler.Type.MATERIAL, source, target, this.precise)) {
                        return true;
                     }
                  }
               }
            }

            return !anyClaimed && ItemStack.isSameItem(source, target);
         }
      }

      public static ListHandler.Line fromTag(CompoundTag data) {
         ListHandler.Line line = new ListHandler.Line();
         if (data != null && data.contains("st")) {
            ListTag l = (ListTag)BcNbt.getList(data, "st");
            if (l != null) {
               for (int i = 0; i < l.size() && i < 9; i++) {
                  CompoundTag itemTag = (CompoundTag)BcNbt.getCompound(l, i);
                  if (itemTag != null) {
                     Tag stackPayload = itemTag.get("stack");
                     if (stackPayload != null) {
                        int slotIdx = i;
                        ItemStack.CODEC
                           .parse(NBTUtilBC.registryAwareOps(), stackPayload)
                           .resultOrPartial()
                           .filter(s -> !s.isEmpty())
                           .ifPresent(s -> line.stacks.set(slotIdx, s));
                     } else if (itemTag.contains("id")) {
                        String itemId = BcNbt.getString(itemTag, "id", "");
                        int count = BcNbt.getInt(itemTag, "count", 1);
                        Identifier id = Identifier.tryParse(itemId);
                        if (id != null) {
                           Item item = BcRegistryUtil.getItem(id);
                           if (item != null && item != Items.AIR) {
                              line.stacks.set(i, new ItemStack(item, count));
                           }
                        }
                     }
                  }
               }
            }

            line.precise = BcNbt.getBoolean(data, "Fp", false);
            line.byType = BcNbt.getBoolean(data, "Ft", false);
            line.byMaterial = BcNbt.getBoolean(data, "Fm", false);
         }

         return line;
      }

      public CompoundTag toTag() {
         CompoundTag data = new CompoundTag();
         ListTag stackList = new ListTag();

         for (ItemStack stack : this.stacks) {
            CompoundTag stackTag = new CompoundTag();
            if (!stack.isEmpty()) {
               ItemStack.CODEC.encodeStart(NBTUtilBC.registryAwareOps(), stack).resultOrPartial().ifPresent(payload -> stackTag.put("stack", payload));
            }

            stackList.add(stackTag);
         }

         data.put("st", stackList);
         data.putBoolean("Fp", this.precise);
         data.putBoolean("Ft", this.byType);
         data.putBoolean("Fm", this.byMaterial);
         return data;
      }

      public void setStack(int slotIndex, @Nonnull ItemStack stack) {
         if (slotIndex == 0 || !this.byType && !this.byMaterial) {
            if (stack.isEmpty()) {
               this.stacks.set(slotIndex, ItemStack.EMPTY);
            } else {
               stack = stack.copy();
               stack.setCount(1);
               this.stacks.set(slotIndex, stack);
            }
         }
      }

      @Nonnull
      public ItemStack getStack(int i) {
         return i >= 0 && i < this.stacks.size() ? (ItemStack)this.stacks.get(i) : ItemStack.EMPTY;
      }

      public List<ItemStack> getExamples() {
         ItemStack source = (ItemStack)this.stacks.get(0);
         if (!source.isEmpty() && (this.byType || this.byMaterial)) {
            Set<Item> seen = new HashSet<>();
            seen.add(source.getItem());
            List<ItemStack> out = new ArrayList<>();
            if (this.byType) {
               collectExamples(source, ListMatchHandler.Type.TYPE, seen, out);
            }

            if (this.byMaterial) {
               collectExamples(source, ListMatchHandler.Type.MATERIAL, seen, out);
            }

            return out;
         } else {
            return new ArrayList<>();
         }
      }

      private static void collectExamples(ItemStack source, ListMatchHandler.Type t, Set<Item> seen, List<ItemStack> out) {
         for (ListMatchHandler h : ListRegistry.getHandlers()) {
            if (h.isValidSource(t, source)) {
               NonNullList<ItemStack> examples = h.getClientExamples(t, source);
               if (examples != null) {
                  for (ItemStack ex : examples) {
                     if (!ex.isEmpty() && seen.add(ex.getItem())) {
                        out.add(ex);
                     }
                  }
               }
            }
         }
      }
   }
}
