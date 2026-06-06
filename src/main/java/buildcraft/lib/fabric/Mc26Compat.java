package buildcraft.lib.fabric;

import buildcraft.lib.fluids.FluidStack;
import com.mojang.serialization.DynamicOps;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class Mc26Compat {
   private Mc26Compat() {
   }

   public static boolean componentsPatchEmpty(ItemStack stack) {
      return stack.isEmpty() || stack.getComponentsPatch().isEmpty();
   }

   public static boolean componentsPatchEmpty(FluidStack stack) {
      return stack.isEmpty() || stack.getComponentsPatch().isEmpty();
   }

   public static boolean componentsPatchEmpty(DataComponentPatch patch) {
      return patch == null || patch.isEmpty();
   }

   public static void containerSetItem(Container container, int slot, ItemStack stack) {
      container.setItem(slot, stack);
   }

   public static Fluid bucketFluid(BucketItem bucket) {
      return bucket.getContent();
   }

   public static Item fluidBucketItem(Fluid fluid) {
      if (fluid.isSame(Fluids.EMPTY)) {
         return Items.BUCKET;
      }

      for (Item item : BuiltInRegistries.ITEM) {
         if (item instanceof BucketItem bucket && bucket.getContent().isSame(fluid)) {
            return item;
         }
      }

      return Items.AIR;
   }

   public static float composterValue(ItemStack stack) {
      return ComposterBlock.COMPOSTABLES.getFloat(stack.getItem());
   }

   public static Holder<Fluid> fluidHolder(Fluid fluid) {
      return BuiltInRegistries.FLUID.wrapAsHolder(fluid);
   }

   public static Holder<Item> itemHolder(Item item) {
      return BuiltInRegistries.ITEM.wrapAsHolder(item);
   }

   public static Holder<Fluid> emptyFluidHolder() {
      return fluidHolder(Fluids.EMPTY);
   }

   public static boolean isEmptyFluid(Holder<Fluid> holder) {
      return ((Fluid)holder.value()).isSame(Fluids.EMPTY);
   }

   public static boolean isChunkLoaded(Level level, BlockPos pos) {
      return level.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
   }

   public static boolean isChunkLoaded(ServerLevel level, int chunkX, int chunkZ) {
      return level.getChunkSource().hasChunk(chunkX, chunkZ);
   }

   public static DynamicOps<Tag> registryAwareOps() {
      Provider client = clientLevelRegistryAccess();
      return (DynamicOps<Tag>)(client != null ? RegistryOps.create(NbtOps.INSTANCE, client) : NbtOps.INSTANCE);
   }

   public static DynamicOps<Tag> registryAwareOps(Level level) {
      return RegistryOps.create(NbtOps.INSTANCE, level.registryAccess());
   }

   @Nullable
   private static Provider clientLevelRegistryAccess() {
      try {
         ClientLevel level = Minecraft.getInstance().level;
         return level == null ? null : level.registryAccess();
      } catch (Throwable ignored) {
         return null;
      }
   }
}
