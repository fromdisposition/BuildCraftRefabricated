package buildcraft.energy.tile;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.Nullable;

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

import buildcraft.api.mj.MjRfConversion;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.core.BCCoreItems;
import buildcraft.energy.BCEnergyBlockEntities;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.attachments.Attachments;
import buildcraft.lib.attachments.AttachmentQueries;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.transfer.energy.EnergyHandler;
import buildcraft.lib.transfer.energy.SimpleEnergyHandler;
import buildcraft.lib.transfer.transaction.Transaction;

@SuppressWarnings("this-escape")
public class TileDynamoMJ extends TileEngineBase_BC8 implements MenuProvider, BlockEntityExtendedMenu {
    public static final int MAX_FE = 10_000;
    public static final long MAX_MJ = 1000 * MjAPI.MJ;

    public static final float HEAT_RATE = 0.06f;
    public static final float COOLDOWN_RATE = 0.01f;

    public static final Map<Item, Long> UPGRADE_VALUES = new LinkedHashMap<>();

    public static void initUpgrades() {
        if (UPGRADE_VALUES.isEmpty()) {
            UPGRADE_VALUES.put(BCCoreItems.GEAR_IRON, MjAPI.MJ * 2);
            UPGRADE_VALUES.put(BCCoreItems.GEAR_GOLD, MjAPI.MJ * 3);
        }
    }

    private final MjBattery mjBattery;
    private final MjBatteryReceiver mjConnector;

    public final buildcraft.lib.tile.item.ItemHandlerSimple upgrades =
        new buildcraft.lib.tile.item.ItemHandlerSimple(4, (handler, slot, bef, aft) -> setChanged());

    {
        upgrades.setChecker((slot, stack) -> {
            initUpgrades();
            return UPGRADE_VALUES.containsKey(stack.getItem());
        });
        upgrades.setLimitedInsertor(1);
    }

    public final SimpleEnergyHandler energyStorage = new SimpleEnergyHandler(MAX_FE, 0, MAX_FE) {
        @Override
        protected void onEnergyChanged(int previousAmount) {
            setChanged();
        }
    };

    public TileDynamoMJ(BlockPos pos, BlockState state) {
        super(BCEnergyBlockEntities.DYNAMO_MJ, pos, state);
        mjBattery = new MjBattery(MAX_MJ);
        mjConnector = new MjBatteryReceiver(mjBattery);
    }

    public int getCurrentFe() {
        return (int) energyStorage.getAmountAsLong();
    }

    public void setCurrentFe(int fe) {
        energyStorage.set(Math.max(0, Math.min(MAX_FE, fe)));
    }

    @Nonnull
    @Override
    protected IMjConnector createConnector() {
        return mjConnector;
    }

    public MjBattery getMjBattery() {
        return mjBattery;
    }

    public MjBatteryReceiver getMjReceiver() {
        return mjConnector;
    }

    @Override
    public boolean isBurning() {
        return mjBattery.getStored() > 0 && isRedstonePowered;
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

    public int getFeProductionRate(long mjInput) {
        long mjPerRf = MjRfConversion.createParsed(BCLibConfig.mjRfConversionAmount.get()).mjPerRf;
        if (mjPerRf == 0) return 0;
        return (int) (mjInput / mjPerRf);
    }

    @Override
    protected void engineUpdate() {

        sendFeToReceiver();

        currentOutput = 0;

        long mjStored = mjBattery.getStored();
        if (mjStored <= 0) return;

        if (isRedstonePowered) {
            long mjPerRf = MjRfConversion.createParsed(BCLibConfig.mjRfConversionAmount.get()).mjPerRf;
            if (mjPerRf == 0) return;

            int genFe = getFeProductionRate(getMjPerTick());
            int maxFe = (int) Math.min(genFe, mjStored / mjPerRf);

            int currentFe = getCurrentFe();
            maxFe = Math.min(maxFe, MAX_FE - currentFe);

            if (maxFe <= 0) return;

            if (mjBattery.extractPower(maxFe * mjPerRf)) {
                currentOutput = maxFe;
                energyStorage.set(currentFe + maxFe);
                heat += HEAT_RATE;
                if (heat >= 200) {
                    heat = 200;
                }
            }
        } else {
            currentOutput = 0;
        }
    }

    private void sendFeToReceiver() {
        int currentFe = getCurrentFe();
        if (level == null || currentFe <= 0) return;
        EnergyHandler receiver = getFeReceiver(orientation);
        if (receiver == null) return;
        try (Transaction transaction = Transaction.openRoot()) {
            int accepted = receiver.insert(currentFe, transaction);
            if (accepted > 0) {
                transaction.commit();
                energyStorage.set(currentFe - accepted);
            }
        }
    }

    @Nullable
    public EnergyHandler getFeReceiver(Direction side) {
        if (level == null) return null;

        BlockPos pos = getBlockPos();
        for (int len = 0; len <= getMaxChainLength(); len++) {
            BlockPos targetPos = pos.relative(side);
            BlockEntity tile = level.getBlockEntity(targetPos);
            if (tile == null) {
                return null;
            }
            if (tile.getClass() == getClass()) {

                if (((TileDynamoMJ) tile).orientation != side) {
                    return null;
                }
                pos = targetPos;
                continue;
            }

            return AttachmentQueries.getBlock(level, Attachments.Energy.BLOCK, targetPos, side.getOpposite());
        }
        return null;
    }

    @Nullable
    @Override
    public IMjReceiver getReceiverToPower(Direction side) {
        if (getFeReceiver(side) != null) {
            return new IMjReceiver() {
                @Override public long getPowerRequested() { return 1; }
                @Override public long receivePower(long microJoules, boolean simulate) { return 0; }
                @Override public boolean canConnect(IMjConnector other) { return true; }
            };
        }
        return null;
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

    @Override
    public long getMaxPower() {
        return MAX_MJ;
    }

    @Override
    public long minPowerReceived() {
        return 0;
    }

    @Override
    public long maxPowerReceived() {
        return 0;
    }

    @Override
    public long maxPowerExtracted() {
        return 0;
    }

    @Override
    public long extractPower(long min, long max, boolean doExtract) {
        if (!doExtract && currentOutput > 0) {
            return Math.max(min, 1);
        }
        return 0;
    }

    @Override
    protected void sendPower(@Nullable IMjReceiver receiver) {

    }

    @Override
    public long getCurrentOutput() {
        long mjPerRf = MjRfConversion.createParsed(BCLibConfig.mjRfConversionAmount.get()).mjPerRf;
        if (mjPerRf <= 0) {
            return 0;
        }
        return currentOutput * mjPerRf;
    }

    @Override
    public float explosionRange() {
        return 4;
    }

    @Override
    protected int getMaxChainLength() {
        return 3;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("currentFe", getCurrentFe());
        output.store("upgrades", net.minecraft.nbt.CompoundTag.CODEC, upgrades.serializeNBT());
        output.putLong("mjStored", mjBattery.getStored());
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        setCurrentFe(input.getIntOr("currentFe", 0));
        upgrades.deserializeNBT(input.read("upgrades", net.minecraft.nbt.CompoundTag.CODEC).orElseGet(net.minecraft.nbt.CompoundTag::new));

        CompoundTag mjTag = new CompoundTag();
        mjTag.putLong("stored", input.getLongOr("mjStored", 0L));
        mjBattery.deserializeNBT(mjTag);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraftenergy.mj_dynamo");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new buildcraft.energy.container.ContainerDynamoMJ(containerId, playerInv, this);
    }

    @Override
    public BlockEntity asBlockEntity() {
        return this;
    }
}
