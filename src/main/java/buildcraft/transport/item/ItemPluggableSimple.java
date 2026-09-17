/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.item;

import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.lib.misc.SoundUtil;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class ItemPluggableSimple extends Item implements IItemPluggable {
   private final Supplier<PluggableDefinition> definition;
   private final PluggableDefinition.IPluggableCreator creator;
   private final boolean requiresRedstoneReceiver;
   @Nullable
   private final Function<Direction, AABB> placementBox;

   public ItemPluggableSimple(
      Properties properties,
      Supplier<PluggableDefinition> definition,
      PluggableDefinition.IPluggableCreator creator,
      boolean requiresRedstoneReceiver,
      @Nullable Function<Direction, AABB> placementBox
   ) {
      super(properties);
      this.definition = definition;
      this.creator = creator;
      this.requiresRedstoneReceiver = requiresRedstoneReceiver;
      this.placementBox = placementBox;
   }

   @Nullable
   @Override
   public PipePluggable onPlace(ItemStack stack, IPipeHolder holder, Direction side, Player player, InteractionHand hand) {
      PluggableDefinition def = this.definition.get();
      if (def == null) {
         return null;
      }

      if (this.requiresRedstoneReceiver) {
         IPipe pipe = holder.getPipe();
         if (pipe == null || !(pipe.getBehaviour() instanceof IMjRedstoneReceiver)) {
            return null;
         }
      }

      SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos());
      return this.creator.createSimplePluggable(def, holder, side);
   }

   @Override
   public AABB getPlacementBoundingBox(ItemStack stack, Direction side) {
      return this.placementBox == null ? IItemPluggable.super.getPlacementBoundingBox(stack, side) : this.placementBox.apply(side);
   }
}
