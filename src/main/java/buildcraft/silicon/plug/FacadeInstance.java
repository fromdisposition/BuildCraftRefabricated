/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.plug;

import buildcraft.lib.nbt.BcNbt;
import net.minecraft.network.FriendlyByteBuf;
import buildcraft.api.facades.FacadeType;
import buildcraft.api.facades.IFacade;
import buildcraft.api.facades.IFacadePhasedState;
import buildcraft.lib.net.BcPayloadBuffers;
import buildcraft.lib.net.PacketBufferBC;
import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.DyeColor;

public class FacadeInstance implements IFacade {
   public final FacadePhasedState[] phasedStates;
   public final FacadeType type;
   public final boolean isHollow;

   public FacadeInstance(FacadePhasedState[] phasedStates, boolean isHollow) {
      if (phasedStates == null) {
         throw new NullPointerException("phasedStates");
      }

      if (phasedStates.length == 0) {
         throw new IllegalArgumentException("phasedStates.length was 0");
      }

      if (phasedStates.length > 17) {
         throw new IllegalArgumentException("phasedStates.length was > 17");
      }

      this.phasedStates = phasedStates;
      if (phasedStates.length == 1) {
         this.type = FacadeType.Basic;
      } else {
         this.type = FacadeType.Phased;
      }

      this.isHollow = isHollow;
   }

   public static FacadeInstance createSingle(FacadeBlockStateInfo info, boolean isHollow) {
      return new FacadeInstance(new FacadePhasedState[]{new FacadePhasedState(info, null)}, isHollow);
   }

   public static FacadeInstance readFromNbt(CompoundTag nbt) {
      ListTag list = BcNbt.getList(nbt, "states");
      if (list.isEmpty()) {
         return createSingle(FacadeStateManager.defaultState, false);
      }

      FacadePhasedState[] states = new FacadePhasedState[list.size()];

      for (int i = 0; i < list.size(); i++) {
         CompoundTag compound = list.get(i) instanceof CompoundTag ct ? ct : new CompoundTag();
         states[i] = FacadePhasedState.readFromNbt(compound);
      }

      boolean hollow = BcNbt.getBoolean(nbt, "isHollow", false);
      return new FacadeInstance(states, hollow);
   }

   public CompoundTag writeToNbt() {
      CompoundTag nbt = new CompoundTag();
      ListTag list = new ListTag();

      for (FacadePhasedState state : this.phasedStates) {
         list.add(state.writeToNbt());
      }

      nbt.put("states", list);
      nbt.putBoolean("isHollow", this.isHollow);
      return nbt;
   }

   public static FacadeInstance readFromBuffer(FriendlyByteBuf buf) {
      PacketBufferBC bc = BcPayloadBuffers.ensure(buf);
      boolean isHollow = bc.readBoolean();
      int count = bc.readFixedBits(5);
      // 5 bits decode to 0..31 but the constructor only accepts 1..17; writeToBuffer never emits anything
      // else, so an out-of-range count means a corrupt/hostile packet -- degrade to the default facade
      // instead of letting the constructor's IllegalArgumentException escape the network decode.
      if (count < 1 || count > 17) {
         return createSingle(FacadeStateManager.defaultState, isHollow);
      }

      FacadePhasedState[] states = new FacadePhasedState[count];

      for (int i = 0; i < count; i++) {
         states[i] = FacadePhasedState.readFromBuffer(buf);
      }

      return new FacadeInstance(states, isHollow);
   }

   public void writeToBuffer(FriendlyByteBuf buf) {
      PacketBufferBC bc = BcPayloadBuffers.ensure(buf);
      bc.writeBoolean(this.isHollow);
      bc.writeFixedBits(this.phasedStates.length, 5);

      for (FacadePhasedState phasedState : this.phasedStates) {
         phasedState.writeToBuffer(buf);
      }
   }

   public boolean canAddColour(DyeColor colour) {
      for (FacadePhasedState state : this.phasedStates) {
         if (state.activeColour == colour) {
            return false;
         }
      }

      return true;
   }

   @Nullable
   public FacadeInstance withState(FacadePhasedState state) {
      if (this.canAddColour(state.activeColour)) {
         FacadePhasedState[] newStates = Arrays.copyOf(this.phasedStates, this.phasedStates.length + 1);
         newStates[newStates.length - 1] = state;
         return new FacadeInstance(newStates, this.isHollow);
      } else {
         return null;
      }
   }

   public FacadePhasedState getCurrentStateForStack() {
      int count = this.phasedStates.length;
      if (count == 1) {
         return this.phasedStates[0];
      }

      int now = (int)(System.currentTimeMillis() % 100000L);
      return this.phasedStates[now / 500 % count];
   }

   public FacadeInstance withSwappedIsHollow() {
      return new FacadeInstance(this.phasedStates, !this.isHollow);
   }

   public boolean areAllStatesSolid(Direction side) {
      for (FacadePhasedState state : this.phasedStates) {
         if (!state.isSideSolid(side)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public FacadeType getType() {
      return this.type;
   }

   @Override
   public boolean isHollow() {
      return this.isHollow;
   }

   @Override
   public IFacadePhasedState[] getPhasedStates() {
      return this.phasedStates;
   }
}
