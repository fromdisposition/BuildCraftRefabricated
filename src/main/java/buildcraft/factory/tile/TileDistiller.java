/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;


import buildcraft.lib.nbt.BcAuth;
import buildcraft.lib.fluid.display.FluidDisplayNames;
import buildcraft.lib.fluid.identity.FluidIdentity;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.factory.BCFactoryAttachments;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.FactoryFluidContainers;
import buildcraft.factory.block.BlockDistiller;
import buildcraft.factory.container.ContainerDistiller;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.fabric.transfer.fluid.SidedFluidStorages;
import buildcraft.lib.fabric.transfer.fluid.SingleFluidTank;
import buildcraft.lib.fluid.registry.FluidSmoother;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.BlockDropsUtil;
import buildcraft.lib.misc.LocaleUtil;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerLevel;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.tile.ItemHandlerSimple;
import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import buildcraft.lib.nbt.BcValueIn;
import buildcraft.lib.nbt.BcValueOut;
//? if >= 1.21.10 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?}

public class TileDistiller extends BlockEntity implements MenuProvider, BlockEntityExtendedMenu, IDebuggable {
   public static final long MAX_MJ_PER_TICK = 6L * MjAPI.MJ;
   private static final Identifier ADVANCEMENT_HEATING_AND_DISTILLING = Identifier.parse("buildcraftfactory:heating_and_distilling");
   private static final Identifier ADVANCEMENT_REFINE_AND_REDEFINE = Identifier.parse("buildcraftenergy:refine_and_redefine");
   public final SingleFluidTank tankIn = new SingleFluidTank(4000, SingleFluidTank.TankAccess.filteredInput(this::isDistillableFluid));
   public final SingleFluidTank tankGasOut = new SingleFluidTank(4000, SingleFluidTank.TankAccess.MACHINE_OUTPUT);
   public final SingleFluidTank tankLiquidOut = new SingleFluidTank(4000, SingleFluidTank.TankAccess.MACHINE_OUTPUT);
   private final MjBattery mjBattery = new MjBattery(1024L * MjAPI.MJ);
   private final IMjReceiver mjReceiver = new MjBatteryReceiver(this.mjBattery);
   public final ItemHandlerSimple containerSlots = new ItemHandlerSimple(3, 1);
   private final FluidSmoother smoothIn;
   private final FluidSmoother smoothGasOut;
   private final FluidSmoother smoothLiquidOut;
   private IRefineryRecipeManager.IDistillationRecipe currentRecipe;
   private long distillPower;
   private boolean isActive;
   private boolean isStuck;
   private GameProfile owner;
   private boolean wasDistillingForAdvancement;
   private long powerAvgSmoothed;
   private long powerAvgClient;
   private double animState;
   private double prevAnimState;
   private double powerAvgVisual;
   private int lastSyncedIn;
   private int lastSyncedGas;
   private int lastSyncedLiquid;
   private FluidStack lastSyncedInFluid;
   private FluidStack lastSyncedGasFluid;
   private FluidStack lastSyncedLiquidFluid;
   private boolean lastSyncedActive;
   private boolean lastSyncedStuck;
   private long lastSyncedPower;

   public TileDistiller(BlockPos pos, BlockState state) {
      super(BCFactoryBlockEntities.DISTILLER, pos, state);
      this.containerSlots.setCallback((handler, slot, bef, aft) -> this.setChanged());
      this.smoothIn = new FluidSmoother(this.tankIn::getAmountMb, this.tankIn::getFluidStack, this.tankIn::getCapacityMb);
      this.smoothGasOut = new FluidSmoother(this.tankGasOut::getAmountMb, this.tankGasOut::getFluidStack, this.tankGasOut::getCapacityMb);
      this.smoothLiquidOut = new FluidSmoother(this.tankLiquidOut::getAmountMb, this.tankLiquidOut::getFluidStack, this.tankLiquidOut::getCapacityMb);
      this.distillPower = 0L;
      this.isActive = false;
      this.isStuck = false;
      this.wasDistillingForAdvancement = false;
      this.powerAvgSmoothed = 0L;
      this.powerAvgClient = 0L;
      this.animState = 0.0;
      this.prevAnimState = 0.0;
      this.powerAvgVisual = 0.0;
      this.lastSyncedIn = -1;
      this.lastSyncedGas = -1;
      this.lastSyncedLiquid = -1;
      this.lastSyncedInFluid = FluidStack.EMPTY;
      this.lastSyncedGasFluid = FluidStack.EMPTY;
      this.lastSyncedLiquidFluid = FluidStack.EMPTY;
      this.lastSyncedActive = false;
      this.lastSyncedStuck = false;
      this.lastSyncedPower = -1L;
   }

