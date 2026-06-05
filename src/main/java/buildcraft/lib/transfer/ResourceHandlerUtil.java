/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.redstone.Redstone;
import buildcraft.lib.transfer.resource.Resource;
import buildcraft.lib.transfer.resource.ResourceStack;
import buildcraft.lib.transfer.transaction.Transaction;
import buildcraft.lib.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class ResourceHandlerUtil {
    private ResourceHandlerUtil() {}

    public static boolean isEmpty(Resource resource, int amount) {
        return amount <= 0 || resource.isEmpty();
    }

    public static boolean isEmpty(ResourceHandler<? extends Resource> handler) {
        int size = handler.size();
        for (int i = 0; i < size; i++) {
            if (handler.getAmountAsLong(i) > 0) {
                return false;
            }
        }
        return true;
    }

    public static <T extends Resource> boolean isFull(ResourceHandler<T> handler) {
        int size = handler.size();
        for (int i = 0; i < size; i++) {
            if (handler.getAmountAsLong(i) < handler.getCapacityAsLong(i, handler.getResource(i))) {
                return false;
            }
        }
        return true;
    }

    public static <T extends Resource> boolean isValid(ResourceHandler<T> handler, T resource) {
        TransferPreconditions.checkNonEmpty(resource);

        int size = handler.size();
        for (int i = 0; i < size; i++) {
            if (handler.isValid(i, resource)) {
                return true;
            }
        }
        return false;
    }

    public static <T extends Resource> int getRedstoneSignalFromResourceHandler(ResourceHandler<T> handler) {
        float proportion = 0.0F;
        int sampleCount = 0;
        int size = handler.size();
        for (int index = 0; index < size; ++index) {
            long indexFill = handler.getAmountAsLong(index);
            if (indexFill > 0) {
                long capacity = handler.getCapacityAsLong(index, handler.getResource(index));
                if (capacity > 0) {

                    proportion += Math.min(1.0f, (float) indexFill / capacity);
                    sampleCount++;
                }
            }
        }

        if (sampleCount == 0) {
            return Redstone.SIGNAL_NONE;
        }

        proportion /= sampleCount;
        return Mth.lerpDiscrete(proportion, Redstone.SIGNAL_NONE, Redstone.SIGNAL_MAX);
    }

    public static <T extends Resource> int insertStacking(
            @Nullable ResourceHandler<T> handler,
            T resource,
            int amount,
            @Nullable TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (handler == null || amount == 0) return 0;

        try (Transaction tx = Transaction.open(transaction)) {
            int inserted = 0;
            int size = handler.size();

            for (int index = 0; index < size; index++) {
                if (!handler.getResource(index).isEmpty()) {
                    inserted += handler.insert(index, resource, amount - inserted, tx);
                    if (inserted == amount) break;
                }
            }

            if (inserted < amount) {
                for (int index = 0; index < size; index++) {
                    if (handler.getResource(index).isEmpty()) {
                        inserted += handler.insert(index, resource, amount - inserted, tx);
                        if (inserted == amount) break;
                    }
                }
            }

            tx.commit();
            return inserted;
        }
    }

    @Nullable
    public static <T extends Resource> ResourceStack<T> extractFirst(
            @Nullable ResourceHandler<T> handler,
            Predicate<T> filter,
            int amount,
            @Nullable TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        if (handler == null || amount == 0) return null;

        T resource = findExtractableResource(handler, filter, transaction);
        if (resource == null) return null;

        try (var tx = Transaction.open(transaction)) {
            int extracted = handler.extract(resource, amount, tx);
            if (extracted > 0) {
                tx.commit();
                return new ResourceStack<>(resource, extracted);
            } else {
                return null;
            }
        }
    }

    public static <T extends Resource> int move(
            @Nullable ResourceHandler<T> from,
            @Nullable ResourceHandler<T> to,
            Predicate<T> filter,
            int amount,
            @Nullable TransactionContext transaction) {
        return moveInternal(from, to, filter, amount, false, transaction);
    }

    public static <T extends Resource> int moveStacking(
            @Nullable ResourceHandler<T> from,
            @Nullable ResourceHandler<T> to,
            Predicate<T> filter,
            int amount,
            @Nullable TransactionContext transaction) {
        return moveInternal(from, to, filter, amount, true, transaction);
    }

    private static <T extends Resource> int moveInternal(
            @Nullable ResourceHandler<T> from,
            @Nullable ResourceHandler<T> to,
            Predicate<T> filter,
            int amount,
            boolean stacking,
            @Nullable TransactionContext transaction) {
        Objects.requireNonNull(filter, "Filter may not be null");
        TransferPreconditions.checkNonNegative(amount);
        if (from == null || to == null || amount == 0) return 0;

        try (Transaction subTransaction = Transaction.open(transaction)) {
            int totalMoved = 0;
            int size = from.size();

            for (int index = 0; index < size; ++index) {
                T fromResource = from.getResource(index);
                if (fromResource.isEmpty() || !filter.test(fromResource)) continue;

                int maxExtracted;
                try (Transaction simulatedExtract = Transaction.open(subTransaction)) {
                    maxExtracted = from.extract(index, fromResource, amount - totalMoved, simulatedExtract);
                }

                if (maxExtracted == 0) continue;

                try (Transaction transferTransaction = Transaction.open(subTransaction)) {

                    int inserted;
                    if (stacking) {
                        inserted = insertStacking(to, fromResource, maxExtracted, transferTransaction);
                    } else {
                        inserted = to.insert(fromResource, maxExtracted, transferTransaction);
                    }

                    if (inserted != from.extract(index, fromResource, inserted, transferTransaction))
                        continue;

                    totalMoved += inserted;
                    transferTransaction.commit();

                    if (totalMoved >= amount) break;
                }

            }

            subTransaction.commit();
            return totalMoved;
        } catch (Exception e) {
            CrashReport report = CrashReport.forThrowable(e, "Moving resources between resource handlers");

            report.addCategory("Move details")
                    .setDetail("Input", from::toString)
                    .setDetail("Output", to::toString)
                    .setDetail("Filter", filter::toString)
                    .setDetail("Amount", amount)
                    .setDetail("Transaction", transaction);
            throw new ReportedException(report);
        }
    }

    @Nullable
    public static <T extends Resource> ResourceStack<T> moveFirst(
            @Nullable ResourceHandler<T> from,
            @Nullable ResourceHandler<T> to,
            Predicate<T> filter,
            int amount,
            @Nullable TransactionContext transaction) {
        return moveFirstInternal(from, to, filter, amount, false, transaction);
    }

    @Nullable
    public static <T extends Resource> ResourceStack<T> moveFirstStacking(
            @Nullable ResourceHandler<T> from,
            @Nullable ResourceHandler<T> to,
            Predicate<T> filter,
            int amount,
            @Nullable TransactionContext transaction) {
        return moveFirstInternal(from, to, filter, amount, true, transaction);
    }

    @Nullable
    private static <T extends Resource> ResourceStack<T> moveFirstInternal(
            @Nullable ResourceHandler<T> from,
            @Nullable ResourceHandler<T> to,
            Predicate<T> filter,
            int amount,
            boolean stacking,
            @Nullable TransactionContext transaction) {
        Objects.requireNonNull(filter, "Filter may not be null");
        TransferPreconditions.checkNonNegative(amount);
        if (from == null || to == null || amount == 0)
            return null;

        try {
            int totalMoved = 0;
            T selectedResource = null;

            int size = from.size();

            for (int index = 0; index < size; ++index) {
                T fromResource = from.getResource(index);

                if (selectedResource == null && (fromResource.isEmpty() || !filter.test(fromResource))
                        || selectedResource != null && !areEquivalentResources(selectedResource, fromResource)) {
                    continue;
                }

                int extracted;
                try (Transaction simulatedExtractTransaction = Transaction.open(transaction)) {
                    extracted = from.extract(index, fromResource, amount - totalMoved, simulatedExtractTransaction);
                }

                if (extracted == 0) continue;

                try (Transaction transferTransaction = Transaction.open(transaction)) {

                    int inserted;
                    if (stacking) {
                        inserted = insertStacking(to, fromResource, extracted, transferTransaction);
                    } else {
                        inserted = to.insert(fromResource, extracted, transferTransaction);
                    }

                    if (inserted == 0) continue;

                    if (inserted != from.extract(index, fromResource, inserted, transferTransaction))
                        continue;

                    totalMoved += inserted;
                    transferTransaction.commit();
                    selectedResource = fromResource;

                    if (totalMoved >= amount) break;
                }

            }

            return totalMoved > 0 ? new ResourceStack<>(selectedResource, totalMoved) : null;
        } catch (Exception e) {
            CrashReport report = CrashReport.forThrowable(e, "Moving resources between storages");

            report.addCategory("Move details")
                    .setDetail("Input", from::toString)
                    .setDetail("Output", to::toString)
                    .setDetail("Filter", filter::toString)
                    .setDetail("Amount", amount)
                    .setDetail("Transaction", transaction);
            throw new ReportedException(report);
        }
    }

    private static boolean areEquivalentResources(Resource a, Resource b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a instanceof buildcraft.lib.transfer.fluid.FluidResource fa
                && b instanceof buildcraft.lib.transfer.fluid.FluidResource fb) {
            return buildcraft.lib.misc.FluidUtilBC.areEquivalentFluidResources(fa, fb);
        }
        return a.equals(b);
    }

    public static <T extends Resource> boolean contains(ResourceHandler<T> handler, T resource) {
        return indexOf(handler, resource) != -1;
    }

    public static <T extends Resource> int indexOf(ResourceHandler<T> handler, T resource) {
        TransferPreconditions.checkNonEmpty(resource);
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            if (resource.equals(handler.getResource(index)))
                return index;
        }
        return -1;
    }

    @Nullable
    public static <T extends Resource> T findExtractableResource(
            ResourceHandler<T> handler,
            Predicate<T> filter,
            @Nullable TransactionContext transaction) {
        try (Transaction temp = Transaction.open(transaction)) {
            int size = handler.size();
            for (int index = 0; index < size; index++) {
                T resource = handler.getResource(index);
                if (!resource.isEmpty() && filter.test(resource) && handler.extract(index, resource, handler.getAmountAsInt(index), temp) > 0) {
                    return resource;
                }
            }
            return null;
        }
    }
}
