package buildcraft.lib.tile.item;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import buildcraft.api.core.IStackFilter;
import buildcraft.lib.inventory.AbstractInvItemTransactor;
import buildcraft.lib.misc.INBTSerializable;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.item.StackInsertionFunction.InsertionResult;

import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.item.ItemResource;
import buildcraft.lib.transfer.transaction.TransactionContext;
import buildcraft.lib.transfer.transaction.Transaction;

import buildcraft.lib.transfer.transaction.SnapshotJournal;

@SuppressWarnings("this-escape")
public class ItemHandlerSimple extends AbstractInvItemTransactor
    implements IItemHandlerAdv, INBTSerializable<CompoundTag> {

    private final SnapshotJournal<ItemStack[]> journal = new SnapshotJournal<>() {
        @Override
        protected ItemStack[] createSnapshot() {
            ItemStack[] snap = new ItemStack[stacks.size()];
            for (int i = 0; i < snap.length; i++) {
                snap[i] = stacks.get(i).copy();
            }
            return snap;
        }

        @Override
        protected void revertToSnapshot(ItemStack[] snapshot) {
            for (int i = 0; i < snapshot.length; i++) {
                stacks.set(i, snapshot[i] != null ? snapshot[i] : StackUtil.EMPTY);
            }
        }
    };

    private StackInsertionChecker checker;
    private StackInsertionFunction inserter;

    @Nullable
    private StackChangeCallback callback;

    public final NonNullList<ItemStack> stacks;

    private int firstUsed = Integer.MAX_VALUE;

    private int slotCapacity = 64;

    public ItemHandlerSimple(int size) {
        this(size, (slot, stack) -> true, StackInsertionFunction.getDefaultInserter(), null);
    }

    public ItemHandlerSimple(int size, int maxStackSize) {
        this(size);
        setLimitedInsertor(maxStackSize);
        this.slotCapacity = maxStackSize;
    }

    public ItemHandlerSimple(int size, @Nullable StackChangeCallback callback) {
        this(size, (slot, stack) -> true, StackInsertionFunction.getDefaultInserter(), callback);
    }

    public ItemHandlerSimple(int size, StackInsertionChecker checker, StackInsertionFunction insertionFunction,
        @Nullable StackChangeCallback callback) {
        stacks = NonNullList.withSize(size, StackUtil.EMPTY);
        this.checker = checker;
        this.inserter = insertionFunction;
        this.callback = callback;
    }

    public void setChecker(StackInsertionChecker checker) {
        this.checker = checker;
    }

    public void setInsertor(StackInsertionFunction insertor) {
        this.inserter = insertor;
    }

    public void setLimitedInsertor(int maxStackSize) {
        setInsertor(StackInsertionFunction.getInsertionFunction(maxStackSize));
        this.slotCapacity = maxStackSize;
    }

    public void setCallback(StackChangeCallback callback) {
        this.callback = callback;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        ListTag list = new ListTag();
        for (ItemStack stack : stacks) {
            CompoundTag itemNbt = new CompoundTag();
            if (!stack.isEmpty()) {

                ItemStack.CODEC.encodeStart(buildcraft.lib.misc.NBTUtilBC.registryAwareOps(), stack)
                        .resultOrPartial()
                        .ifPresent(payload -> itemNbt.put("stack", payload));
            }
            list.add(itemNbt);
        }
        nbt.put("items", list);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag list = nbt.getList("items").orElseGet(ListTag::new);
        for (int i = 0; i < list.size() && i < size(); i++) {
            CompoundTag itemNbt = list.getCompound(i).orElseGet(CompoundTag::new);
            ItemStack stack = ItemStack.EMPTY;
            Tag stackPayload = itemNbt.get("stack");
            if (stackPayload != null) {
                stack = ItemStack.CODEC.parse(buildcraft.lib.misc.NBTUtilBC.registryAwareOps(), stackPayload)
                        .resultOrPartial()
                        .orElse(ItemStack.EMPTY);
            } else if (itemNbt.contains("id")) {

                String idStr = itemNbt.getString("id").orElse("");
                net.minecraft.resources.Identifier id = net.minecraft.resources.Identifier.tryParse(idStr);
                if (id != null) {
                    net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(id);
                    int count = itemNbt.getInt("count").orElse(1);
                    if (item != null && item != net.minecraft.world.item.Items.AIR) {
                        stack = new ItemStack(item, count);
                    }
                }
            }
            setStackInternal(i, stack);
        }
        for (int i = list.size(); i < size(); i++) {
            setStackInternal(i, StackUtil.EMPTY);
        }
    }

    @Override
    public int size() {
        return stacks.size();
    }

    public int getSlots() {
        return size();
    }

    public net.minecraft.world.item.ItemStack getStackInSlot(int slot) {
        if (badSlotIndex(slot)) return net.minecraft.world.item.ItemStack.EMPTY;
        return stacks.get(slot);
    }

    public net.minecraft.world.item.ItemStack insertItem(int slot, @Nonnull net.minecraft.world.item.ItemStack stack, boolean simulate) {
        return insert(slot, stack, simulate);
    }

    public net.minecraft.world.item.ItemStack extractItem(int slot, int amount, boolean simulate) {
        return extract(slot, s -> true, 1, amount, simulate);
    }

    public int getSlotLimit(int slot) {
        return 64;
    }

    private boolean badSlotIndex(int slot) {
        return slot < 0 || slot >= stacks.size();
    }

    @Override
    protected boolean isEmpty(int slot) {
        if (badSlotIndex(slot)) return true;
        return stacks.get(slot).isEmpty();
    }

    @Override
    public ItemResource getResource(int index) {
        return badSlotIndex(index) ? ItemResource.EMPTY : ItemResource.of(stacks.get(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return badSlotIndex(index) ? 0 : stacks.get(index).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return slotCapacity;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return canSet(index, resource.toStack());
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
        if (badSlotIndex(index) || amount <= 0 || resource.isEmpty()) return 0;
        ItemStack stack = resource.toStack(amount);

        ItemStack current = stacks.get(index);
        if (!canSet(index, stack) || !canSet(index, current)) return 0;

        InsertionResult result = inserter.modifyForInsertion(index, asValid(current.copy()), asValid(stack.copy()));
        if (!canSet(index, result.toSet)) {
            CrashReport report = new CrashReport("Inserting an item (buildcraft:ItemHandlerSimple)",
                new IllegalStateException("Conflicting Insertion!"));
            CrashReportCategory cat = report.addCategory("Inventory details");
            cat.setDetail("Existing Item", current.toString());
            cat.setDetail("Inserting Item", stack.toString());
            cat.setDetail("To Set", result.toSet.toString());
            cat.setDetail("To Return", result.toReturn.toString());
            cat.setDetail("Slot", String.valueOf(index));
            throw new RuntimeException("Conflicting Insertion! See log for details.");
        }

        int inserted = amount - result.toReturn.getCount();
        if (inserted > 0) {
            if (tx != null) {
                journal.updateSnapshots(tx);
            }
            setStackInternal(index, result.toSet);
            if (callback != null) {
                callback.onStackChange(this, index, current, result.toSet);
            }
        }
        return inserted;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
        if (badSlotIndex(index) || amount <= 0 || resource.isEmpty()) return 0;
        ItemStack current = stacks.get(index);
        if (current.isEmpty() || !ItemResource.of(current).equals(resource)) return 0;

        int toExtract = Math.min(amount, current.getCount());
        if (toExtract > 0) {
            ItemStack before = current.copy();
            ItemStack after = current.copy();
            after.shrink(toExtract);
            if (after.getCount() <= 0) after = StackUtil.EMPTY;

            if (tx != null) {
                journal.updateSnapshots(tx);
            }
            setStackInternal(index, after);
            if (callback != null) {
                callback.onStackChange(this, index, before, after);
            }
        }
        return toExtract;
    }

    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (badSlotIndex(slot)) {
            throw new IndexOutOfBoundsException("Slot index out of range: " + slot);
        }
        ItemStack before = stacks.get(slot);
        setStackInternal(slot, stack);
        if (callback != null) {
            callback.onStackChange(this, slot, before, asValid(stack));
        }
    }

    @Override
    public final boolean canSet(int slot, @Nonnull ItemStack stack) {
        ItemStack copied = asValid(stack);
        if (copied.isEmpty()) return true;
        return checker.canSet(slot, copied);
    }

    private void setStackInternal(int slot, @Nonnull ItemStack stack) {
        stacks.set(slot, asValid(stack));
        if (stack.isEmpty() && firstUsed == slot) {
            for (int s = firstUsed; s < size(); s++) {
                if (!stacks.get(s).isEmpty()) {
                    firstUsed = s;
                    break;
                }
            }
            if (firstUsed == slot) {
                firstUsed = Integer.MAX_VALUE;
            }
        } else if (!stack.isEmpty() && firstUsed > slot) {
            firstUsed = slot;
        }
    }

    @Override
    protected ItemStack insert(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        try (Transaction tx = Transaction.openRoot()) {
            int inserted = insert(slot, ItemResource.of(stack), stack.getCount(), tx);
            if (!simulate) tx.commit();
            return stack.copyWithCount(stack.getCount() - inserted);
        }
    }

    @Override
    protected ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate) {
        if (badSlotIndex(slot) || max < min) return StackUtil.EMPTY;
        ItemStack current = stacks.get(slot);
        if (current.isEmpty() || current.getCount() < min || !filter.matches(asValid(current))) return StackUtil.EMPTY;
        try (Transaction tx = Transaction.openRoot()) {
            int toExtract = Math.min(max, current.getCount());
            int ex = extract(slot, ItemResource.of(current), toExtract, tx);
            if (!simulate) tx.commit();
            return current.copyWithCount(ex);
        }
    }

    @Override
    public String toString() {
        return "ItemHandlerSimple " + stacks;
    }
}
