/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import buildcraft.api.lists.ListMatchHandler;
import buildcraft.api.lists.ListMatchHandler.Type;
import buildcraft.api.lists.ListRegistry;

public final class ListHandler {
    public static final int WIDTH = 9;
    public static final int HEIGHT = 2;

    public static class Line {
        public final NonNullList<ItemStack> stacks;
        public boolean precise, byType, byMaterial;

        public Line() {
            stacks = NonNullList.withSize(WIDTH, ItemStack.EMPTY);
        }

        public boolean isDefault() {
            if (precise || byType || byMaterial) return false;
            return !hasItems();
        }

        public boolean hasItems() {
            for (ItemStack stack : stacks) {
                if (!stack.isEmpty()) return true;
            }
            return false;
        }

        public boolean isOneStackMode() {
            return byType || byMaterial;
        }

        public boolean getOption(int id) {
            return id == 0 ? precise : (id == 1 ? byType : byMaterial);
        }

        public void toggleOption(int id) {
            if (!byType && !byMaterial && (id == 1 || id == 2)) {
                for (int i = 1; i < stacks.size(); i++) {
                    stacks.set(i, ItemStack.EMPTY);
                }
            }
            switch (id) {
                case 0:
                    precise = !precise;

                    if (precise) {
                        byType = false;
                        byMaterial = false;
                    }
                    break;
                case 1:
                    byType = !byType;

                    if (byType) {
                        precise = false;
                    }
                    break;
                case 2:
                    byMaterial = !byMaterial;
                    if (byMaterial) {
                        precise = false;
                    }
                    break;
            }
        }

