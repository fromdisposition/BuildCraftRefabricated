/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.energy;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.redstone.Redstone;
import buildcraft.lib.transfer.TransferPreconditions;
import buildcraft.lib.transfer.transaction.Transaction;
import buildcraft.lib.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class EnergyHandlerUtil {

    public static boolean isFull(EnergyHandler handler) {
        return handler.getAmountAsLong() >= handler.getCapacityAsLong();
    }

    public static int getRedstoneSignalFromEnergyHandler(EnergyHandler handler) {
        long amount = handler.getAmountAsLong();
        if (amount == 0) {
            return Redstone.SIGNAL_NONE;
        }
        long capacity = handler.getCapacityAsLong();
        if (capacity == 0) {
            return Redstone.SIGNAL_NONE;
        }
        return Mth.lerpDiscrete(

                Math.min(1.0f, (float) amount / capacity),
                Redstone.SIGNAL_NONE, Redstone.SIGNAL_MAX);
    }

    public static int move(
            @Nullable EnergyHandler from, @Nullable EnergyHandler to,
            int amount,
            @Nullable TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        if (from == null || to == null || amount == 0) return 0;

        try (Transaction subTransaction = Transaction.open(transaction)) {
            int maxExtracted;
            try (Transaction simulatedExtract = Transaction.open(subTransaction)) {
                maxExtracted = from.extract(amount, simulatedExtract);
            }

            if (maxExtracted == 0) return 0;

            int inserted = to.insert(maxExtracted, subTransaction);

            if (inserted != from.extract(inserted, subTransaction)) {
                return 0;
            }

            subTransaction.commit();
            return inserted;
        } catch (Exception e) {
            CrashReport report = CrashReport.forThrowable(e, "Moving energy between handlers");

            report.addCategory("Move details")
                    .setDetail("Input", from::toString)
                    .setDetail("Output", to::toString)
                    .setDetail("Amount", amount)
                    .setDetail("Transaction", transaction);
            throw new ReportedException(report);
        }
    }

    private EnergyHandlerUtil() {}
}
