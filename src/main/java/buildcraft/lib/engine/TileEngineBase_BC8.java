/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.engine;

import buildcraft.lib.nbt.BcAuth;
import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjToRfAutoConvertor;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.fabric.transfer.BcTransfers;
import net.fabricmc.loader.api.FabricLoader;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.data.ModelVariableData;
import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import buildcraft.lib.nbt.BcProfiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import buildcraft.lib.nbt.BcValueIn;
import buildcraft.lib.nbt.BcValueOut;
//? if >= 1.21.10 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?}
import team.reborn.energy.api.EnergyStorage;

public abstract class TileEngineBase_BC8 extends BlockEntity implements IDebuggable {
   public static final Identifier ADVANCEMENT_TO_MUCH_POWER = Identifier.parse("buildcraftenergy:to_much_power");
   public static final float MIN_HEAT = 20.0F;
   public static final float MAX_HEAT = 250.0F;
   @Nullable
   private GameProfile owner;
   protected Direction orientation = Direction.UP;
   protected long power = 0L;
   public long currentOutput = 0L;
   protected float heat = 20.0F;
   protected float progress = 0.0F;
   protected int progressPart = 0;
   protected boolean isPumping = false;
   protected boolean isRedstonePowered = false;
   private float lastProgress = 0.0F;
   private float clientProgress = 0.0F;
   private boolean clientIsPumping = false;
   Direction prevOrientation = Direction.UP;
   boolean prevIsPumping = false;
   EnumPowerStage prevPowerStage = EnumPowerStage.BLUE;
   protected int orientationChecksRemaining = 1;
   protected boolean checkRedstonePower = true;
   protected int redstonePollTimer = 0;
   private EnumPowerStage powerStage = EnumPowerStage.BLUE;
   public final ModelVariableData clientModelData = new ModelVariableData();
   private IMjConnector mjConnector;

   public long getPower() {
      return this.power;
   }

   public TileEngineBase_BC8(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   public abstract boolean isBurning();

   protected abstract void engineUpdate();

   public abstract long getMaxPower();

   public abstract long minPowerReceived();

   public abstract long maxPowerReceived();

   public abstract long maxPowerExtracted();

   public abstract long getCurrentOutput();

   public abstract float explosionRange();

   @Nonnull
   protected abstract IMjConnector createConnector();

   public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
      if (placer instanceof Player player) {
         this.owner = player.getGameProfile();
      }

      if (this.getBlockState().hasProperty(BuildCraftProperties.BLOCK_FACING_6)) {
         this.setOrientation((Direction)this.getBlockState().getValue(BuildCraftProperties.BLOCK_FACING_6));
      }
   }

   @Nullable
   public GameProfile getOwner() {
      return this.owner;
   }

   public void setOwner(@Nullable GameProfile owner) {
      this.owner = owner;
   }

   protected int getMaxChainLength() {
      return 2;
   }

   public double getPistonSpeed() {
      switch (this.getPowerStage()) {
         case BLUE:
            return 0.02;
         case GREEN:
            return 0.04;
         case YELLOW:
            return 0.08;
         case RED:
            return 0.12;
         default:
            return 0.0;
      }
   }

   public void updateHeatLevel() {
      this.heat = (float)(230.0 * this.getEnergyLevel() + 20.0);
   }

   protected EnumPowerStage computePowerStage() {
      float heatLevel = this.getHeatLevel();
      if (heatLevel < 0.25F) {
         return EnumPowerStage.BLUE;
      } else if (heatLevel < 0.5F) {
         return EnumPowerStage.GREEN;
      } else if (heatLevel < 0.75F) {
         return EnumPowerStage.YELLOW;
      } else {
         return heatLevel < 0.85F ? EnumPowerStage.RED : EnumPowerStage.OVERHEAT;
      }
   }

   public float getHeat() {
      return this.heat;
   }

   public float getHeatLevel() {
      return (this.heat - 20.0F) / 230.0F;
   }

   public double getEnergyLevel() {
      long max = this.getMaxPower();
      return max <= 0L ? 0.0 : (double)this.power / max;
   }

   public final EnumPowerStage getPowerStage() {
      if (this.level != null && !this.level.isClientSide()) {
         if (this.powerStage == EnumPowerStage.OVERHEAT) {
            return this.powerStage;
         }

         EnumPowerStage newStage = this.computePowerStage();
         if (this.powerStage != newStage) {
            this.powerStage = newStage;
            if (this.powerStage == EnumPowerStage.OVERHEAT) {
               this.overheat();
            }

            this.setChanged();
         }
      }

      return this.powerStage;
   }

