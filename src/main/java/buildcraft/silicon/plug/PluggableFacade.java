/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.plug;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.facades.FacadeType;
import buildcraft.api.facades.IFacade;
import buildcraft.api.facades.IFacadePhasedState;
import buildcraft.api.transport.IWireManager;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.net.BcPayloadBuffers;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.client.model.key.KeyPlugFacade;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

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
         ListTag tagStates = BcNbt.getList(nbt, "states");
         if (!tagStates.isEmpty()) {
            boolean isHollow = tagStates.get(0) instanceof CompoundTag ct && BcNbt.getBoolean(ct, "isHollow", false);
            CompoundTag tagFacade = new CompoundTag();
            tagFacade.put("states", tagStates);
            tagFacade.putBoolean("isHollow", isHollow);
            nbt.put("facade", tagFacade);
         }
      }

      this.states = FacadeInstance.readFromNbt(BcNbt.getCompound(nbt, "facade"));
      this.activeState = MathUtil.clamp(BcNbt.getInt(nbt, "activeState", 0), 0, this.states.phasedStates.length - 1);
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
      PacketBufferBC buf = BcPayloadBuffers.ensure(buffer);
      this.states = FacadeInstance.readFromBuffer(buf);
      this.isSideSolid = buf.readBoolean();
   }

   @Override
   public void writeCreationPayload(FriendlyByteBuf buffer) {
      PacketBufferBC buf = BcPayloadBuffers.ensure(buffer);
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

   /**
    * Hollow facades render a frame with an 8px hole for the pipe (the baker cuts 0.25..0.75), but the bounding box
    * is the full face panel -- so the vanilla raytrace stopped on invisible geometry when aiming through the hole,
    * targeting this pipe instead of whatever is visibly behind. Return the true frame-with-hole shape so rays pass
    * through the hole exactly like the visuals suggest.
    */
   @Override
   public VoxelShape getShape() {
      return (this.isHollow() ? HOLLOW_SHAPES : SOLID_SHAPES)[this.side.ordinal()];
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

   @Override
   public boolean needsTick() {
      // A plain facade (every state has a null colour) never switches, so it needn't tick.
      return this.isPhased();
   }

   @Override
   public void onTick() {
      if (this.holder == null || this.holder.getPipeWorld() == null || this.holder.getPipeWorld().isClientSide()) {
         return;
      }

      // Phased facades change appearance based on which coloured pipe wire is powered. activeState was only ever
      // set at construction, so this switching never happened — resolve it each server tick and push a render
      // update (which syncs the new activeState to clients via the pluggable client-update data) when it changes.
      int target = this.resolveActiveState();
      if (target != this.activeState) {
         this.activeState = target;
         this.holder.scheduleRenderUpdate();
      }
   }

   private boolean isPhased() {
      for (FacadePhasedState state : this.states.phasedStates) {
         if (state.activeColour != null) {
            return true;
         }
      }

      return false;
   }

   /**
    * The state to display now: the first colour-keyed state whose wire colour is powered on this pipe, else the
    * default (null-colour) state, else state 0.
    */
   private int resolveActiveState() {
      IWireManager wires = this.holder.getWireManager();
      int defaultState = 0;
      FacadePhasedState[] phased = this.states.phasedStates;

      for (int i = 0; i < phased.length; i++) {
         DyeColor colour = phased[i].activeColour;
         if (colour == null) {
            defaultState = i;
         } else if (wires.isAnyPowered(colour)) {
            return i;
         }
      }

      return defaultState;
   }

   @Override
   public CompoundTag writeClientUpdateData() {
      CompoundTag nbt = new CompoundTag();
      nbt.putInt("activeState", this.activeState);
      return nbt;
   }

   @Override
   public void readClientUpdateData(CompoundTag nbt) {
      this.activeState = MathUtil.clamp(BcNbt.getInt(nbt, "activeState", this.activeState), 0, this.states.phasedStates.length - 1);
   }

   private static final VoxelShape[] SOLID_SHAPES = new VoxelShape[6];
   private static final VoxelShape[] HOLLOW_SHAPES = new VoxelShape[6];

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

      for (Direction dir : Direction.values()) {
         AABB box = BOXES[dir.ordinal()];
         SOLID_SHAPES[dir.ordinal()] = Shapes.create(box);
         HOLLOW_SHAPES[dir.ordinal()] = buildHollowFrame(dir, box);
      }
   }

   /** The full-face panel minus the 8px pipe hole (0.25..0.75 -- the exact cut the hollow baker renders). */
   private static VoxelShape buildHollowFrame(Direction side, AABB box) {
      double h1 = 0.25;
      double h2 = 0.75;
      AABB a;
      AABB b;
      AABB c;
      AABB d;
      switch (side.getAxis()) {
         case Y -> {
            a = new AABB(0.0, box.minY, 0.0, 1.0, box.maxY, h1);
            b = new AABB(0.0, box.minY, h2, 1.0, box.maxY, 1.0);
            c = new AABB(0.0, box.minY, h1, h1, box.maxY, h2);
            d = new AABB(h2, box.minY, h1, 1.0, box.maxY, h2);
         }
         case Z -> {
            a = new AABB(0.0, 0.0, box.minZ, 1.0, h1, box.maxZ);
            b = new AABB(0.0, h2, box.minZ, 1.0, 1.0, box.maxZ);
            c = new AABB(0.0, h1, box.minZ, h1, h2, box.maxZ);
            d = new AABB(h2, h1, box.minZ, 1.0, h2, box.maxZ);
         }
         default -> {
            a = new AABB(box.minX, 0.0, 0.0, box.maxX, h1, 1.0);
            b = new AABB(box.minX, h2, 0.0, box.maxX, 1.0, 1.0);
            c = new AABB(box.minX, h1, 0.0, box.maxX, h2, h1);
            d = new AABB(box.minX, h1, h2, box.maxX, h2, 1.0);
         }
      }

      return Shapes.or(
         Shapes.create(a),
         Shapes.create(b),
         Shapes.create(c),
         Shapes.create(d)
      );
   }
}
