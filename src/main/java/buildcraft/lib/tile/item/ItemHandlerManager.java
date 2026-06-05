package buildcraft.lib.tile.item;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;

import buildcraft.lib.misc.INBTSerializable;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.item.ItemResource;

import buildcraft.api.core.EnumPipePart;

import buildcraft.lib.misc.InventoryUtil;

public class ItemHandlerManager implements INBTSerializable<CompoundTag> {
    public enum EnumAccess {
        NONE,
        PHANTOM,
        INSERT,
        EXTRACT,
        BOTH
    }

    public final StackChangeCallback callback;
    private final List<ResourceHandler<ItemResource>> handlersToDrop = new ArrayList<>();
    private final Map<EnumPipePart, Wrapper> wrappers = new EnumMap<>(EnumPipePart.class);
    private final Map<String, INBTSerializable<CompoundTag>> handlers = new HashMap<>();

    public ItemHandlerManager(StackChangeCallback defaultCallback) {
        this.callback = defaultCallback;
        for (EnumPipePart part : EnumPipePart.VALUES) {
            wrappers.put(part, new Wrapper());
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends INBTSerializable<CompoundTag> & ResourceHandler<ItemResource>> T addInvHandler(String key, T handler,
        EnumAccess access, EnumPipePart... parts) {
        if (parts == null) {
            parts = new EnumPipePart[0];
        }
        ResourceHandler<ItemResource> external = handler;
        if (access == EnumAccess.NONE || access == EnumAccess.PHANTOM) {
            external = null;
            if (parts.length > 0) {
                throw new IllegalArgumentException(
                    "Completely useless to not allow access to multiple sides! Just don't pass any sides!");
            }
        } else if (access == EnumAccess.EXTRACT) {
            external = new WrappedItemHandlerExtract(handler);
        } else if (access == EnumAccess.INSERT) {
            external = new WrappedItemHandlerInsert(handler);
        }

        if (external != null) {
            Set<EnumPipePart> visited = EnumSet.noneOf(EnumPipePart.class);
            for (EnumPipePart part : parts) {
                if (part == null) part = EnumPipePart.CENTER;
                if (visited.add(part)) {
                    Wrapper wrapper = wrappers.get(part);
                    wrapper.handlers.add(external);
                    wrapper.genWrapper();
                }
            }
        }
        if (access != EnumAccess.PHANTOM) {
            handlersToDrop.add(handler);
        }
        handlers.put(key, handler);
        return handler;
    }

    public ItemHandlerSimple addInvHandler(String key, int size, EnumAccess access, EnumPipePart... parts) {
        ItemHandlerSimple handler = new ItemHandlerSimple(size, callback);
        return addInvHandler(key, handler, access, parts);
    }

    public ItemHandlerSimple addInvHandler(String key, int size, StackInsertionChecker checker, EnumAccess access,
        EnumPipePart... parts) {
        ItemHandlerSimple handler = new ItemHandlerSimple(size, callback);
        handler.setChecker(checker);
        return addInvHandler(key, handler, access, parts);
    }

    public ItemHandlerSimple addInvHandler(String key, int size, StackInsertionFunction insertionFunction,
        EnumAccess access, EnumPipePart... parts) {
        ItemHandlerSimple handler = new ItemHandlerSimple(size, callback);
        handler.setInsertor(insertionFunction);
        return addInvHandler(key, handler, access, parts);
    }

    public ItemHandlerSimple addInvHandler(String key, int size, StackInsertionChecker checker,
        StackInsertionFunction insertionFunction, EnumAccess access, EnumPipePart... parts) {
        ItemHandlerSimple handler = new ItemHandlerSimple(size, checker, insertionFunction, callback);
        return addInvHandler(key, handler, access, parts);
    }

    public void addDrops(NonNullList<ItemStack> toDrop) {
        for (ResourceHandler<ItemResource> itemHandler : handlersToDrop) {
            InventoryUtil.addAll(itemHandler, toDrop);
        }
    }

    public ResourceHandler<ItemResource> getItemHandler(Direction facing) {
        Wrapper wrapper = wrappers.get(EnumPipePart.fromFacing(facing));
        return wrapper.combined;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        for (Entry<String, INBTSerializable<CompoundTag>> entry : handlers.entrySet()) {
            String key = entry.getKey();
            nbt.put(key, entry.getValue().serializeNBT());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        for (Entry<String, INBTSerializable<CompoundTag>> entry : handlers.entrySet()) {
            String key = entry.getKey();
            entry.getValue().deserializeNBT(nbt.getCompound(key).orElseGet(CompoundTag::new));
        }
    }

    private static class Wrapper {
        private final List<ResourceHandler<ItemResource>> handlers = new ArrayList<>();
        private ResourceHandler<ItemResource> combined = null;

        public void genWrapper() {
            if (handlers.size() == 1) {

                combined = handlers.get(0);
                return;
            }
            @SuppressWarnings({"unchecked", "rawtypes"})
            ResourceHandler<ItemResource>[] arr = new ResourceHandler[handlers.size()];
            arr = handlers.toArray(arr);
            combined = new CombinedItemHandlerWrapper(arr);
        }
    }
}
