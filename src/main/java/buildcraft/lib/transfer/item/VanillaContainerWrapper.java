/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.item;

import com.google.common.collect.MapMaker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.transaction.RootCommitJournal;
import buildcraft.lib.transfer.transaction.TransactionContext;

public class VanillaContainerWrapper implements ResourceHandler<ItemResource> {

    private static final Map<Container, VanillaContainerWrapper> wrappers = new MapMaker().weakKeys().weakValues().makeMap();

    public static ResourceHandler<ItemResource> of(Container container) {

        return internalOf(container);
    }

    static VanillaContainerWrapper internalOf(Container container) {
        VanillaContainerWrapper wrapper = wrappers.computeIfAbsent(container, cont -> {
            if (cont instanceof Inventory inventory) {
                return new PlayerInventoryWrapper(inventory);
            } else {
                return new VanillaContainerWrapper(cont);
            }
        });
        wrapper.resize();
        return wrapper;
    }

    private final Container container;
    int size;
    final List<SlotWrapper> slotWrappers = new ArrayList<>();
    private final RootCommitJournal setChangedJournal;

    VanillaContainerWrapper(Container container) {
        this.container = container;
        this.setChangedJournal = new RootCommitJournal(this::onRootCommit);
    }

    void resize() {
        size = container.getContainerSize();
        while (slotWrappers.size() < size) {
            slotWrappers.add(new SlotWrapper(slotWrappers.size()));
        }
    }

    SlotWrapper getSlotWrapper(int index) {
        Objects.checkIndex(index, size());
        return slotWrappers.get(index);
    }

    void onRootCommit() {
        container.setChanged();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return getSlotWrapper(index).insert(0, resource, amount, transaction);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return getSlotWrapper(index).extract(0, resource, amount, transaction);
    }

    @Override
    public ItemResource getResource(int index) {
        return getSlotWrapper(index).getResource(0);
    }

    @Override
    public long getAmountAsLong(int index) {
        return getSlotWrapper(index).getAmountAsLong(0);
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return getSlotWrapper(index).getCapacityAsLong(0, resource);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return getSlotWrapper(index).isValid(0, resource);
    }

    @Override
    public String toString() {
        return "VanillaContainerWrapper{container=%s}".formatted(container);
    }

    class SlotWrapper extends ItemStackResourceHandler {
        private final int index;

        SlotWrapper(int index) {
            this.index = index;
        }

        @Override
        protected ItemStack getStack() {
            return container.getItem(index);
        }

        @Override
        protected void setStack(ItemStack item) {

            buildcraft.lib.fabric.Mc26Compat.containerSetItem(container, index, item);
        }

        @Override
        protected boolean isValid(ItemResource resource) {
            return container.canPlaceItem(index, resource.toStack());
        }

        @Override
        protected int getCapacity(ItemResource resource) {

            if (index ==  1 && resource.is(Items.BUCKET) && container instanceof AbstractFurnaceBlockEntity) {
                return 1;
            }

            if (index < 3 && container instanceof BrewingStandBlockEntity) {
                return 1;
            }

            return resource.isEmpty() ? container.getMaxStackSize() : container.getMaxStackSize(resource.toStack());
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return super.insert(index, resource, amount, transaction);
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return super.extract(index, resource, amount, transaction);
        }

        @Override
        public void updateSnapshots(TransactionContext transaction) {
            super.updateSnapshots(transaction);
            setChangedJournal.updateSnapshots(transaction);

            if (container instanceof ChestBlockEntity chest && chest.getBlockState().getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                BlockPos otherChestPos = chest.getBlockPos().relative(ChestBlock.getConnectedDirection(chest.getBlockState()));

                if (chest.getLevel().getBlockEntity(otherChestPos) instanceof ChestBlockEntity otherChest) {
                    VanillaContainerWrapper.internalOf(otherChest).setChangedJournal.updateSnapshots(transaction);
                }
            }
        }

        @Override
        protected void onRootCommit(ItemStack original) {

            ItemStack currentStack = getStack();

            if (!original.isEmpty() && original.getItem() == currentStack.getItem()) {

                ((PatchedDataComponentMap) original.getComponents()).restorePatch(currentStack.getComponentsPatch());
                original.setCount(currentStack.getCount());
                setStack(original);
            } else {

                original.setCount(0);
            }
        }

        @Override
        public String toString() {
            return "vanilla container wrapper[container=" + container + ",slot=" + index + "]";
        }
    }
}
