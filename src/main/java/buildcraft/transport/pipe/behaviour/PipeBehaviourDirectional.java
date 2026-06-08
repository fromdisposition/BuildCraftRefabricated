/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventActionActivate;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventStatement;
import buildcraft.api.transport.pipe.PipeFaceTex;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.transport.BCTransportStatements;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;

public abstract class PipeBehaviourDirectional extends PipeBehaviour {
   protected EnumPipePart currentDir = EnumPipePart.CENTER;

   public PipeBehaviourDirectional(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourDirectional(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
      Direction dir = NBTUtilBC.readEnum(nbt.get("currentDir"), Direction.class);
      this.setCurrentDir(dir);
   }

   @Override
   public CompoundTag writeToNbt() {
      CompoundTag nbt = super.writeToNbt();
      if (this.getCurrentDir() != null) {
         nbt.put("currentDir", NBTUtilBC.writeEnum(this.getCurrentDir()));
      }

      return nbt;
   }

   @Override
   public void readFromNbt(CompoundTag nbt) {
      super.readFromNbt(nbt);
      Direction dir = NBTUtilBC.readEnum(nbt.get("currentDir"), Direction.class);
      this.currentDir = EnumPipePart.fromFacing(dir);
   }

   @Override
   public void writePayload(FriendlyByteBuf buffer) {
      super.writePayload(buffer);
      buffer.writeByte(this.currentDir.ordinal());
   }

   @Override
   public void readPayload(FriendlyByteBuf buffer, Object ctx) throws IOException {
      super.readPayload(buffer, ctx);
      int ord = buffer.readUnsignedByte();
      if (ord >= 0 && ord < EnumPipePart.VALUES.length) {
         this.currentDir = EnumPipePart.VALUES[ord];
      }
   }

   @Override
   public boolean onPipeActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
      if (isHoldingWrench(player)) {
         if (part == EnumPipePart.CENTER) {
            return this.advanceFacing();
         }

         if (part.face != this.getCurrentDir() && this.canFaceDirection(part.face)) {
            this.setCurrentDir(part.face);
            return true;
         }
      }

      return false;
   }

   protected static boolean isHoldingWrench(Player player) {
      for (InteractionHand hand : InteractionHand.values()) {
         if (player.getItemInHand(hand).getItem() instanceof IToolWrench) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean hasSimulationWork() {
      return !this.pipe.getHolder().getPipeWorld().isClientSide() && this.getCurrentDir() != null;
   }

   @Override
   public void onTick() {
      if (!this.pipe.getHolder().getPipeWorld().isClientSide()) {
         if (!this.canFaceDirection(this.getCurrentDir()) && !this.advanceFacing()) {
            this.setCurrentDir(null);
         }
      }
   }

   protected abstract boolean canFaceDirection(Direction var1);

   public boolean advanceFacing() {
      Direction current = this.currentDir.face;

      for (int i = 0; i < 6; i++) {
         if (current == null) {
            current = Direction.DOWN;
         } else {
            int nextOrd = (current.ordinal() + 1) % 6;
            current = Direction.values()[nextOrd];
         }

         if (this.canFaceDirection(current)) {
            this.setCurrentDir(current);
            return true;
         }
      }

      return false;
   }

   @Nullable
   protected Direction getCurrentDir() {
      return this.currentDir.face;
   }

   protected void setCurrentDir(Direction setTo) {
      if (this.currentDir.face != setTo) {
         this.currentDir = EnumPipePart.fromFacing(setTo);
         if (this.pipe.getHolder().getPipeWorld() != null && !this.pipe.getHolder().getPipeWorld().isClientSide()) {
            this.pipe.getHolder().scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.BEHAVIOUR);
            this.pipe.getHolder().scheduleRenderUpdate();
         }
      }
   }

   @Override
   public PipeFaceTex getTextureData(@Nullable Direction face) {
      return PipeFaceTex.get(this.getTextureIndex(face));
   }

   @Override
   public int getTextureIndex(@Nullable Direction face) {
      return 0;
   }

   @PipeEventHandler
   public void addInternalActions(PipeEventStatement.AddActionInternal event) {
      for (Direction face : Direction.values()) {
         if (this.canFaceDirection(face)) {
            event.actions.add(BCTransportStatements.ACTION_PIPE_DIRECTION[face.ordinal()]);
         }
      }
   }

   @PipeEventHandler
   public void onActionActivate(PipeEventActionActivate event) {
      for (Direction face : Direction.values()) {
         if (event.action == BCTransportStatements.ACTION_PIPE_DIRECTION[face.ordinal()]) {
            if (this.canFaceDirection(face)) {
               this.setCurrentDir(face);
            }

            return;
         }
      }
   }
}
