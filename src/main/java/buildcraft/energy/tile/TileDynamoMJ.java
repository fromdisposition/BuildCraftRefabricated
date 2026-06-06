package buildcraft.energy.tile;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjRfConversion;
import buildcraft.core.BCCoreItems;
import buildcraft.energy.BCEnergyBlockEntities;
import buildcraft.energy.container.ContainerDynamoMJ;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.fabric.transfer.EnergyStorageOps;
import buildcraft.lib.fabric.transfer.FeEnergyStorage;
import buildcraft.lib.fabric.transfer.TriggerTransferAccess;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.tile.ItemHandlerSimple;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public class TileDynamoMJ extends TileEngineBase_BC8 implements MenuProvider, BlockEntityExtendedMenu {
   public static final int MAX_FE = 10000;
   public static final long MAX_MJ = 1000L * MjAPI.MJ;
   public static final float HEAT_RATE = 0.06F;
   public static final float COOLDOWN_RATE = 0.01F;
   public static final Map<Item, Long> UPGRADE_VALUES = new LinkedHashMap<>();
   private final MjBattery mjBattery;
   private final MjBatteryReceiver mjConnector;
   public final ItemHandlerSimple upgrades = new ItemHandlerSimple(4, (handler, slot, bef, aft) -> this.setChanged());
   public final FeEnergyStorage energyStorage;

   public static void initUpgrades() {
      if (UPGRADE_VALUES.isEmpty()) {
         UPGRADE_VALUES.put(BCCoreItems.GEAR_IRON, MjAPI.MJ * 2L);
         UPGRADE_VALUES.put(BCCoreItems.GEAR_GOLD, MjAPI.MJ * 3L);
      }
   }

   public TileDynamoMJ(BlockPos pos, BlockState state) {
      super(BCEnergyBlockEntities.DYNAMO_MJ, pos, state);
      this.upgrades.setChecker((slot, stack) -> {
         initUpgrades();
         return UPGRADE_VALUES.containsKey(stack.getItem());
      });
      this.upgrades.setLimitedInsertor(1);
      this.energyStorage = new FeEnergyStorage(10000, 0, 10000) {
         @Override
         protected void onEnergyChanged(int previousAmount) {
            TileDynamoMJ.this.setChanged();
         }
      };
      this.mjBattery = new MjBattery(MAX_MJ);
      this.mjConnector = new MjBatteryReceiver(this.mjBattery);
   }

   @Nullable
   public EnergyStorage getSidedEnergyStorage(@Nullable Direction direction) {
      return direction != null && direction == this.getOrientation() ? this.energyStorage : null;
   }

   public int getCurrentFe() {
      return (int)this.energyStorage.getAmount();
   }

   public void setCurrentFe(int fe) {
      this.energyStorage.set(Math.max(0, Math.min(10000, fe)));
   }

   @Nonnull
   @Override
   protected IMjConnector createConnector() {
      return this.mjConnector;
   }

   public MjBattery getMjBattery() {
      return this.mjBattery;
   }

   public MjBatteryReceiver getMjReceiver() {
      return this.mjConnector;
   }

   @Override
   public boolean isBurning() {
      return this.mjBattery.getStored() > 0L && this.isRedstonePowered;
   }

   public long getMjPerTick() {
      initUpgrades();
      long value = MjAPI.MJ * 4L;

      for (int slot = 0; slot < this.upgrades.getSlots(); slot++) {
         ItemStack stack = this.upgrades.getStackInSlot(slot);
         if (!stack.isEmpty()) {
            Long add = UPGRADE_VALUES.get(stack.getItem());
            if (add != null) {
               value += add;
            }
         }
      }

      return value;
   }

   public int getFeProductionRate(long mjInput) {
      long mjPerRf = MjRfConversion.createParsed(BCLibConfig.mjRfConversionAmount.get()).mjPerRf;
      return mjPerRf == 0L ? 0 : (int)(mjInput / mjPerRf);
   }

   @Override
   protected void engineUpdate() {
      this.sendFeToReceiver();
      this.currentOutput = 0L;
      long mjStored = this.mjBattery.getStored();
      if (mjStored > 0L) {
         if (this.isRedstonePowered) {
            long mjPerRf = MjRfConversion.createParsed(BCLibConfig.mjRfConversionAmount.get()).mjPerRf;
            if (mjPerRf == 0L) {
               return;
            }

            int genFe = this.getFeProductionRate(this.getMjPerTick());
            int maxFe = (int)Math.min(genFe, mjStored / mjPerRf);
            int currentFe = this.getCurrentFe();
            maxFe = Math.min(maxFe, 10000 - currentFe);
            if (maxFe <= 0) {
               return;
            }

            if (this.mjBattery.extractPower(maxFe * mjPerRf)) {
               this.currentOutput = maxFe;
               this.energyStorage.set(currentFe + maxFe);
               this.heat += 0.06F;
               if (this.heat >= 200.0F) {
                  this.heat = 200.0F;
               }
            }
         } else {
            this.currentOutput = 0L;
         }
      }
   }

   private void sendFeToReceiver() {
      int currentFe = this.getCurrentFe();
      if (this.level != null && currentFe > 0) {
         EnergyStorage receiver = this.getFeReceiver(this.orientation);
         if (receiver != null) {
            int accepted = EnergyStorageOps.insert(receiver, currentFe, true);
            if (accepted > 0) {
               this.energyStorage.set(currentFe - accepted);
            }
         }
      }
   }

   @Nullable
   public EnergyStorage getFeReceiver(Direction side) {
      if (this.level == null) {
         return null;
      }

      BlockPos pos = this.getBlockPos();

      for (int len = 0; len <= this.getMaxChainLength(); len++) {
         BlockPos targetPos = pos.relative(side);
         BlockEntity tile = this.level.getBlockEntity(targetPos);
         if (tile == null) {
            return null;
         }

         if (tile.getClass() != this.getClass()) {
            return TriggerTransferAccess.blockEnergyStorage(this.level, targetPos, side.getOpposite());
         }

         if (((TileDynamoMJ)tile).orientation != side) {
            return null;
         }

         pos = targetPos;
      }

      return null;
   }

   @Nullable
   @Override
   public IMjReceiver getReceiverToPower(Direction side) {
      return this.getFeReceiver(side) != null ? new IMjReceiver() {
         @Override
         public long getPowerRequested() {
            return 1L;
         }

         @Override
         public long receivePower(long microJoules, boolean simulate) {
            return 0L;
         }

         @Override
         public boolean canConnect(IMjConnector other) {
            return true;
         }
      } : null;
   }

   @Override
   public void updateHeatLevel() {
      if (this.heat > 20.0F) {
         this.heat -= 0.01F;
      }

      if (this.heat <= 20.0F) {
         this.heat = 20.0F;
      }

      this.getPowerStage();
   }

   @Override
   public long getMaxPower() {
      return MAX_MJ;
   }

   @Override
   public long minPowerReceived() {
      return 0L;
   }

   @Override
   public long maxPowerReceived() {
      return 0L;
   }

   @Override
   public long maxPowerExtracted() {
      return 0L;
   }

   @Override
   public long extractPower(long min, long max, boolean doExtract) {
      return !doExtract && this.currentOutput > 0L ? Math.max(min, 1L) : 0L;
   }

   @Override
   protected void sendPower(@Nullable IMjReceiver receiver) {
   }

   @Override
   public long getCurrentOutput() {
      long mjPerRf = MjRfConversion.createParsed(BCLibConfig.mjRfConversionAmount.get()).mjPerRf;
      return mjPerRf <= 0L ? 0L : this.currentOutput * mjPerRf;
   }

   @Override
   public float explosionRange() {
      return 4.0F;
   }

   @Override
   protected int getMaxChainLength() {
      return 3;
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putInt("currentFe", this.getCurrentFe());
      output.store("upgrades", CompoundTag.CODEC, this.upgrades.serializeNBT());
      output.putLong("mjStored", this.mjBattery.getStored());
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.setCurrentFe(input.getIntOr("currentFe", 0));
      this.upgrades.deserializeNBT((CompoundTag)input.read("upgrades", CompoundTag.CODEC).orElseGet(CompoundTag::new));
      CompoundTag mjTag = new CompoundTag();
      mjTag.putLong("stored", input.getLongOr("mjStored", 0L));
      this.mjBattery.deserializeNBT(mjTag);
   }

   public Component getDisplayName() {
      return Component.translatable("block.buildcraftenergy.mj_dynamo");
   }

   public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
      return new ContainerDynamoMJ(containerId, playerInv, this);
   }

   @Override
   public BlockEntity asBlockEntity() {
      return this;
   }
}