   protected void overheat() {
      this.isPumping = false;
      if (BCLibConfig.canEnginesExplode.get()) {
         float range = this.explosionRange();
         if (range > 0.0F && this.level != null) {
            this.level
               .explode(
                  null, this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 0.5, this.getBlockPos().getZ() + 0.5, range, ExplosionInteraction.BLOCK
               );
            this.level.removeBlock(this.getBlockPos(), false);
         }
      }
   }

   public boolean clearOverheat(@Nullable Player player) {
      if (this.powerStage != EnumPowerStage.OVERHEAT) {
         return false;
      }

      this.heat = 20.0F;
      this.powerStage = this.computePowerStage();
      this.isPumping = false;
      this.setChanged();
      if (this.level != null && !this.level.isClientSide()) {
         this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
         if (player != null) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_TO_MUCH_POWER);
         }
      }

      return true;
   }

   public boolean hasAlternateReceiver() {
      for (Direction d : Direction.values()) {
         if (d != this.orientation && this.getReceiverToPower(d) != null) {
            return true;
         }
      }

      return false;
   }

   public IMjConnector getMjConnector() {
      if (this.mjConnector == null) {
         this.mjConnector = this.createConnector();
      }

      return this.mjConnector;
   }

   public long extractPower(long min, long max, boolean doExtract) {
      if (this.power < min) {
         return 0L;
      }

      long actualMax = Math.min(max, this.maxPowerExtracted());
      if (actualMax < min) {
         return 0L;
      }

      long extracted = Math.min(this.power, actualMax);
      if (doExtract) {
         this.power -= extracted;
      }

      return extracted;
   }

   // FabricLoader.isModLoaded is a map lookup, and the old code ran it per engine per tick.
   private static final boolean TR_ENERGY_LOADED = FabricLoader.getInstance().isModLoaded("team_reborn_energy");
   // Identity-cache of the RF bridge: the old code allocated a new MjToRfAutoConvertor wrapper every tick for
   // an engine facing an RF machine.
   private Object lastFeStorage;
   private IMjReceiver lastFeReceiver;

   @Nullable
   public IMjReceiver getReceiverToPower(Direction side) {
      if (this.level == null) {
         return null;
      }

      BlockPos pos = this.getBlockPos();

      for (int len = 0; len <= this.getMaxChainLength(); len++) {
         BlockPos targetPos = pos.relative(side);
         BlockEntity tile = BcTransfers.cachedBlockEntity(this.level, targetPos);
         if (tile == null) {
            return null;
         }

         if (tile.getClass() != this.getClass()) {
            IMjReceiver receiver = BcTransfers.mjReceiver(this.level, targetPos, side.getOpposite());
            if (receiver != null && receiver.canConnect(this.getMjConnector()) && this.getMjConnector().canConnect(receiver)) {
               return receiver;
            }

            if (TR_ENERGY_LOADED) {
               Object feStorage = BcTransfers.energy(this.level, targetPos, side.getOpposite());
               if (feStorage != null) {
                  IMjReceiver feReceiver;
                  if (feStorage == this.lastFeStorage && this.lastFeReceiver != null) {
                     feReceiver = this.lastFeReceiver;
                  } else {
                     feReceiver = MjToRfAutoConvertor.createReceiver((team.reborn.energy.api.EnergyStorage) feStorage);
                     this.lastFeStorage = feStorage;
                     this.lastFeReceiver = feReceiver;
                  }

                  if (feReceiver != null && feReceiver.canConnect(this.getMjConnector())) {
                     return feReceiver;
                  }
               }
            }

            return null;
         }

         if (((TileEngineBase_BC8)tile).orientation != side) {
            return null;
         }

         pos = targetPos;
      }

      return null;
   }

   protected void sendPower(@Nullable IMjReceiver receiver) {
      if (receiver == null) {
         this.currentOutput = 0L;
      } else {
         long requested = receiver.getPowerRequested();
         long extracted = this.extractPower(0L, requested, false);
         if (extracted > 0L) {
            long excess = receiver.receivePower(extracted, false);
            long actualSent = extracted - excess;
            this.extractPower(actualSent, actualSent, true);
            this.currentOutput = actualSent;
         } else {
            this.currentOutput = 0L;
         }
      }
   }

   public static <T extends TileEngineBase_BC8> void serverTick(Level level, BlockPos pos, BlockState state, T engine) {
      ProfilerFiller _profiler = BcProfiler.get();
      _profiler.push("buildcraft:engine_serverTick");

      try {
         engine.redstonePollTimer++;
         if (engine.redstonePollTimer >= 10) {
            engine.redstonePollTimer = 0;
            engine.checkRedstonePower = true;
         }

         if (engine.checkRedstonePower) {
            engine.checkRedstoneLevel();
         }

         if (engine.orientationChecksRemaining > 0) {
            engine.orientationChecksRemaining--;
            if (engine.getReceiverToPower(engine.orientation) == null) {
               if (engine.attemptRotation()) {
                  engine.orientationChecksRemaining = 0;
                  level.setBlock(pos, (BlockState)state.setValue(BuildCraftProperties.BLOCK_FACING_6, engine.orientation), 3);
                  level.sendBlockUpdated(pos, state, state, 3);
               }
            } else {
               engine.orientationChecksRemaining = 0;
            }
         }

         engine.updateHeatLevel();
         engine.getPowerStage();
         if (engine.getPowerStage() != EnumPowerStage.OVERHEAT) {
            if (!engine.isRedstonePowered) {
               if (engine.power > MjAPI.MJ) {
                  engine.power = engine.power - MjAPI.MJ;
               } else if (engine.power > 0L) {
                  engine.power = 0L;
               }
            }

            engine.engineUpdate();
            IMjReceiver receiver = engine.getReceiverToPower(engine.orientation);
            boolean pulsedPower = receiver instanceof IMjRedstoneReceiver;
            if (engine.progressPart != 0) {
               engine.progress = engine.progress + (float)engine.getPistonSpeed();
               if (engine.progress > 0.5F && engine.progressPart == 1) {
                  engine.progressPart = 2;
                  if (pulsedPower) {
                     engine.sendPower(receiver);
                  }
               } else if (engine.progress >= 1.0F) {
                  engine.progress = 0.0F;
                  engine.progressPart = 0;
               }
            } else if (engine.isRedstonePowered && (engine.isBurning() || engine.power > 0L) && receiver != null) {
               long requested = receiver.getPowerRequested();
               if (requested > 0L && engine.extractPower(0L, requested, false) > 0L) {
                  engine.progressPart = 1;
                  engine.setPumping(true);
               } else {
                  engine.setPumping(false);
               }
            } else {
               engine.setPumping(false);
            }

            if (!pulsedPower) {
               // Deliver buffered power whenever the engine is on, even if it is not currently producing (out of
               // fuel / FE): already-generated MJ must always flow onward to a consumer instead of sitting frozen
               // in the buffer. Otherwise an FE engine on a slow feed converts a full buffer, runs its fuel to 0,
               // and then starves the network while holding a full charge until fuel returns.
               if (engine.isRedstonePowered && (engine.isBurning() || engine.power > 0L)) {
                  engine.sendPower(receiver);
               } else {
                  engine.currentOutput = 0L;
               }
            }

            boolean needsSync = false;
            if (engine.orientation != engine.prevOrientation) {
               engine.prevOrientation = engine.orientation;
               needsSync = true;
            }

            if (engine.isPumping != engine.prevIsPumping) {
               engine.prevIsPumping = engine.isPumping;
               needsSync = true;
            }

            if (engine.getPowerStage() != engine.prevPowerStage) {
               engine.prevPowerStage = engine.getPowerStage();
               needsSync = true;
            }

            if (needsSync) {
               engine.setChanged();
               level.sendBlockUpdated(pos, state, state, 3);
            } else if (engine.isBurning() && level.getGameTime() % 100L == 0L) {
               // The old unconditional per-tick setChanged kept every engine chunk permanently dirty. Visible
               // state changes mark + sync above; a slow keepalive while burning covers autosave of the
               // gradually moving power/heat without per-tick chunk dirtying.
               engine.setChanged();
            }

            return;
         }

         engine.power = Math.max(engine.power - 10L, 0L);
      } finally {
         _profiler.pop();
      }
   }

   public void checkRedstoneLevel() {
      this.checkRedstonePower = false;
      if (this.level != null) {
         this.isRedstonePowered = this.level.hasNeighborSignal(this.getBlockPos());
      }
   }

   public void onNeighborUpdate() {
      this.checkRedstonePower = true;
      this.orientationChecksRemaining = 5;
   }

   protected final void setPumping(boolean active) {
      if (this.isPumping != active) {
         this.isPumping = active;
         this.setChanged();
      }
   }

   public boolean isPumping() {
      return this.isPumping;
   }

   public Direction getOrientation() {
      return this.orientation;
   }

   public void setOrientation(Direction dir) {
      this.orientation = dir;
      this.orientationChecksRemaining = 1;
      this.setChanged();
   }

   public void rotateOrientation() {
      int next = (this.orientation.ordinal() + 1) % 6;
      this.setOrientation(Direction.values()[next]);
   }

   public boolean attemptRotation() {
      Direction current = this.orientation;
      Direction[] dirs = Direction.values();

      for (int i = 0; i < 6; i++) {
         current = dirs[(current.ordinal() + 1) % 6];
         if (this.isFacingReceiver(current)) {
            if (current != this.orientation) {
               this.setOrientation(current);
               return true;
            }

            return false;
         }
      }

      return false;
   }

   private boolean isFacingReceiver(Direction dir) {
      return this.getReceiverToPower(dir) != null;
   }

   public void clientTick() {
      this.lastProgress = this.clientProgress;
      this.clientIsPumping = this.isPumping;
      if (this.clientIsPumping) {
         this.clientProgress = this.clientProgress + (float)this.getPistonSpeed();
         if (this.clientProgress >= 1.0F) {
            this.clientProgress = 0.0F;
         }
      } else if (this.clientProgress > 0.0F) {
         this.clientProgress -= 0.02F;
         if (this.clientProgress < 0.0F) {
            this.clientProgress = 0.0F;
         }
      }
   }

   public float getProgressClient(float partialTicks) {
      if (this.lastProgress > 0.8F && this.clientProgress < 0.2F) {
         float interp = this.lastProgress + (1.0F + this.clientProgress - this.lastProgress) * partialTicks;
         return interp >= 1.0F ? interp - 1.0F : interp;
      } else {
         return this.lastProgress + (this.clientProgress - this.lastProgress) * partialTicks;
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
      left.add("facing = " + this.orientation);
      left.add("heat = " + LocaleUtil.localizeHeat(this.heat) + " -- " + String.format("%.2f %%", this.getHeatLevel() * 100.0F));
      left.add("power = " + LocaleUtil.localizeMj(this.power));
      left.add("stage = " + this.getPowerStage());
      left.add("progress = " + this.progress);
      left.add("last = " + LocaleUtil.localizeMjFlow(this.currentOutput));
   }

   @Override
   public void getClientDebugInfo(List<String> left, List<String> right, Direction side) {
      left.add("Current Model Variables:");
      this.clientModelData.addDebugInfo(left);
   }

   //? if >= 1.21.10 {
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      this.writeData(new BcValueOut(output));
   }

   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.readData(new BcValueIn(input));
   }
   //?} else {
   /*protected void saveAdditional(CompoundTag tag, Provider registries) {
      super.saveAdditional(tag, registries);
      this.writeData(new BcValueOut(tag, registries));
   }

   protected void loadAdditional(CompoundTag tag, Provider registries) {
      super.loadAdditional(tag, registries);
      this.readData(new BcValueIn(tag, registries));
   }
   *///?}

   protected void writeData(BcValueOut output) {
      output.putByte("orientation", (byte)this.orientation.ordinal());
      output.putLong("power", this.power);
      output.putFloat("heat", this.heat);
      output.putFloat("progress", this.progress);
      output.putBoolean("isPumping", this.isPumping);
      output.putBoolean("isRedstonePowered", this.isRedstonePowered);
      output.putByte("powerStage", (byte)this.powerStage.ordinal());
      if (this.owner != null && BcAuth.id(this.owner) != null) {
         output.putString("ownerUUID", BcAuth.id(this.owner).toString());
         if (BcAuth.name(this.owner) != null) {
            output.putString("ownerName", BcAuth.name(this.owner));
         }
      }
   }

   protected void readData(BcValueIn input) {
      int ord = input.getByteOr("orientation", (byte)Direction.UP.ordinal());
      this.orientation = Direction.values()[Math.min(ord, 5)];
      this.power = input.getLongOr("power", 0L);
      this.heat = input.getFloatOr("heat", 20.0F);
      this.progress = input.getFloatOr("progress", 0.0F);
      this.isPumping = input.getBooleanOr("isPumping", false);
      this.isRedstonePowered = input.getBooleanOr("isRedstonePowered", false);
      int ps = input.getByteOr("powerStage", (byte)0);
      this.powerStage = EnumPowerStage.VALUES[Math.min(ps, EnumPowerStage.VALUES.length - 1)];
      String uuidStr = input.getStringOr("ownerUUID", "");
      if (!uuidStr.isEmpty()) {
         try {
            UUID uuid = UUID.fromString(uuidStr);
            String name = input.getStringOr("ownerName", "Unknown");
            this.owner = new GameProfile(uuid, name);
         } catch (IllegalArgumentException e) {
            this.owner = null;
         }
      }
   }
}
