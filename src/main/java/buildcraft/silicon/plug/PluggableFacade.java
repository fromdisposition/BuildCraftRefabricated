package buildcraft.silicon.plug;

import buildcraft.api.facades.FacadeType;
import buildcraft.api.facades.IFacade;
import buildcraft.api.facades.IFacadePhasedState;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.client.model.key.KeyPlugFacade;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.AABB;

public class PluggableFacade extends PipePluggable implements IFacade {
   private static final AABB[] BOXES = new AABB[6];
   public static final int SIZE = 2;
   public final FacadeInstance states;
   public final boolean isSideSolid;
   public int activeState;

   public PluggableFacade(PluggableDefinition definition, IPipeHolder holder, Direction side, FacadeInstance states) {
      super(definition, holder, side);
      this.states = states;
      this.isSideSolid = states.areAllStatesSolid(side);
   }

   public PluggableFacade(PluggableDefinition def, IPipeHolder holder, Direction side, CompoundTag nbt) {
      super(def, holder, side);
      if (nbt.contains("states") && !nbt.contains("facade")) {
         ListTag tagStates = nbt.getListOrEmpty("states");
         if (!tagStates.isEmpty()) {
            boolean isHollow = tagStates.get(0) instanceof CompoundTag ct && ct.getBooleanOr("isHollow", false);
            CompoundTag tagFacade = new CompoundTag();
            tagFacade.put("states", tagStates);
            tagFacade.putBoolean("isHollow", isHollow);
            nbt.put("facade", tagFacade);
         }
      }

      this.states = FacadeInstance.readFromNbt(nbt.getCompoundOrEmpty("facade"));
      this.activeState = MathUtil.clamp(nbt.getIntOr("activeState", 0), 0, this.states.phasedStates.length - 1);
      this.isSideSolid = this.states.areAllStatesSolid(side);
   }

   @Override
   public CompoundTag writeToNbt() {
      CompoundTag nbt = super.writeToNbt();
      nbt.put("facade", this.states.writeToNbt());
      nbt.putInt("activeState", this.activeState);
      return nbt;
   }

   public PluggableFacade(PluggableDefinition def, IPipeHolder holder, Direction side, FriendlyByteBuf buffer) {
      super(def, holder, side);
      PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
      this.states = FacadeInstance.readFromBuffer(buf);
      this.isSideSolid = buf.readBoolean();
   }

   @Override
   public void writeCreationPayload(FriendlyByteBuf buffer) {
      PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
      this.states.writeToBuffer(buf);
      buf.writeBoolean(this.isSideSolid);
   }

   public static AABB boundingBoxFor(Direction side) {
      return BOXES[side.ordinal()];
   }

   @Override
   public AABB getBoundingBox() {
      return boundingBoxFor(this.side);
   }

   @Override
   public boolean isBlocking() {
      return !this.isHollow();
   }

   @Override
   public boolean canBeConnected() {
      return !this.isHollow();
   }

   @Override
   public boolean isSideSolid() {
      return this.isSideSolid;
   }

   @Override
   public float getExplosionResistance(@Nullable Entity exploder, Explosion explosion) {
      return this.states.phasedStates[this.activeState].stateInfo.state.getBlock().getExplosionResistance();
   }

   @Override
   public ItemStack getPickStack() {
      return BCSiliconItems.PLUG_FACADE.createItemStack(this.states);
   }

   @Override
   public PluggableModelKey getModelRenderKey(Object layer) {
      FacadePhasedState state = this.states.phasedStates[this.activeState];
      return new KeyPlugFacade(layer, this.side, state.stateInfo.state, this.states.isHollow());
   }

   @Override
   public FacadeType getType() {
      return this.states.getType();
   }

   @Override
   public boolean isHollow() {
      return this.states.isHollow();
   }

   @Override
   public IFacadePhasedState[] getPhasedStates() {
      return this.states.getPhasedStates();
   }

   static {
      double ll = 0.0;
      double lu = 0.125;
      double ul = 0.875;
      double uu = 1.0;
      double min = 0.0;
      double max = 1.0;
      BOXES[Direction.DOWN.ordinal()] = new AABB(min, ll, min, max, lu, max);
      BOXES[Direction.UP.ordinal()] = new AABB(min, ul, min, max, uu, max);
      BOXES[Direction.NORTH.ordinal()] = new AABB(min, min, ll, max, max, lu);
      BOXES[Direction.SOUTH.ordinal()] = new AABB(min, min, ul, max, max, uu);
      BOXES[Direction.WEST.ordinal()] = new AABB(ll, min, min, lu, max, max);
      BOXES[Direction.EAST.ordinal()] = new AABB(ul, min, min, uu, max, max);
   }
}
