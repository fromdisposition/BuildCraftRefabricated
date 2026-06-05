/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.mj;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;

import buildcraft.lib.transfer.energy.EnergyHandler;
import buildcraft.lib.transfer.transaction.SnapshotJournal;
import buildcraft.lib.transfer.transaction.TransactionContext;

public class MjBatteryEnergyHandler extends SnapshotJournal<Long> implements EnergyHandler {
    private final MjBattery battery;

    public MjBatteryEnergyHandler(MjBattery battery) {
        this.battery = battery;
    }

    public static MjBatteryEnergyHandler createIfRfEnabled(MjBattery battery) {
        return MjAPI.isRfAutoConversionEnabled() ? new MjBatteryEnergyHandler(battery) : null;
    }

    private long mjPerRf() {
        return MjAPI.getRfConversion().mjPerRf;
    }

    @Override
    protected Long createSnapshot() {
        return battery.getStored();
    }

    @Override
    protected void revertToSnapshot(Long snapshot) {

        long current = battery.getStored();
        if (current > snapshot) {
            battery.extractPower(current - snapshot, current - snapshot);
        } else if (current < snapshot) {
            battery.addPower(snapshot - current, false);
        }
    }

    @Override
    protected void releaseSnapshot(Long snapshot) {

    }

    @Override
    public long getAmountAsLong() {
        long mjpr = mjPerRf();
        if (mjpr <= 0) return 0;
        return battery.getStored() / mjpr;
    }

    @Override
    public long getCapacityAsLong() {
        long mjpr = mjPerRf();
        if (mjpr <= 0) return 0;
        return battery.getCapacity() / mjpr;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        if (amount <= 0) return 0;
        long mjpr = mjPerRf();
        if (mjpr <= 0) return 0;

        long mjToAdd = (long) amount * mjpr;
        long space = battery.getCapacity() - battery.getStored();
        if (space <= 0) return 0;

        long actualMj = Math.min(mjToAdd, space);
        int actualRf = (int) (actualMj / mjpr);
        if (actualRf <= 0) return 0;

        long finalMj = (long) actualRf * mjpr;

        updateSnapshots(transaction);
        battery.addPower(finalMj, false);

        return actualRf;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        if (amount <= 0) return 0;
        long mjpr = mjPerRf();
        if (mjpr <= 0) return 0;

        long mjToExtract = (long) amount * mjpr;

        updateSnapshots(transaction);

        long extracted = battery.extractPower(0, mjToExtract);
        int extractedRf = (int) (extracted / mjpr);

        if (extractedRf > 0) {

            long actualMj = (long) extractedRf * mjpr;
            long remainder = extracted - actualMj;
            if (remainder > 0) {
                battery.addPower(remainder, false);
            }
        } else if (extracted > 0) {

            battery.addPower(extracted, false);
        }

        return extractedRf;
    }
}
