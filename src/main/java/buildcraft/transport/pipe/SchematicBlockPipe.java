/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.builders.snapshot.SchematicBlockDefault;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.block.BlockPipeHolder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;

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
         this.addWireItems(required);
         return required;
      } else {
         return super.computeRequiredItems(includeContainerContents);
      }
   }

   private void addPluggableItems(@Nonnull List<ItemStack> out) {
      if (this.tileNbt != null) {
         CompoundTag plugTag = BcNbt.getCompound(this.tileNbt, "plugs");

         for (Direction face : Direction.values()) {
            ItemStack stack = pluggableItem(BcNbt.getCompound(plugTag, face.getName()), face);
            if (stack != null && !stack.isEmpty()) {
               out.add(stack);
            }
         }
      }
   }

   @Nullable
   private static ItemStack pluggableItem(CompoundTag entry, Direction face) {
      String plugId = BcNbt.getString(entry, "id", "");
      if (plugId.isEmpty()) {
         return ItemStack.EMPTY;
      }

      Identifier id = Identifier.tryParse(plugId);
      PluggableDefinition def = id == null || PipeApi.pluggableRegistry == null ? null : PipeApi.pluggableRegistry.getDefinition(id);
      if (def == null) {
         return null;
      }

      try {
         PipePluggable plug = def.readFromNbt(null, face, BcNbt.getCompound(entry, "data"));
         return plug == null ? null : plug.getPickStack();
      } catch (RuntimeException e) {
         return null;
      }
   }

   private void addWireItems(@Nonnull List<ItemStack> out) {
      if (this.tileNbt != null) {
         int[] parts = BcNbt.getIntArray(BcNbt.getCompound(this.tileNbt, "wires"), "parts");
         Map<Integer, DyeColor> wires = new HashMap<>();

         for (int i = 0; i + 1 < parts.length; i += 2) {
            if (parts[i] >= 0 && parts[i] < EnumWirePart.VALUES.length) {
               wires.put(parts[i], DyeColor.byId(parts[i + 1]));
            }
         }

         for (DyeColor colour : wires.values()) {
            Item wire = BCTransportItems.WIRE_ITEMS.get(colour);
            if (wire != null) {
               out.add(new ItemStack(wire));
            }
         }
      }
   }

   @Override
   protected void loadPaidTileNbt(BlockEntity tileEntity, CompoundTag newTileNbt, Level level, boolean includeContainerContents) {
      CompoundTag plugTag = BcNbt.getCompound(newTileNbt, "plugs");

      for (Direction face : Direction.values()) {
         if (pluggableItem(BcNbt.getCompound(plugTag, face.getName()), face) == null) {
            plugTag.remove(face.getName());
         }
      }

      super.loadPaidTileNbt(tileEntity, newTileNbt, level, includeContainerContents);
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
