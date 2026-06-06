package buildcraft.silicon.plug;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.api.facades.IFacadePhasedState;
import buildcraft.api.facades.IFacadeState;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.BcPayloadBuffers;
import buildcraft.lib.net.PacketBufferBC;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FacadePhasedState implements IFacadePhasedState {
   public final FacadeBlockStateInfo stateInfo;
   @Nullable
   public final DyeColor activeColour;

   public FacadePhasedState(FacadeBlockStateInfo stateInfo, @Nullable DyeColor activeColour) {
      this.stateInfo = stateInfo;
      this.activeColour = activeColour;
   }

   public static FacadePhasedState readFromNbt(CompoundTag nbt) {
      FacadeBlockStateInfo stateInfo = FacadeStateManager.defaultState;
      if (nbt.contains("state")) {
         try {
            BlockState blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK, nbt.getCompoundOrEmpty("state"));
            stateInfo = FacadeStateManager.validFacadeStates.get(blockState);
            if (stateInfo == null) {
               stateInfo = FacadeStateManager.defaultState;
            }
         } catch (Throwable t) {
            stateInfo = FacadeStateManager.defaultState;
         }
      }

      DyeColor colour = NBTUtilBC.readEnum(nbt.get("activeColour"), DyeColor.class);
      return new FacadePhasedState(stateInfo, colour);
   }

   public CompoundTag writeToNbt() {
      CompoundTag nbt = new CompoundTag();

      try {
         nbt.put("state", NbtUtils.writeBlockState(this.stateInfo.state));
      } catch (Throwable t) {
         throw new IllegalStateException(
            "Writing facade block state\n\tState = "
               + this.stateInfo
               + "\n\tBlock = "
               + this.stateInfo.state.getBlock()
               + "\n\tBlock Class = "
               + this.stateInfo.state.getBlock().getClass(),
            t
         );
      }

      if (this.activeColour != null) {
         nbt.put("activeColour", NBTUtilBC.writeEnum(this.activeColour));
      }

      return nbt;
   }

   public static FacadePhasedState readFromBuffer(FriendlyByteBuf buf) {
      PacketBufferBC bc = BcPayloadBuffers.ensure(buf);
      int stateId = bc.readVarInt();
      BlockState state = (BlockState)Block.BLOCK_STATE_REGISTRY.byId(stateId);
      boolean hasColour = bc.readBoolean();
      DyeColor colour = hasColour ? bc.readEnumValue(DyeColor.class) : null;
      FacadeBlockStateInfo info = FacadeStateManager.validFacadeStates.get(state);
      if (info == null) {
         info = FacadeStateManager.defaultState;
      }

      return new FacadePhasedState(info, colour);
   }

   public void writeToBuffer(FriendlyByteBuf buf) {
      PacketBufferBC bc = BcPayloadBuffers.ensure(buf);
      try {
         bc.writeVarInt(Block.BLOCK_STATE_REGISTRY.getId(this.stateInfo.state));
      } catch (Throwable t) {
         throw new IllegalStateException("Writing facade block state\n\tState = " + this.stateInfo.state, t);
      }

      bc.writeBoolean(this.activeColour != null);
      if (this.activeColour != null) {
         bc.writeEnumValue(this.activeColour);
      }
   }

   public FacadePhasedState withColour(DyeColor colour) {
      return new FacadePhasedState(this.stateInfo, colour);
   }

   public boolean isSideSolid(Direction side) {
      return this.stateInfo.isSideSolid[side.ordinal()];
   }

   @Override
   public String toString() {
      return (this.activeColour == null ? "" : this.activeColour + " ") + this.getState();
   }

   @Override
   public IFacadeState getState() {
      return this.stateInfo;
   }

   @Override
   public DyeColor getActiveColor() {
      return this.activeColour;
   }
}
