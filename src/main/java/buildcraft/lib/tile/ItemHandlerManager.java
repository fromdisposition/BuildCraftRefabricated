package buildcraft.lib.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.lib.fabric.transfer.FabricItemStorageProvider;
import buildcraft.lib.fabric.transfer.ItemStorageResolve;
import buildcraft.lib.misc.INBTSerializable;
import buildcraft.lib.misc.InventoryUtil;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ItemHandlerManager implements INBTSerializable<CompoundTag> {
   public final StackChangeCallback callback;
   private final List<FabricItemStorageProvider> handlersToDrop = new ArrayList<>();
   private final Map<EnumPipePart, ItemHandlerManager.Wrapper> wrappers = new EnumMap<>(EnumPipePart.class);
   private final Map<String, INBTSerializable<CompoundTag>> handlers = new HashMap<>();

   public ItemHandlerManager(StackChangeCallback defaultCallback) {
      this.callback = defaultCallback;

      for (EnumPipePart part : EnumPipePart.VALUES) {
         this.wrappers.put(part, new ItemHandlerManager.Wrapper());
      }
   }

   public <T extends INBTSerializable<CompoundTag> & FabricItemStorageProvider> T addInvHandler(
      String key, T handler, ItemHandlerManager.EnumAccess access, EnumPipePart... parts
   ) {
      if (parts == null) {
         parts = new EnumPipePart[0];
      }

      FabricItemStorageProvider external = handler;
      if (access == ItemHandlerManager.EnumAccess.NONE || access == ItemHandlerManager.EnumAccess.PHANTOM) {
         external = null;
         if (parts.length > 0) {
            throw new IllegalArgumentException("Completely useless to not allow access to multiple sides! Just don't pass any sides!");
         }
      } else if (access == ItemHandlerManager.EnumAccess.EXTRACT) {
         external = new WrappedItemStorageExtract(handler);
      } else if (access == ItemHandlerManager.EnumAccess.INSERT) {
         external = new WrappedItemStorageInsert(handler);
      }

      if (external != null) {
         Set<EnumPipePart> visited = EnumSet.noneOf(EnumPipePart.class);

         for (EnumPipePart part : parts) {
            if (part == null) {
               part = EnumPipePart.CENTER;
            }

            if (visited.add(part)) {
               ItemHandlerManager.Wrapper wrapper = this.wrappers.get(part);
               wrapper.handlers.add(external);
               wrapper.genWrapper();
            }
         }
      }

      if (access != ItemHandlerManager.EnumAccess.PHANTOM) {
         this.handlersToDrop.add(handler);
      }

      this.handlers.put(key, handler);
      return handler;
   }

   public ItemHandlerSimple addInvHandler(String key, int size, ItemHandlerManager.EnumAccess access, EnumPipePart... parts) {
      ItemHandlerSimple handler = new ItemHandlerSimple(size, this.callback);
      return this.addInvHandler(key, handler, access, parts);
   }

   public ItemHandlerSimple addInvHandler(String key, int size, StackInsertionChecker checker, ItemHandlerManager.EnumAccess access, EnumPipePart... parts) {
      ItemHandlerSimple handler = new ItemHandlerSimple(size, this.callback);
      handler.setChecker(checker);
      return this.addInvHandler(key, handler, access, parts);
   }

   public ItemHandlerSimple addInvHandler(
      String key, int size, StackInsertionFunction insertionFunction, ItemHandlerManager.EnumAccess access, EnumPipePart... parts
   ) {
      ItemHandlerSimple handler = new ItemHandlerSimple(size, this.callback);
      handler.setInsertor(insertionFunction);
      return this.addInvHandler(key, handler, access, parts);
   }

   public ItemHandlerSimple addInvHandler(
      String key,
      int size,
      StackInsertionChecker checker,
      StackInsertionFunction insertionFunction,
      ItemHandlerManager.EnumAccess access,
      EnumPipePart... parts
   ) {
      ItemHandlerSimple handler = new ItemHandlerSimple(size, checker, insertionFunction, this.callback);
      return this.addInvHandler(key, handler, access, parts);
   }

   public void addDrops(NonNullList<ItemStack> toDrop) {
      for (FabricItemStorageProvider provider : this.handlersToDrop) {
         if (provider instanceof BcItemInventory inventory) {
            InventoryUtil.addAll(inventory, toDrop);
         }
      }
   }

   @Nullable
   public Storage<ItemVariant> getItemStorage(@Nullable Direction facing) {
      if (facing == null) {
         return null;
      }

      ItemHandlerManager.Wrapper wrapper = this.wrappers.get(EnumPipePart.fromFacing(facing));
      return ItemStorageResolve.of(wrapper.combined);
   }

   public CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();

      for (Entry<String, INBTSerializable<CompoundTag>> entry : this.handlers.entrySet()) {
         String key = entry.getKey();
         nbt.put(key, entry.getValue().serializeNBT());
      }

      return nbt;
   }

   public void deserializeNBT(CompoundTag nbt) {
      for (Entry<String, INBTSerializable<CompoundTag>> entry : this.handlers.entrySet()) {
         String key = entry.getKey();
         entry.getValue().deserializeNBT((CompoundTag)nbt.getCompound(key).orElseGet(CompoundTag::new));
      }
   }

   public enum EnumAccess {
      NONE,
      PHANTOM,
      INSERT,
      EXTRACT,
      BOTH;
   }

   private static class Wrapper {
      private final List<FabricItemStorageProvider> handlers = new ArrayList<>();
      private FabricItemStorageProvider combined = null;

      public void genWrapper() {
         if (this.handlers.size() == 1) {
            this.combined = this.handlers.get(0);
         } else {
            FabricItemStorageProvider[] arr = this.handlers.toArray(new FabricItemStorageProvider[0]);
            this.combined = new CombinedItemStorageProvider(arr);
         }
      }
   }
}
