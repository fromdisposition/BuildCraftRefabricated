/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.lib.nbt.BcAuth;
import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeFaceTex;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.tile.BcItemInventory;
import buildcraft.lib.tile.ItemHandlerSimple;
import buildcraft.transport.container.ContainerDiamondPipe;
import buildcraft.transport.container.PipeFilterMenus;
import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public abstract class PipeBehaviourDiamond extends PipeBehaviour {
   public static final int FILTERS_PER_SIDE = 9;
   private static final Identifier ADVANCEMENT_NEED_LIST = Identifier.parse("buildcrafttransport:too_many_pipe_filters");
   public final ItemHandlerSimple filters = new ItemHandlerSimple(54, this::onFilterSlotChange);

   public PipeBehaviourDiamond(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourDiamond(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
      CompoundTag filtersTag = BcNbt.getCompound(nbt, "filters");
      if (!filtersTag.isEmpty()) {
         this.filters.deserializeNBT(filtersTag);
      }
   }

   @Override
   public CompoundTag writeToNbt() {
      CompoundTag nbt = super.writeToNbt();
      nbt.put("filters", this.filters.serializeNBT());
      return nbt;
   }

   @Override
   public void readFromNbt(CompoundTag nbt) {
      super.readFromNbt(nbt);
      this.filters.deserializeNBT(BcNbt.getCompound(nbt, "filters"));
   }

   protected void onFilterSlotChange(BcItemInventory handler, int slot, ItemStack before, ItemStack after) {
      Level level = this.pipe.getHolder().getPipeWorld();
      if (!level.isClientSide()) {
         int baseIndex = 9 * (slot / 9);
         int count = 0;

         for (int i = 0; i < 9; i++) {
            if (!this.filters.getStackInSlot(baseIndex + i).isEmpty()) {
               count++;
            }
         }

         if (count >= 7) {
            GameProfile owner = this.pipe.getHolder().getOwner();
            if (owner != null && BcAuth.id(owner) != null) {
               AdvancementUtil.unlockAdvancement(BcAuth.id(owner), level, ADVANCEMENT_NEED_LIST);
            }
         }
      }
   }

   @Override
   public PipeFaceTex getTextureData(@Nullable Direction face) {
      return PipeFaceTex.get(face == null ? 0 : face.ordinal() + 1);
   }

   @Override
   public boolean onPipeActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
      if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
         PipeFilterMenus.open(
            serverPlayer,
            this.pipe.getHolder(),
            Component.translatable("gui.buildcraft.pipe_diamond.title"),
            ContainerDiamondPipe::new
         );
      }

      return true;
   }
}
