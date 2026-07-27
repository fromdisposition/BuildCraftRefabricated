/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.builders.snapshot.SchematicBlockDefault;
import buildcraft.transport.block.BlockPipeHolder;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;

public class SchematicBlockPipe extends SchematicBlockDefault {
   public static boolean predicate(SchematicBlockContext context) {
      return context.block instanceof BlockPipeHolder && SchematicBlockDefault.predicate(context);
   }

   @Nonnull
   @Override
   public List<ItemStack> computeRequiredItems(boolean includeContainerContents) {
      ItemStack pipeStack = this.resolvePipeItem();
      if (pipeStack != null && !pipeStack.isEmpty()) {
         List<ItemStack> required = new ArrayList<>();
         required.add(pipeStack);
         this.addPluggableItems(required);
         return required;
      } else {
         return super.computeRequiredItems(includeContainerContents);
      }
   }

   private void addPluggableItems(@Nonnull List<ItemStack> out) {
      if (this.tileNbt != null && PipeApi.pluggableRegistry != null) {
         CompoundTag plugTag = BcNbt.getCompound(this.tileNbt, "plugs");
         if (!plugTag.isEmpty()) {
            for (Direction face : Direction.values()) {
               CompoundTag entry = BcNbt.getCompound(plugTag, face.getName());
               String plugId = BcNbt.getString(entry, "id", "");
               if (!plugId.isEmpty()) {
                  PluggableDefinition def = PipeApi.pluggableRegistry.getDefinition(Identifier.parse(plugId));
                  if (def != null) {
                     try {
                        PipePluggable plug = def.readFromNbt(null, face, BcNbt.getCompound(entry, "data"));
                        if (plug != null) {
                           ItemStack stack = plug.getPickStack();
                           if (stack != null && !stack.isEmpty()) {
                              out.add(stack);
                           }
                        }
                     } catch (Throwable var12) {
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public SchematicBlockDefault getRotated(Rotation rotation) {
      SchematicBlockDefault rotated = super.getRotated(rotation);
      if (rotated instanceof SchematicBlockPipe pipe && this.tileNbt != null) {
         pipe.tileNbt = rotatePluggableFaces(this.tileNbt, rotation);
      }

      return rotated;
   }

   @Nonnull
   private static CompoundTag rotatePluggableFaces(@Nonnull CompoundTag original, Rotation rotation) {
      if (rotation != Rotation.NONE && original.contains("plugs")) {
         CompoundTag copy = original.copy();
         CompoundTag oldPlugs = BcNbt.getCompound(copy, "plugs");
         CompoundTag newPlugs = new CompoundTag();

         for (Direction face : Direction.values()) {
            CompoundTag entry = BcNbt.getCompound(oldPlugs, face.getName());
            if (!entry.isEmpty()) {
               newPlugs.put(rotation.rotate(face).getName(), entry);
            }
         }

         copy.put("plugs", newPlugs);
         return copy;
      } else {
         return original;
      }
   }

   @Nullable
   private ItemStack resolvePipeItem() {
      if (this.tileNbt == null) {
         return null;
      } else {
         CompoundTag pipeTag = BcNbt.getCompound(this.tileNbt, "pipe");
         String defId = BcNbt.getString(pipeTag, "def", "");
         if (defId.isEmpty()) {
            return null;
         } else {
            PipeDefinition def = PipeRegistry.INSTANCE.getDefinition(defId);
            if (def == null) {
               return null;
            } else {
               return PipeRegistry.INSTANCE.getItemForPipe(def) instanceof Item item ? new ItemStack(item) : null;
            }
         }
      }
   }
}
