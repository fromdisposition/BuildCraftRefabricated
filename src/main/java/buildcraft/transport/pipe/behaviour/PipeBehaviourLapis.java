package buildcraft.transport.pipe.behaviour;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventActionActivate;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.api.transport.pipe.PipeEventStatement;
import buildcraft.api.transport.pipe.PipeFaceTex;
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

public class PipeBehaviourLapis extends PipeBehaviour {
   private final PipeBehaviourColourData colourData = new PipeBehaviourColourData();

   public PipeBehaviourLapis(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourLapis(IPipe pipe, CompoundTag nbt) {
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
   public PipeFaceTex getTextureData(@Nullable Direction face) {
      return PipeFaceTex.get(this.colourData.getColour().getId());
   }

   @Override
   public boolean onPipeActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
      if (this.pipe.getHolder().getPipeWorld().isClientSide()) {
         return EntityUtil.getWrenchHand(player) != null;
      } else if (EntityUtil.getWrenchHand(player) != null) {
         EntityUtil.activateWrench(player, trace);
         return this.colourData.cycleOnWrench(player, IPipeHolder.PipeMessageReceiver.BEHAVIOUR, this.pipe.getHolder());
      } else {
         return false;
      }
   }

   @PipeEventHandler
   public void onReachCenter(PipeEventItem.ReachCenter reachCenter) {
      reachCenter.colour = this.colourData.getColour();
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
