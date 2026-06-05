package buildcraft.energy.tile;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import buildcraft.api.mj.MjRfConversion;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.core.BCCoreItems;
import buildcraft.energy.BCEnergyBlockEntities;
import buildcraft.lib.attachments.Attachments;
import buildcraft.lib.attachments.AttachmentQueries;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.transfer.energy.EnergyHandler;
import buildcraft.lib.transfer.energy.SimpleEnergyHandler;
import buildcraft.lib.transfer.transaction.Transaction;

@SuppressWarnings("this-escape")
public class TileEngineRF extends TileEngineBase_BC8 implements MenuProvider, BlockEntityExtendedMenu {
    public static final int MAX_FE = 10_000;
    public static final float HEAT_RATE = 0.06f;
    public static final float COOLDOWN_RATE = 0.01f;

    public static final Map<Item, Long> UPGRADE_VALUES = new LinkedHashMap<>();

    public static void initUpgrades() {
        if (UPGRADE_VALUES.isEmpty()) {
            UPGRADE_VALUES.put(BCCoreItems.GEAR_IRON, MjAPI.MJ * 2);
            UPGRADE_VALUES.put(BCCoreItems.GEAR_GOLD, MjAPI.MJ * 3);
        }
    }

    public final buildcraft.lib.tile.item.ItemHandlerSimple upgrades =
        new buildcraft.lib.tile.item.ItemHandlerSimple(4, (handler, slot, bef, aft) -> setChanged());

    {
        upgrades.setChecker((slot, stack) -> {
            initUpgrades();
            return UPGRADE_VALUES.containsKey(stack.getItem());
        });
        upgrades.setLimitedInsertor(1);
    }

    public final SimpleEnergyHandler energyStorage = new SimpleEnergyHandler(MAX_FE, MAX_FE, 0) {
        @Override
        protected void onEnergyChanged(int previousAmount) {
            setChanged();
        }
    };

    public TileEngineRF(BlockPos pos, BlockState state) {
        super(BCEnergyBlockEntities.ENGINE_FE, pos, state);
    }

    public int getCurrentFe() {
        return (int) energyStorage.getAmountAsLong();
    }

    public void setCurrentFe(int fe) {
        energyStorage.set(Math.max(0, Math.min(MAX_FE, fe)));
    }

    @Override
    public boolean isBurning() {
        return getCurrentFe() > 0 && isRedstonePowered;
    }

    public long getMjPerTick() {
        initUpgrades();
        long value = MjAPI.MJ * 4;
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            ItemStack stack = upgrades.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            Long add = UPGRADE_VALUES.get(stack.getItem());
            if (add != null) {
                value += add;
            }
        }
        return value;
    }

    public int getFeConsumptionRate() {
        final long mjPerTick = getMjPerTick();
        long mjPerRf = MjRfConversion.createParsed(BCLibConfig.mjRfConversionAmount.get()).mjPerRf;
        if (mjPerRf == 0) return 0;
        return (int) (mjPerTick / mjPerRf);
    }

    @Override
    protected void engineUpdate() {

        pullFeFromNeighbors();

        currentOutput = 0;
        int currentFe = getCurrentFe();
        if (currentFe <= 0 || !isRedstonePowered) return;

        long mjPerRf = MjRfConversion.createParsed(BCLibConfig.mjRfConversionAmount.get()).mjPerRf;
        if (mjPerRf == 0) {
            return;
        }
        int maxFe = getFeConsumptionRate();

        int feConsumed = Math.min(currentFe, maxFe);
        long mjGenerated = feConsumed * mjPerRf;

        if (power + mjGenerated >= getMaxPower()) {
            return;
        }

        currentOutput = mjGenerated;
        power += mjGenerated;
        energyStorage.set(currentFe - feConsumed);
        heat += HEAT_RATE;
        if (heat >= 200) {
            heat = 200;
        }
    }

    private void pullFeFromNeighbors() {
        int currentFe = getCurrentFe();
        if (level == null || currentFe >= MAX_FE) return;
        for (Direction dir : Direction.values()) {
            if (dir == orientation) continue;
            if (currentFe >= MAX_FE) break;
            BlockPos neighborPos = getBlockPos().relative(dir);
            EnergyHandler handler = AttachmentQueries.getBlock(level, Attachments.Energy.BLOCK, neighborPos, dir.getOpposite());
            if (handler == null) continue;
            int want = MAX_FE - currentFe;
            if (want <= 0) break;
            try (Transaction transaction = Transaction.openRoot()) {
                int extracted = handler.extract(want, transaction);
                if (extracted > 0) {
                    transaction.commit();
                    currentFe += extracted;
                    energyStorage.set(currentFe);
                }
            }
        }
    }

    @Override
    public void updateHeatLevel() {
        if (heat > MIN_HEAT) {
            heat -= COOLDOWN_RATE;
        }
        if (heat <= MIN_HEAT) {
            heat = MIN_HEAT;
        }
        getPowerStage();
    }

    @Nonnull
    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(false);
    }

    @Override
    public long getMaxPower() {
        return 1000 * MjAPI.MJ;
    }

    @Override
    public long minPowerReceived() {
        return 0;
    }

    @Override
    public long maxPowerReceived() {
        return 200 * MjAPI.MJ;
    }

    @Override
    public long maxPowerExtracted() {
        return 500 * MjAPI.MJ;
    }

    @Override
    public long getCurrentOutput() {
        return currentOutput;
    }

    @Override
    public float explosionRange() {
        return 4;
    }

    @Override
    protected int getMaxChainLength() {
        return 4;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("currentFe", getCurrentFe());
        output.store("upgrades", net.minecraft.nbt.CompoundTag.CODEC, upgrades.serializeNBT());
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        setCurrentFe(input.getIntOr("currentFe", 0));
        upgrades.deserializeNBT(input.read("upgrades", net.minecraft.nbt.CompoundTag.CODEC).orElseGet(net.minecraft.nbt.CompoundTag::new));
    }

    @Override
    public net.minecraft.world.level.block.entity.BlockEntity asBlockEntity() {
        return this;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraftenergy.engine_rf");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new buildcraft.energy.container.ContainerEngineRF(containerId, playerInv, this);
    }
}
