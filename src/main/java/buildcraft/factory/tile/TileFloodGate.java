/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;


import buildcraft.lib.fabric.transfer.fluid.FluidStorageInteractions;
import buildcraft.lib.fluid.display.FluidDisplayNames;
import buildcraft.lib.fluid.identity.FluidIdentity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.block.BlockFloodGate;
import buildcraft.lib.fabric.transfer.fluid.SingleFluidTank;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.BlockDropsUtil;
import buildcraft.lib.misc.BlockUtil;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerPlayer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.authlib.GameProfile;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileFloodGate extends BlockEntity implements IDebuggable {
   private static final Direction[] SEARCH_NORMAL = new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
   private static final Direction[] SEARCH_GASEOUS = new Direction[]{Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
   private static final int[] REBUILD_DELAYS = new int[]{16, 32, 64, 128, 256};
   private static final Identifier ADVANCEMENT_FLOODING_THE_WORLD = Identifier.parse("buildcraftfactory:flooding_the_world");
   public final SingleFluidTank fluidTank = new SingleFluidTank(2000);
   public final EnumSet<Direction> openSides = EnumSet.of(Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
   public final Deque<BlockPos> queue = new ArrayDeque<>();
   private final Map<BlockPos, List<BlockPos>> paths = new HashMap<>();
   private int delayIndex = 0;
   private int tick = 0;
   private int lastSyncedAmount = 0;
   private FluidStack lastSyncedFluid = FluidStack.EMPTY;
   private GameProfile owner;

   public TileFloodGate(BlockPos pos, BlockState state) {
      super(BCFactoryBlockEntities.FLOOD_GATE, pos, state);
   }

   @Nullable
   public Storage<FluidVariant> getSidedFluidStorage(@Nullable Direction direction) {
      return direction == null ? null : this.fluidTank;
   }

   @Nullable
   public GameProfile getOwner() {
      return this.owner;
   }

   public void onPlacedBy(@Nullable LivingEntity placer) {
      if (placer instanceof Player player) {
         this.owner = player.getGameProfile();
         this.setChanged();
      }
   }

   private int getCurrentDelay() {
      return REBUILD_DELAYS[this.delayIndex];
   }

   public void onSidesToggled() {
      this.queue.clear();
      this.delayIndex = 0;
      this.tick = 0;
   }

   private void buildQueue() {
      this.queue.clear();
      this.paths.clear();
      FluidStack fluid = this.fluidTank.getFluidStack();
      if (!fluid.isEmpty()) {
         Set<BlockPos> checked = new HashSet<>();
         checked.add(this.worldPosition);
         List<BlockPos> nextPosesToCheck = new ArrayList<>();

         for (Direction face : this.openSides) {
            BlockPos offset = this.worldPosition.relative(face);
            nextPosesToCheck.add(offset);
            this.paths.put(offset, ImmutableList.of(offset));
         }

         Direction[] directions = FluidVariantAttributes.isLighterThanAir(FluidVariant.of(fluid.getFluid())) ? SEARCH_GASEOUS : SEARCH_NORMAL;

         while (!nextPosesToCheck.isEmpty()) {
            List<BlockPos> nextPosesToCheckCopy = new ArrayList<>(nextPosesToCheck);
            nextPosesToCheck.clear();

            for (BlockPos toCheck : nextPosesToCheckCopy) {
               if (!(toCheck.distSqr(this.worldPosition) > 4096.0) && checked.add(toCheck) && this.canSearch(toCheck)) {
                  if (this.canFill(toCheck)) {
                     this.queue.push(toCheck);
                     if (this.queue.size() >= 4096) {
                        return;
                     }
                  }

                  List<BlockPos> checkPath = this.paths.get(toCheck);

                  for (Direction side : directions) {
                     BlockPos next = toCheck.relative(side);
                     if (!checked.contains(next)) {
                        Builder<BlockPos> pathBuilder = ImmutableList.builder();
                        pathBuilder.addAll(checkPath);
                        pathBuilder.add(next);
                        this.paths.put(next, pathBuilder.build());
                        nextPosesToCheck.add(next);
                     }
                  }
               }
            }
         }
      }
   }

   private boolean canFill(BlockPos offsetPos) {
      if (this.level.isEmptyBlock(offsetPos)) {
         return true;
      }

      Fluid fluid = BlockUtil.getFluidWithFlowing(this.level, offsetPos);
      return fluid != null
         && FluidIdentity.areFluidsEqual(fluid, this.fluidTank.getFluidStack().getFluid())
         && BlockUtil.getFluidWithoutFlowing(this.level.getBlockState(offsetPos)) == null;
   }

   private boolean canSearch(BlockPos offsetPos) {
      if (this.canFill(offsetPos)) {
         return true;
      }

      Fluid fluid = BlockUtil.getFluid(this.level, offsetPos);
      return FluidIdentity.areFluidsEqual(fluid, this.fluidTank.getFluidStack().getFluid());
   }

   private boolean canFillThrough(BlockPos pos) {
      if (this.level.isEmptyBlock(pos)) {
         return false;
      }

      Fluid fluid = BlockUtil.getFluidWithFlowing(this.level, pos);
      return FluidIdentity.areFluidsEqual(fluid, this.fluidTank.getFluidStack().getFluid());
   }

   public void serverTick() {
      if (this.level != null && !this.level.isClientSide()) {
         int currentAmount = this.fluidTank.getAmountMb();
         FluidStack currentFluid = this.fluidTank.getFluidStack();
         FluidStack currentIdentity = currentFluid.isEmpty() ? FluidStack.EMPTY : currentFluid.copyWithAmount(1);
         if (currentAmount != this.lastSyncedAmount || !FluidIdentity.areEquivalentFluidStacks(currentIdentity, this.lastSyncedFluid)) {
            this.lastSyncedAmount = currentAmount;
            this.lastSyncedFluid = currentIdentity;
            this.setChanged();
            if (this.level instanceof ServerLevel level) {
               Packet<?> packet = this.getUpdatePacket();
               if (packet != null) {
                  for (ServerPlayer player : PlayerLookup.tracking(level, this.getBlockPos())) {
                     player.connection.send(packet);
                  }
               }
            }
         }

         if (this.fluidTank.getAmountMb() >= 1000) {
            this.tick++;
            if (this.tick % 16 == 0 && !this.fluidTank.isEmpty() && !this.queue.isEmpty() && this.fluidTank.getAmountMb() >= 1000) {
                  BlockPos currentPos = this.queue.removeLast();
                  List<BlockPos> path = this.paths.get(currentPos);
                  boolean canFill = true;
                  if (path != null) {
                     for (BlockPos p : path) {
                        if (!p.equals(currentPos) && !this.canFillThrough(p)) {
                           canFill = false;
                           break;
                        }
                     }
                  }

                  if (canFill && this.canFill(currentPos)) {
                     ServerLevel serverLevel = (ServerLevel)this.level;
                     Player fakePlayer = BuildCraftAPI.fakePlayerProvider.getFakePlayer(serverLevel, this.owner, this.worldPosition);
                     FluidStack placed = FluidStorageInteractions.tryPlaceFluid(this.fluidTank, fakePlayer, this.level, InteractionHand.MAIN_HAND, currentPos);
                     if (!placed.isEmpty()) {
                        if (this.owner != null) {
                           AdvancementUtil.unlockAdvancement(this.owner.id(), this.level, ADVANCEMENT_FLOODING_THE_WORLD);
                        }

                        this.delayIndex = 0;
                        this.tick = 0;
                     } else {
                        this.buildQueue();
                     }
                  } else {
                     this.buildQueue();
                  }
            }

            if (this.queue.isEmpty() && this.tick >= this.getCurrentDelay()) {
               this.delayIndex = Math.min(this.delayIndex + 1, REBUILD_DELAYS.length - 1);
               this.tick = 0;
               this.buildQueue();
            }
         }
      }
   }

   @Override
   public void preRemoveSideEffects(BlockPos pos, BlockState state) {
      if (this.level != null && !this.level.isClientSide()) {
         this.dropTankContents(pos);
      }

      super.preRemoveSideEffects(pos, state);
   }

   private void dropTankContents(BlockPos pos) {
      if (this.fluidTank.isEmpty()) {
         return;
      }

      FluidStack held = this.fluidTank.getFluidStack();
      int amountMb = this.fluidTank.getAmountMb();
      BlockDropsUtil.dropFluidShard(this.level, pos, held);

      try (Transaction tx = Transaction.openOuter()) {
         this.fluidTank.extractMb(held, amountMb, tx);
         tx.commit();
      }
   }

   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      if (this.owner != null && this.owner.id() != null) {
         output.putString("ownerUUID", this.owner.id().toString());
         if (this.owner.name() != null) {
            output.putString("ownerName", this.owner.name());
         }
      }

      byte sides = 0;

      for (Direction face : Direction.values()) {
         if (this.openSides.contains(face)) {
            sides |= (byte)(1 << face.get3DDataValue());
         }
      }

      output.putByte("openSides", sides);
      this.fluidTank.serialize(output);
   }

   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      String ownerUuid = input.getStringOr("ownerUUID", "");
      if (!ownerUuid.isEmpty()) {
         try {
            this.owner = new GameProfile(UUID.fromString(ownerUuid), input.getStringOr("ownerName", "Unknown"));
         } catch (IllegalArgumentException e) {
            this.owner = null;
         }
      }

      byte sides = input.getByteOr("openSides", (byte)31);
      this.openSides.clear();

      for (Direction face : Direction.values()) {
         if ((sides >> face.get3DDataValue() & 1) == 1) {
            this.openSides.add(face);
         }
      }

      this.fluidTank.deserialize(input);
      this.syncOpenSidesToBlockState();
   }

   public void onLoad() {
      this.syncOpenSidesToBlockState();
   }

   private void syncOpenSidesToBlockState() {
      if (this.level != null && !this.level.isClientSide()) {
         BlockState state = this.getBlockState();
         if (state.getBlock() instanceof BlockFloodGate) {
            BlockState newState = state;

            for (Entry<Direction, Property<Boolean>> entry : BlockFloodGate.CONNECTED_MAP.entrySet()) {
               newState = (BlockState)newState.setValue(entry.getValue(), this.openSides.contains(entry.getKey()));
            }

            if (newState != state) {
               this.level.setBlock(this.worldPosition, newState, 2);
            }

            this.onSidesToggled();
         }
      }
   }

   public CompoundTag getUpdateTag(Provider registries) {
      return this.saveCustomOnly(registries);
   }

   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      left.add("fluid = " + FluidDisplayNames.debugString(this.fluidTank.getFluidStack()));
      left.add("owner = " + (this.owner != null ? this.owner.name() : "none"));
      left.add("openSides = " + this.openSides.stream().map(Enum::name).collect(Collectors.joining(", ")));
      left.add("delay = " + this.getCurrentDelay());
      left.add("tick = " + this.tick);
      left.add("queue size = " + this.queue.size());
      left.add("paths size = " + this.paths.size());
   }

   @Override
   public void getClientDebugInfo(List<String> left, List<String> right, Direction side) {
      BlockState state = this.getBlockState();
      List<String> open = new ArrayList<>();

      for (Entry<Direction, Property<Boolean>> e : BlockFloodGate.CONNECTED_MAP.entrySet()) {
         if ((Boolean)state.getValue(e.getValue())) {
            open.add(e.getKey().name());
         }
      }

      left.add("openSides (state) = " + String.join(", ", open));
   }
}
