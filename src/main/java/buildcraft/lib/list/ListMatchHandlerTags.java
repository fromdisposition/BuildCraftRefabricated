/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import buildcraft.api.lists.ListMatchHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ListMatchHandlerTags extends ListMatchHandler {
   @Override
   public boolean isValidSource(ListMatchHandler.Type type, @Nonnull ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         return type != ListMatchHandler.Type.TYPE && type != ListMatchHandler.Type.MATERIAL ? false : tagsOf(stack).findAny().isPresent();
      }
   }

   @Override
   public boolean matches(ListMatchHandler.Type type, @Nonnull ItemStack source, @Nonnull ItemStack target, boolean precise) {
      if (source.isEmpty() || target.isEmpty()) {
         return false;
      }

      if (type != ListMatchHandler.Type.TYPE && type != ListMatchHandler.Type.MATERIAL) {
         return false;
      }

      Set<String> sourceParts = collectParts(source, type);
      if (sourceParts.isEmpty()) {
         return false;
      }

      Set<String> targetParts = collectParts(target, type);
      return !Collections.disjoint(sourceParts, targetParts);
   }

   @Nullable
   @Override
   public NonNullList<ItemStack> getClientExamples(ListMatchHandler.Type type, @Nonnull ItemStack stack) {
      if (stack.isEmpty()) {
         return null;
      }

      if (type != ListMatchHandler.Type.TYPE && type != ListMatchHandler.Type.MATERIAL) {
         return null;
      }

      Set<String> parts = collectParts(stack, type);
      if (parts.isEmpty()) {
         return null;
      }

      Set<Item> seen = new HashSet<>();
      NonNullList<ItemStack> out = NonNullList.create();
      BuiltInRegistries.ITEM.getTags().forEach(named -> {
         TagKey<Item> tag = named.key();
         String part = partOf(tag, type);
         if (parts.contains(part)) {
            for (Holder<Item> h : named) {
               Item item = (Item)h.value();
               if (seen.add(item)) {
                  out.add(new ItemStack(item));
               }
            }
         }
      });
      return out;
   }

   @Nonnull
   @Override
   public List<String> describeMatch(ListMatchHandler.Type type, @Nonnull ItemStack stack) {
      if (stack.isEmpty()) {
         return List.of();
      }

      if (type != ListMatchHandler.Type.TYPE && type != ListMatchHandler.Type.MATERIAL) {
         return List.of();
      }

      Set<String> out = new LinkedHashSet<>();
      tagsOf(stack).forEach(tag -> {
         String part = partOf((TagKey<Item>)tag, type);
         out.add("#" + tag.location() + " (" + part + ")");
      });
      return new ArrayList<>(out);
   }

   private static Stream<TagKey<Item>> tagsOf(ItemStack stack) {
      return stack.typeHolder().tags();
   }

   private static Set<String> collectParts(ItemStack stack, ListMatchHandler.Type type) {
      List<TagKey<Item>> tags = tagsOf(stack).toList();
      Set<String> parts = new HashSet<>();
      if (type == ListMatchHandler.Type.MATERIAL) {
         boolean hasSlashed = tags.stream().anyMatch(t -> t.location().getPath().indexOf(47) >= 0);

         for (TagKey<Item> tag : tags) {
            boolean slashed = tag.location().getPath().indexOf(47) >= 0;
            if (!hasSlashed || slashed) {
               parts.add(partOf(tag, type));
            }
         }
      } else {
         for (TagKey<Item> tag : tags) {
            parts.add(partOf(tag, type));
         }
      }

      return parts;
   }

   @Nonnull
   private static String partOf(TagKey<Item> tag, ListMatchHandler.Type type) {
      String path = tag.location().getPath();
      int slash = path.indexOf(47);
      if (slash < 0) {
         return path;
      } else if (type == ListMatchHandler.Type.TYPE) {
         return path.substring(0, slash);
      } else {
         return slash == path.length() - 1 ? path : path.substring(slash + 1);
      }
   }
}