        public boolean matches(@Nonnull ItemStack target) {
            if (target.isEmpty()) return false;
            if (byType || byMaterial) {
                ItemStack source = stacks.get(0);
                if (source.isEmpty()) return false;

                boolean anyClaimed = false;
                if (byType) {
                    for (ListMatchHandler h : ListRegistry.getHandlers()) {
                        if (h.isValidSource(Type.TYPE, source)) {
                            anyClaimed = true;
                            if (h.matches(Type.TYPE, source, target, precise)) return true;
                        }
                    }
                }
                if (byMaterial) {
                    for (ListMatchHandler h : ListRegistry.getHandlers()) {
                        if (h.isValidSource(Type.MATERIAL, source)) {
                            anyClaimed = true;
                            if (h.matches(Type.MATERIAL, source, target, precise)) return true;
                        }
                    }
                }

                return !anyClaimed && ItemStack.isSameItem(source, target);
            } else {
                for (ItemStack s : stacks) {
                    if (!s.isEmpty() && ItemStack.isSameItem(s, target)) {
                        if (!precise || ItemStack.isSameItemSameComponents(s, target)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public static Line fromTag(CompoundTag data) {
            Line line = new Line();

            if (data != null && data.contains("st")) {
                ListTag l = data.getList("st").orElse(null);
                if (l != null) {
                    for (int i = 0; i < l.size() && i < WIDTH; i++) {
                        CompoundTag itemTag = l.getCompound(i).orElse(null);
                        if (itemTag == null) continue;

                        Tag stackPayload = itemTag.get("stack");
                        if (stackPayload != null) {
                            final int slotIdx = i;
                            ItemStack.CODEC.parse(buildcraft.lib.misc.NBTUtilBC.registryAwareOps(), stackPayload)
                                    .resultOrPartial()
                                    .filter(s -> !s.isEmpty())
                                    .ifPresent(s -> line.stacks.set(slotIdx, s));
                        } else if (itemTag.contains("id")) {

                            String itemId = itemTag.getString("id").orElse("");
                            int count = itemTag.getInt("count").orElse(1);
                            net.minecraft.resources.Identifier id = net.minecraft.resources.Identifier.tryParse(itemId);
                            if (id != null) {
                                Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(id);
                                if (item != null && item != net.minecraft.world.item.Items.AIR) {
                                    line.stacks.set(i, new ItemStack(item, count));
                                }
                            }
                        }
                    }
                }

                line.precise = data.getBoolean("Fp").orElse(false);
                line.byType = data.getBoolean("Ft").orElse(false);
                line.byMaterial = data.getBoolean("Fm").orElse(false);
            }

            return line;
        }

        public CompoundTag toTag() {
            CompoundTag data = new CompoundTag();
            ListTag stackList = new ListTag();
            for (ItemStack stack : stacks) {
                CompoundTag stackTag = new CompoundTag();
                if (!stack.isEmpty()) {

                    ItemStack.CODEC.encodeStart(buildcraft.lib.misc.NBTUtilBC.registryAwareOps(), stack)
                            .resultOrPartial()
                            .ifPresent(payload -> stackTag.put("stack", payload));
                }
                stackList.add(stackTag);
            }
            data.put("st", stackList);
            data.putBoolean("Fp", precise);
            data.putBoolean("Ft", byType);
            data.putBoolean("Fm", byMaterial);
            return data;
        }

        public void setStack(int slotIndex, @Nonnull ItemStack stack) {
            if (slotIndex == 0 || (!byType && !byMaterial)) {
                if (stack.isEmpty()) {
                    stacks.set(slotIndex, ItemStack.EMPTY);
                } else {
                    stack = stack.copy();
                    stack.setCount(1);
                    stacks.set(slotIndex, stack);
                }
            }
        }

        @Nonnull
        public ItemStack getStack(int i) {
            if (i < 0 || i >= stacks.size()) {
                return ItemStack.EMPTY;
            } else {
                return stacks.get(i);
            }
        }

        public List<ItemStack> getExamples() {
            ItemStack source = stacks.get(0);
            if (source.isEmpty() || (!byType && !byMaterial)) return new ArrayList<>();
            Set<Item> seen = new HashSet<>();

            seen.add(source.getItem());
            List<ItemStack> out = new ArrayList<>();
            if (byType) collectExamples(source, Type.TYPE, seen, out);
            if (byMaterial) collectExamples(source, Type.MATERIAL, seen, out);
            return out;
        }

        private static void collectExamples(ItemStack source, Type t, Set<Item> seen, List<ItemStack> out) {
            for (ListMatchHandler h : ListRegistry.getHandlers()) {
                if (!h.isValidSource(t, source)) continue;
                NonNullList<ItemStack> examples = h.getClientExamples(t, source);
                if (examples == null) continue;
                for (ItemStack ex : examples) {
                    if (!ex.isEmpty() && seen.add(ex.getItem())) {
                        out.add(ex);
                    }
                }
            }
        }
    }

    private ListHandler() {
    }

    public static boolean hasItems(@Nonnull ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return false;
        CompoundTag data = customData.copyTag();
        if (!data.contains("written")) return false;
        for (Line l : getLines(stack)) {
            if (l.hasItems()) return true;
        }
        return false;
    }

    public static boolean isDefault(@Nonnull ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return true;
        for (Line l : getLines(stack)) {
            if (!l.isDefault()) return false;
        }
        return true;
    }

    public static Line[] getLines(@Nonnull ItemStack item) {
        CustomData customData = item.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag data = customData.copyTag();
            if (data.contains("written") && data.contains("lines")) {
                ListTag list = data.getList("lines").orElse(null);
                if (list != null) {
                    Line[] lines = new Line[list.size()];
                    for (int i = 0; i < lines.length; i++) {
                        CompoundTag lineTag = list.getCompound(i).orElse(null);
                        lines[i] = lineTag != null ? Line.fromTag(lineTag) : new Line();
                    }
                    return lines;
                }
            }
        }
        Line[] lines = new Line[HEIGHT];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = new Line();
        }
        return lines;
    }

    public static void saveLines(@Nonnull ItemStack stackList, Line[] lines) {
        boolean hasLine = false;

        for (Line l : lines) {
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
                for (Line saving : lines) {
                    lineList.add(saving.toTag());
                }
                data.put("lines", lineList);
                return CustomData.of(data);
            });
        } else {
            CustomData customData = stackList.get(DataComponents.CUSTOM_DATA);
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
        CustomData customData = stackList.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return false;
        CompoundTag data = customData.copyTag();
        if (data.contains("written") && data.contains("lines")) {
            ListTag list = data.getList("lines").orElse(null);
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag lineTag = list.getCompound(i).orElse(null);
                    if (lineTag != null) {
                        Line line = Line.fromTag(lineTag);
                        if (line.matches(item)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
