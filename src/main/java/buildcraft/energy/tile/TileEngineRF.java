package buildcraft.energy.tile;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjRfConversion;
import buildcraft.core.BCCoreItems;
import buildcraft.energy.BCEnergyBlockEntities;
import buildcraft.energy.container.ContainerEngineRF;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import static buildcraft.lib.engine.TileEngineBase_BC8.MAX_HEAT;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.fabric.transfer.EnergyStorageOps;
import buildcraft.lib.fabric.transfer.FeEnergyStorage;
import buildcraft.lib.fabric.transfer.TriggerTransferAccess;
import buildcraft.lib.tile.ItemHandlerSimple;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import team.reborn.energy.api.EnergyStorage;

public class TileEngineRF extends TileEngineBase_BC8 implements MenuProvider, BlockEntityExtendedMenu {
   public static final int MAX_FE = 10000;
   public static final float HEAT_RATE = 0.06F;
   public static final float COOLDOWN_RATE = 0.01F;
   public static final Map<Item, Long> UPGRADE_VALUES = new LinkedHashMap<>();
   public final ItemHandlerSimple upgrades = new ItemHandlerSimple(4, (handler, slot, bef, aft) -> this.setChanged());
   public final FeEnergyStorage energyStorage;

   public static void initUpgrades() {
      if (UPGRADE_VALUES.isEmpty()) {
         UPGRADE_VALUES.put(BCCoreItems.GEAR_IRON, MjAPI.MJ * 2L);
         UPGRADE_VALUES.put(BCCoreItems.GEAR_GOLD, MjAPI.MJ * 3L);
      }
   }

   public TileEngineRF(BlockPos pos, BlockState state) {
      super(BCEnergyBlockEntities.ENGINE_FE, pos, state);
      this.upgrades.setChecker((slot, stack) -> {
         initUpgrades();
         return UPGRADE_VALUES.containsKey(stack.getItem());
      });
      this.upgrades.setLimitedInsertor(1);
      this.energyStorage = new FeEnergyStorage(10000, 10000, 0) {
         @Override
         protected void onEnergyChanged(int previousAmount) {
            TileEngineRF.this.setChanged();
         }
      };
   }

   @Nullable
   public EnergyStorage getSidedEnergyStorage(@Nullable Direction direction) {
      return direction != null && direction != this.getOrientation() ? this.energyStorage : null;
   }

   public int getCurrentFe() {
      return (int)this.energyStorage.getAmount();
   }

   public void setCurrentFe(int fe) {
      this.energyStorage.set(Math.max(0, Math.min(10000, fe)));
   }

   @Override
   public boolean isBurning() {
      return this.getCurrentFe() > 0 && this.isRedstonePowered;
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

   public int getFeConsumptionRate() {
      long mjPerTick = this.getMjPerTick();
      long mjPerRf = MjRfConversion.createParsed(BCLibConfig.mjRfConversionAmount.get()).mjPerRf;
      return mjPerRf == 0L ? 0 : (int)(mjPerTick / mjPerRf);
   }

   @Override
   protected void engineUpdate() {
      this.pullFeFromNeighbors();
      this.currentOutput = 0L;
      int currentFe = this.getCurrentFe();
      if (currentFe > 0 && this.isRedstonePowered) {
         long mjPerRf = MjRfConversion.createParsed(BCLibConfig.mjRfConversionAmount.get()).mjPerRf;
         if (mjPerRf != 0L) {
            int maxFe = this.getFeConsumptionRate();
            int feConsumed = Math.min(currentFe, maxFe);
            long mjGenerated = feConsumed * mjPerRf;
            if (this.power + mjGenerated < this.getMaxPower()) {
               this.currentOutput = mjGenerated;
               this.power += mjGenerated;
               this.energyStorage.set(currentFe - feConsumed);
               this.heat += 0.06F;
               if (this.heat >= MAX_HEAT) {
                  this.heat = MAX_HEAT;
               }
            }
         }
      }
   }

   private void pullFeFromNeighbors() {
      int currentFe = this.getCurrentFe();
      if (this.level != null && currentFe < 10000) {
         for (Direction dir : Direction.values()) {
            if (dir != this.orientation) {
               if (currentFe >= 10000) {
                  break;
               }

               BlockPos neighborPos = this.getBlockPos().relative(dir);
               EnergyStorage storage = TriggerTransferAccess.blockEnergyStorage(this.level, neighborPos, dir.getOpposite());
               if (storage != null) {
                  int want = 10000 - currentFe;
                  if (want <= 0) {
                     break;
                  }

                  int extracted = EnergyStorageOps.extract(storage, want, true);
                  if (extracted > 0) {
                     currentFe += extracted;
                     this.energyStorage.set(currentFe);
                  }
               }
            }
         }
      }
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

   @Nonnull
   @Override
   protected IMjConnector createConnector() {
      return new EngineConnector(false);
   }

   @Override
   public long getMaxPower() {
      return 1000L * MjAPI.MJ;
   }

   @Override
   public long minPowerReceived() {
      return 0L;
   }

   @Override
   public long maxPowerReceived() {
      return 200L * MjAPI.MJ;
   }

   @Override
   public long maxPowerExtracted() {
      return 500L * MjAPI.MJ;
   }

   @Override
   public long getCurrentOutput() {
      return this.currentOutput;
   }

   @Override
   public float explosionRange() {
      return 4.0F;
   }

   @Override
   protected int getMaxChainLength() {
      return 4;
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putInt("currentFe", this.getCurrentFe());
      output.store("upgrades", CompoundTag.CODEC, this.upgrades.serializeNBT());
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.setCurrentFe(input.getIntOr("currentFe", 0));
      this.upgrades.deserializeNBT((CompoundTag)input.read("upgrades", CompoundTag.CODEC).orElseGet(CompoundTag::new));
   }

   @Override
   public BlockEntity asBlockEntity() {
      return this;
   }

   public Component getDisplayName() {
      return Component.translatable("block.buildcraftenergy.engine_rf");
   }

   public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
      return new ContainerEngineRF(containerId, playerInv, this);
   }
}
