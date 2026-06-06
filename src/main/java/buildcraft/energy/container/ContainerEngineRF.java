package buildcraft.energy.container;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.energy.BCEnergyMenuTypes;
import buildcraft.energy.tile.TileEngineRF;
import buildcraft.lib.fabric.menu.MenuBlockEntityLookup;
import buildcraft.lib.gui.BcMenu;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.tile.ItemHandlerSimple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class ContainerEngineRF extends BcMenu {
   private static final ItemHandlerSimple FALLBACK_UPGRADES = createFallbackUpgrades();
   public final TileEngineRF engine;
   private final ContainerData data;
   private static final int DATA_POWER_HI = 0;
   private static final int DATA_POWER_LO = 1;
   private static final int DATA_HEAT = 2;
   private static final int DATA_OUTPUT_HI = 3;
   private static final int DATA_OUTPUT_LO = 4;
   private static final int DATA_POWER_STAGE = 5;
   private static final int DATA_IS_BURNING_ENGINE = 6;
   private static final int DATA_FE_STORED = 7;
   private static final int DATA_COUNT = 8;

   public ContainerEngineRF(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, MenuBlockEntityLookup.get(playerInv, pos, TileEngineRF.class));
   }

   public ContainerEngineRF(int containerId, Inventory playerInv, final TileEngineRF engine) {
      super(BCEnergyMenuTypes.ENGINE_FE, containerId, playerInv.player);
      this.engine = engine;
      if (engine != null && engine.getLevel() != null && !engine.getLevel().isClientSide()) {
         this.data = new ContainerData() {
            public int get(int index) {
               return switch (index) {
                  case 0 -> (int)(engine.getPower() >>> 32);
                  case 1 -> (int)(engine.getPower() & 4294967295L);
                  case 2 -> Float.floatToIntBits(engine.getHeat());
                  case 3 -> (int)(engine.getCurrentOutput() >>> 32);
                  case 4 -> (int)(engine.getCurrentOutput() & 4294967295L);
                  case 5 -> engine.getPowerStage().ordinal();
                  case 6 -> engine.isBurning() ? 1 : 0;
                  case 7 -> engine.getCurrentFe();
                  default -> 0;
               };
            }

            public void set(int index, int value) {
            }

            public int getCount() {
               return 8;
            }
         };
      } else {
         SimpleContainerData clientData = new SimpleContainerData(8);
         clientData.set(2, Float.floatToIntBits(20.0F));
         this.data = clientData;
      }

      this.addDataSlots(this.data);
      ItemHandlerSimple upgrades = engine != null ? engine.upgrades : FALLBACK_UPGRADES;

      for (int slot = 0; slot < 4; slot++) {
         this.addSlot(new SlotBase(upgrades, slot, 62 + 18 * slot, 44) {
            @Override
            public int getMaxStackSize() {
               return 1;
            }

            @Override
            public int getMaxStackSize(ItemStack stack) {
               return 1;
            }
         });
      }

      this.addFullPlayerInventory(8, 95, playerInv);
   }

   public long getSyncedPower() {
      return (long)this.data.get(0) << 32 | this.data.get(1) & 4294967295L;
   }

   public float getSyncedHeat() {
      return Float.intBitsToFloat(this.data.get(2));
   }

   public EnumPowerStage getSyncedPowerStage() {
      int ordinal = this.data.get(5);
      EnumPowerStage[] values = EnumPowerStage.values();
      return ordinal >= 0 && ordinal < values.length ? values[ordinal] : EnumPowerStage.BLUE;
   }

   public boolean isSyncedBurningEngine() {
      return this.data.get(6) != 0;
   }

   public long getSyncedCurrentOutput() {
      return (long)this.data.get(3) << 32 | this.data.get(4) & 4294967295L;
   }

   public int getSyncedFeStored() {
      return this.data.get(7);
   }

   @Override
   public boolean stillValid(Player player) {
      return this.engine != null && !this.engine.isRemoved()
         ? player.distanceToSqr(this.engine.getBlockPos().getX() + 0.5, this.engine.getBlockPos().getY() + 0.5, this.engine.getBlockPos().getZ() + 0.5) <= 64.0
         : false;
   }

   private static ItemHandlerSimple createFallbackUpgrades() {
      ItemHandlerSimple upgrades = new ItemHandlerSimple(4, 1);
      upgrades.setChecker((slot, stack) -> false);
      return upgrades;
   }
}