   public SingleFluidTank getTankIn() {
      return this.tankIn;
   }

   public SingleFluidTank getTankGasOut() {
      return this.tankGasOut;
   }

   public SingleFluidTank getTankLiquidOut() {
      return this.tankLiquidOut;
   }

   public IMjReceiver getMjReceiver() {
      return this.mjReceiver;
   }

   public MjBattery getBattery() {
      return this.mjBattery;
   }


   @Nullable
   public GameProfile getOwner() {
      return this.owner;
   }

   public void onPlacedBy(@Nullable LivingEntity placer) {
      if (placer instanceof Player player) {
         this.owner = player.getGameProfile();
         this.setChanged();
         if (this.level != null && !this.level.isClientSide()) {
            if (this.level instanceof ServerLevel level) {
               Packet<?> packet = this.getUpdatePacket();
               if (packet != null) {
                  for (ServerPlayer trackingPlayer : PlayerLookup.tracking(level, this.getBlockPos())) {
                     trackingPlayer.connection.send(packet);
                  }
               }
            }
         }
      }
   }

   @Nullable
   public Storage<FluidVariant> getSidedFluidStorage(@Nullable Direction direction) {
      if (direction == null) {
         return null;
      } else if (direction == Direction.UP) {
         return SidedFluidStorages.extractOnly(this.tankGasOut);
      } else {
         return direction == Direction.DOWN ? SidedFluidStorages.extractOnly(this.tankLiquidOut) : SidedFluidStorages.insertOnly(this.tankIn);
      }
   }

   public FluidSmoother getSmoothIn() {
      return this.smoothIn;
   }

   public FluidSmoother getSmoothGasOut() {
      return this.smoothGasOut;
   }

   public FluidSmoother getSmoothLiquidOut() {
      return this.smoothLiquidOut;
   }

   public boolean isActive() {
      return this.isActive;
   }

   public boolean isStuck() {
      return this.isStuck;
   }

   public long getPowerAvgClient() {
      return this.powerAvgClient;
   }

   public double getPowerAvgVisual() {
      return this.powerAvgVisual;
   }

   public double getAnimState() {
      return this.animState;
   }

   public double getPrevAnimState() {
      return this.prevAnimState;
   }

   public void clientTick() {
      this.smoothIn.tick();
      this.smoothGasOut.tick();
      this.smoothLiquidOut.tick();
      this.prevAnimState = this.animState;
      double targetPower = this.isActive ? (double)this.powerAvgClient : 0.0;
      double blend = this.isActive ? 0.3 : 0.18;
      this.powerAvgVisual += (targetPower - this.powerAvgVisual) * blend;
      if (!this.isActive && this.powerAvgVisual < (double)MjAPI.MJ * 0.1) {
         this.powerAvgVisual = 0.0;
      }

      double changeSpeed = this.isActive && MAX_MJ_PER_TICK > 0L ? this.powerAvgVisual / MAX_MJ_PER_TICK * 0.06 : 0.01;
      if (this.isActive) {
         this.animState += changeSpeed;
         if (this.animState >= 1.5) {
            this.animState--;
            this.prevAnimState--;
         }
      } else {
         this.animState = this.animState > changeSpeed ? this.animState - changeSpeed : 0.0;
      }
   }

   private boolean isDistillableFluid(FluidStack fluid) {
      IRefineryRecipeManager manager = BuildcraftRecipeRegistry.refineryRecipes;
      if (manager == null) {
         return false;
      }

      IRefineryRecipeManager.IDistillationRecipe recipe = manager.getDistillationRegistry().getRecipeForInput(fluid);
      return recipe != null;
   }

   public static boolean qualifiesForHeatingAdvancement(int inputHeat, boolean isNether) {
      if (inputHeat < 0) {
         return false;
      }

      int naturalHeat = isNether ? 2 : 0;
      return inputHeat != naturalHeat;
   }

