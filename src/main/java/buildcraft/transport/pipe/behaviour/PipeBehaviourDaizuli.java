package buildcraft.transport.pipe.behaviour;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeEventActionActivate;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.api.transport.pipe.PipeEventStatement;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.statements.ActionPipeColor;
import java.io.IOException;
import java.util.Collections;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;

public class PipeBehaviourDaizuli extends PipeBehaviourDirectional {
   private final PipeBehaviourColourData colourData = new PipeBehaviourColourData();

   public PipeBehaviourDaizuli(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourDaizuli(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
      this.colourData.readFromNbt(nbt);
   }

   @Override
   public CompoundTag writeToNbt() {
      CompoundTag nbt = super.writeToNbt();
      this.colourData.writeToNbt(nbt);
      return nbt;
   }

   @Override
   public void readFromNbt(CompoundTag nbt) {
      super.readFromNbt(nbt);
      this.colourData.readFromNbt(nbt);
   }

   @Override
   public void writePayload(FriendlyByteBuf buffer) {
      super.writePayload(buffer);
      this.colourData.writePayload(buffer);
   }

   @Override
   public void readPayload(FriendlyByteBuf buffer, Object ctx) throws IOException {
      super.readPayload(buffer, ctx);
      this.colourData.readPayload(buffer);
   }

   @Override
   public int getTextureIndex(@Nullable Direction face) {
      return face != this.currentDir.face && face != null ? 16 : this.colourData.getColour().getId();
   }

   @Override
   protected boolean canFaceDirection(Direction dir) {
      return true;
   }

   @Override
   public boolean onPipeActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
      if (part != EnumPipePart.CENTER && part != this.currentDir) {
         return super.onPipeActivate(player, trace, hitX, hitY, hitZ, part);
      } else if (this.pipe.getHolder().getPipeWorld().isClientSide()) {
         return EntityUtil.getWrenchHand(player) != null;
      } else if (EntityUtil.getWrenchHand(player) != null) {
         EntityUtil.activateWrench(player, trace);
         return this.colourData.cycleOnWrench(player, IPipeHolder.PipeMessageReceiver.BEHAVIOUR, this.pipe.getHolder());
      } else {
         return false;
      }
   }

   @PipeEventHandler
   public void sideCheck(PipeEventItem.SideCheck sideCheck) {
      if (this.colourData.getColour() == sideCheck.colour) {
         sideCheck.disallowAllExcept(this.currentDir.face);
      } else {
         sideCheck.disallow(this.currentDir.face);
      }
   }

   @PipeEventHandler
   public void addPaintActions(PipeEventStatement.AddActionInternal event) {
      Collections.addAll(event.actions, BCTransportStatements.ACTION_PIPE_COLOUR);
   }

   @PipeEventHandler
   public void onPaintActionActivate(PipeEventActionActivate event) {
      if (event.action instanceof ActionPipeColor action && this.colourData.getColour() != action.color) {
         this.colourData.setColour(action.color);
         this.pipe.getHolder().scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.BEHAVIOUR);
         this.pipe.getHolder().scheduleRenderUpdate();
      }
   }
}
