/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe;

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

import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;

import buildcraft.builders.snapshot.SchematicBlockDefault;

import buildcraft.transport.block.BlockPipeHolder;
import buildcraft.transport.pipe.PipeRegistry;

public class SchematicBlockPipe extends SchematicBlockDefault {

    @SuppressWarnings("unused")
    public static boolean predicate(SchematicBlockContext context) {
        return context.block instanceof BlockPipeHolder
            && SchematicBlockDefault.predicate(context);
    }

    @Nonnull
    @Override
    public List<ItemStack> computeRequiredItems(boolean includeContainerContents) {
        ItemStack pipeStack = resolvePipeItem();
        if (pipeStack == null || pipeStack.isEmpty()) {

            return super.computeRequiredItems(includeContainerContents);
        }

        List<ItemStack> required = new ArrayList<>();
        required.add(pipeStack);
        addPluggableItems(required);
        return required;
    }

    private void addPluggableItems(@Nonnull List<ItemStack> out) {
        if (tileNbt == null || PipeApi.pluggableRegistry == null) {
            return;
        }
        CompoundTag plugTag = tileNbt.getCompoundOrEmpty("plugs");
        if (plugTag.isEmpty()) {
            return;
        }
        for (Direction face : Direction.values()) {
            CompoundTag entry = plugTag.getCompoundOrEmpty(face.getName());
            String plugId = entry.getStringOr("id", "");
            if (plugId.isEmpty()) {
                continue;
            }
            PluggableDefinition def = PipeApi.pluggableRegistry.getDefinition(Identifier.parse(plugId));
            if (def == null) {
                continue;
            }
            try {
                PipePluggable plug = def.readFromNbt(null, face, entry.getCompoundOrEmpty("data"));
                if (plug != null) {
                    ItemStack stack = plug.getPickStack();
                    if (stack != null && !stack.isEmpty()) {
                        out.add(stack);
                    }
                }
            } catch (Throwable t) {

            }
        }
    }

    @Override
    public SchematicBlockDefault getRotated(Rotation rotation) {
        SchematicBlockDefault rotated = super.getRotated(rotation);

        if (rotated instanceof SchematicBlockPipe pipe && tileNbt != null) {
            pipe.tileNbt = rotatePluggableFaces(tileNbt, rotation);
        }
        return rotated;
    }

    @Nonnull
    private static CompoundTag rotatePluggableFaces(@Nonnull CompoundTag original, Rotation rotation) {
        if (rotation == Rotation.NONE || !original.contains("plugs")) {
            return original;
        }
        CompoundTag copy = original.copy();
        CompoundTag oldPlugs = copy.getCompoundOrEmpty("plugs");
        CompoundTag newPlugs = new CompoundTag();
        for (Direction face : Direction.values()) {
            CompoundTag entry = oldPlugs.getCompoundOrEmpty(face.getName());
            if (!entry.isEmpty()) {
                newPlugs.put(rotation.rotate(face).getName(), entry);
            }
        }
        copy.put("plugs", newPlugs);
        return copy;
    }

    @Nullable
    private ItemStack resolvePipeItem() {
        if (tileNbt == null) {
            return null;
        }
        CompoundTag pipeTag = tileNbt.getCompoundOrEmpty("pipe");
        String defId = pipeTag.getStringOr("def", "");
        if (defId.isEmpty()) {
            return null;
        }
        PipeDefinition def = PipeRegistry.INSTANCE.getDefinition(defId);
        if (def == null) {
            return null;
        }
        IItemPipe itemPipe = PipeRegistry.INSTANCE.getItemForPipe(def);
        if (!(itemPipe instanceof Item item)) {
            return null;
        }
        return new ItemStack(item);
    }
}