   private void creditRefineAndRedefine(FluidStack outGas, FluidStack outLiquid) {
      if (this.owner != null && this.level != null && !this.level.isClientSide()) {
         MinecraftServer server = this.level.getServer();
         if (server != null) {
            ServerPlayer player = server.getPlayerList().getPlayer(BcAuth.id(this.owner));
            if (player != null) {
               BCFactoryAttachments.OilAndFuelProduction tracker = BCFactoryAttachments.get(player);
               String gasBase = BCEnergyFluidsFabric.getBaseName(outGas.getFluid());
               if (gasBase != null) {
                  String justSaturated = tracker.recordProduction(gasBase, outGas.getAmount());
                  if (justSaturated != null) {
                     AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_REFINE_AND_REDEFINE, justSaturated);
                  }
               }

               String liquidBase = BCEnergyFluidsFabric.getBaseName(outLiquid.getFluid());
               if (liquidBase != null) {
                  String justSaturated = tracker.recordProduction(liquidBase, outLiquid.getAmount());
                  if (justSaturated != null) {
                     AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_REFINE_AND_REDEFINE, justSaturated);
                  }
               }
            }
         }
      }
   }

   public void serverTick() {
      if (this.level != null && !this.level.isClientSide()) {
         if (this.level.getGameTime() % 5L == 0L) {
            FactoryFluidContainers.syncDrainSlot(this.containerSlots, 0, this.tankIn);
            FactoryFluidContainers.syncFillSlot(this.containerSlots, 1, this.tankGasOut);
            FactoryFluidContainers.syncFillSlot(this.containerSlots, 2, this.tankLiquidOut);
         }

         this.mjBattery.tick(this.level, this.worldPosition);
         this.currentRecipe = null;
         IRefineryRecipeManager manager = BuildcraftRecipeRegistry.refineryRecipes;
         if (manager != null) {
            FluidStack inFluid = this.tankIn.getFluidStack();
            if (!inFluid.isEmpty()) {
               this.currentRecipe = manager.getDistillationRegistry().getRecipeForInput(inFluid);
            }
         }

         if (this.currentRecipe == null) {
            this.mjBattery.addPowerChecking(this.distillPower, false);
            this.distillPower = 0L;
            this.isActive = false;
            this.isStuck = false;
         } else {
            FluidStack reqIn = this.currentRecipe.in();
            FluidStack outLiquid = this.currentRecipe.outLiquid();
            FluidStack outGas = this.currentRecipe.outGas();
            FluidStack resIn = this.tankIn.getFluidStack();
            boolean canExtract = !resIn.isEmpty()
               && FluidIdentity.areEquivalentFluidStacks(resIn.copyWithAmount(1), reqIn.copyWithAmount(1))
               && this.tankIn.getAmountMb() >= reqIn.getAmount();
            boolean canFillLiquid;
            boolean canFillGas;
            try (Transaction liquidCheckTransaction = Transaction.openOuter()) {
               canFillLiquid = this.tankLiquidOut.insertMbInternal(outLiquid, outLiquid.getAmount(), liquidCheckTransaction) >= outLiquid.getAmount();
            }

            try (Transaction gasCheckTransaction = Transaction.openOuter()) {
               canFillGas = this.tankGasOut.insertMbInternal(outGas, outGas.getAmount(), gasCheckTransaction) >= outGas.getAmount();
            }

            this.isStuck = !canFillLiquid || !canFillGas;
            if (canExtract && canFillLiquid && canFillGas) {
               long maxPower = MAX_MJ_PER_TICK;
               long stored = this.mjBattery.getStored();
               long powerLimit;
               if (stored <= 0L) {
                  powerLimit = 0L;
               } else {
                  long capacityHalf = this.mjBattery.getCapacity() / 2L;
                  long scaledPower = capacityHalf <= 0L ? 0L : maxPower * stored / capacityHalf;
                  powerLimit = Math.min(scaledPower, MAX_MJ_PER_TICK);
               }

               long power = this.mjBattery.extractPower(0L, powerLimit);
               this.powerAvgSmoothed = this.powerAvgSmoothed + (long)((power - this.powerAvgSmoothed) * 0.1);
               this.distillPower += power;
               long powerReq = this.currentRecipe.powerRequired();
               boolean crafted = this.distillPower >= powerReq;
               this.isActive = power > 0L || crafted;
               if (crafted) {
                  this.distillPower -= powerReq;
                  try (Transaction craftTransaction = Transaction.openOuter()) {
                     this.tankIn.extractMbInternal(resIn, reqIn.getAmount(), craftTransaction);
                     this.tankGasOut.insertMbInternal(outGas, outGas.getAmount(), craftTransaction);
                     this.tankLiquidOut.insertMbInternal(outLiquid, outLiquid.getAmount(), craftTransaction);
                     craftTransaction.commit();
                  }

                  this.creditRefineAndRedefine(outGas, outLiquid);
               }
            } else {
               this.mjBattery.addPowerChecking(this.distillPower, false);
               this.distillPower = 0L;
               this.isActive = false;
            }
         }

         boolean distilling = this.isActive && this.currentRecipe != null;
         if (distilling && !this.wasDistillingForAdvancement && this.owner != null) {
            int inputHeat = BCEnergyFluidsFabric.getHeat(this.currentRecipe.in().getFluid());
            if (qualifiesForHeatingAdvancement(inputHeat, this.level.dimension() == Level.NETHER)) {
               AdvancementUtil.unlockAdvancement(BcAuth.id(this.owner), this.level, ADVANCEMENT_HEATING_AND_DISTILLING);
            }
         }

         this.wasDistillingForAdvancement = distilling;
         if (this.currentRecipe == null || !this.isActive) {
            this.powerAvgSmoothed = this.powerAvgSmoothed + (long)((0L - this.powerAvgSmoothed) * 0.1);
         }

         long mj = MjAPI.MJ;
         this.powerAvgClient = (this.powerAvgSmoothed / mj) * mj;
         this.powerAvgClient = Math.min(this.powerAvgClient, MAX_MJ_PER_TICK);
         int curIn = this.tankIn.getAmountMb();
         int curGas = this.tankGasOut.getAmountMb();
         int curLiq = this.tankLiquidOut.getAmountMb();
         FluidStack curInFluid = syncIdentity(this.tankIn.getFluidStack());
         FluidStack curGasFluid = syncIdentity(this.tankGasOut.getFluidStack());
         FluidStack curLiqFluid = syncIdentity(this.tankLiquidOut.getFluidStack());
         boolean needsSync = curIn != this.lastSyncedIn
            || curGas != this.lastSyncedGas
            || curLiq != this.lastSyncedLiquid
            || !FluidIdentity.areEquivalentFluidStacks(curInFluid, this.lastSyncedInFluid)
            || !FluidIdentity.areEquivalentFluidStacks(curGasFluid, this.lastSyncedGasFluid)
            || !FluidIdentity.areEquivalentFluidStacks(curLiqFluid, this.lastSyncedLiquidFluid)
            || this.isActive != this.lastSyncedActive
            || this.isStuck != this.lastSyncedStuck
            || this.powerAvgClient != this.lastSyncedPower;
         if (needsSync) {
            this.lastSyncedIn = curIn;
            this.lastSyncedGas = curGas;
            this.lastSyncedLiquid = curLiq;
            this.lastSyncedInFluid = curInFluid;
            this.lastSyncedGasFluid = curGasFluid;
            this.lastSyncedLiquidFluid = curLiqFluid;
            this.lastSyncedActive = this.isActive;
            this.lastSyncedStuck = this.isStuck;
            this.lastSyncedPower = this.powerAvgClient;
            this.setChanged();
            if (this.level instanceof ServerLevel level) {
               Packet<?> packet = this.getUpdatePacket();
               if (packet != null) {
                  for (ServerPlayer trackingPlayer : PlayerLookup.tracking(level, this.getBlockPos())) {
                     trackingPlayer.connection.send(packet);
                  }
               }
            }
         }
      }
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      left.add("In = " + FluidDisplayNames.debugString(this.tankIn.getFluidStack()));
      left.add("GasOut = " + FluidDisplayNames.debugString(this.tankGasOut.getFluidStack()));
      left.add("LiquidOut = " + FluidDisplayNames.debugString(this.tankLiquidOut.getFluidStack()));
      left.add("Battery = " + this.mjBattery.getDebugString());
      left.add("Progress = " + MjAPI.formatMj(this.distillPower));
      left.add("Rate = " + LocaleUtil.localizeMjFlow(this.powerAvgClient));
      left.add("CurrRecipe = " + this.currentRecipe);
   }

   //? if >= 1.21.10 {
   @Override
   //?}
   public void preRemoveSideEffects(BlockPos pos, BlockState state) {
      if (this.level != null && !this.level.isClientSide()) {
         this.dropContents(pos);
      }

      //? if >= 1.21.10 {
      super.preRemoveSideEffects(pos, state);
      //?}
   }

   private void dropContents(BlockPos pos) {
      BlockDropsUtil.dropFluidShards(this.level, pos, this.tankIn, this.tankGasOut, this.tankLiquidOut);
      this.extractTankContents(this.tankIn);
      this.extractTankContents(this.tankGasOut);
      this.extractTankContents(this.tankLiquidOut);
      BlockDropsUtil.dropItems(this.level, pos, this.containerSlots);
   }

   private void extractTankContents(SingleFluidTank tank) {
      if (tank.isEmpty()) {
         return;
      }

      FluidStack held = tank.getFluidStack();
      int amountMb = tank.getAmountMb();

      try (Transaction tx = Transaction.openOuter()) {
         tank.extractMb(held, amountMb, tx);
         tx.commit();
      }
   }

   @Override
   public void getClientDebugInfo(List<String> left, List<String> right, Direction side) {
      this.smoothIn.getDebugInfo(left, right, side);
      this.smoothGasOut.getDebugInfo(left, right, side);
      this.smoothLiquidOut.getDebugInfo(left, right, side);

      Direction facing = Direction.WEST;
      if (this.level != null) {
         BlockState state = this.level.getBlockState(this.worldPosition);
         if (state.hasProperty(BlockDistiller.FACING)) {
            facing = (Direction)state.getValue(BlockDistiller.FACING);
         }
      }

      left.add("Model Variables:");
      left.add("  facing = " + facing);
      left.add("  active = " + this.isActive);
      left.add("  power_average = " + this.powerAvgClient / MjAPI.MJ);
      left.add("  power_max = " + MAX_MJ_PER_TICK / MjAPI.MJ);
      left.add("Current Model Variables:");
      left.add("  animState = " + String.format("%.4f", this.animState));
   }

   @Override
   public BlockEntity asBlockEntity() {
      return this;
   }

   public Component getDisplayName() {
      return Component.translatable("block.buildcraftfactory.distiller");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
      return new ContainerDistiller(containerId, playerInv, this);
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
   /*protected void saveAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
      super.saveAdditional(tag, registries);
      this.writeData(new BcValueOut(tag, registries));
   }

   protected void loadAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
      super.loadAdditional(tag, registries);
      this.readData(new BcValueIn(tag, registries));
   }
   *///?}

   protected void writeData(BcValueOut output) {
      if (this.owner != null && BcAuth.id(this.owner) != null) {
         output.putString("ownerUUID", BcAuth.id(this.owner).toString());
         if (BcAuth.name(this.owner) != null) {
            output.putString("ownerName", BcAuth.name(this.owner));
         }
      }

      if (!this.tankIn.isEmpty()) {
         output.store("fluidIn", FluidStack.CODEC, this.tankIn.getFluidStack());
      }

      if (!this.tankGasOut.isEmpty()) {
         output.store("fluidGasOut", FluidStack.CODEC, this.tankGasOut.getFluidStack());
      }

      if (!this.tankLiquidOut.isEmpty()) {
         output.store("fluidLiquidOut", FluidStack.CODEC, this.tankLiquidOut.getFluidStack());
      }

      output.putLong("mjStored", this.mjBattery.getStored());
      output.putLong("distillPower", this.distillPower);
      output.putBoolean("isActive", this.isActive);
      output.putBoolean("isStuck", this.isStuck);
      output.putLong("powerAvgClient", this.powerAvgClient);
      output.store("containerSlots", CompoundTag.CODEC, this.containerSlots.serializeNBT());
   }

   protected void readData(BcValueIn input) {
      String ownerUuid = input.getStringOr("ownerUUID", "");
      if (!ownerUuid.isEmpty()) {
         try {
            this.owner = new GameProfile(UUID.fromString(ownerUuid), input.getStringOr("ownerName", "Unknown"));
         } catch (IllegalArgumentException e) {
            this.owner = null;
         }
      }

      FluidStack fluidIn = input.read("fluidIn", FluidStack.CODEC).orElse(FluidStack.EMPTY);
      this.tankIn.setContents(fluidIn.isEmpty() ? FluidStack.EMPTY : fluidIn);
      FluidStack fluidGasOut = input.read("fluidGasOut", FluidStack.CODEC).orElse(FluidStack.EMPTY);
      this.tankGasOut.setContents(fluidGasOut.isEmpty() ? FluidStack.EMPTY : fluidGasOut);
      FluidStack fluidLiquidOut = input.read("fluidLiquidOut", FluidStack.CODEC).orElse(FluidStack.EMPTY);
      this.tankLiquidOut.setContents(fluidLiquidOut.isEmpty() ? FluidStack.EMPTY : fluidLiquidOut);
      this.mjBattery.addPowerChecking(input.getLongOr("mjStored", 0L), false);
      this.distillPower = input.getLongOr("distillPower", 0L);
      this.isActive = input.getBooleanOr("isActive", false);
      this.isStuck = input.getBooleanOr("isStuck", false);
      this.powerAvgClient = input.getLongOr("powerAvgClient", 0L);
      this.containerSlots.deserializeNBT((CompoundTag)input.read("containerSlots", CompoundTag.CODEC).orElseGet(CompoundTag::new));
   }

   private static FluidStack syncIdentity(FluidStack stack) {
      return stack.isEmpty() ? FluidStack.EMPTY : stack.copyWithAmount(1);
   }

   public CompoundTag getUpdateTag(Provider registries) {
      return this.saveCustomOnly(registries);
   }

   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }
}
