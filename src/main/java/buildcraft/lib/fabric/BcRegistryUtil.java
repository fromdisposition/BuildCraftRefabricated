package buildcraft.lib.fabric;

import com.mojang.serialization.DynamicOps;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public final class BcRegistryUtil {
   private static final Map<Fluid, Item> FLUID_BUCKET_CACHE = new IdentityHashMap<>();
   private static final Identifier UNKNOWN_BIOME = Identifier.parse("minecraft:plains");

   private BcRegistryUtil() {
   }

   public static Identifier biomeId(Holder<Biome> biome) {
      return registryId(biome).orElse(UNKNOWN_BIOME);
   }

   public static Optional<Identifier> registryId(Holder<?> holder) {
      return holder.unwrapKey().map(buildcraft.lib.misc.RegistryKeyUtil::id);
   }

   @Nullable
   public static Fluid getFluid(Identifier id) {
      //? if >= 1.21.10 {
      return BuiltInRegistries.FLUID.get(id).map(Holder::value).orElse(null);
      //?} else {
      /*return BuiltInRegistries.FLUID.get(id);
      *///?}
   }

   @Nullable
   public static Item getItem(Identifier id) {
      //? if >= 1.21.10 {
      return BuiltInRegistries.ITEM.get(id).map(Holder::value).orElse(null);
      //?} else {
      /*return BuiltInRegistries.ITEM.get(id);
      *///?}
   }

   /** A HolderGetter view of the block registry (Registry is-a HolderGetter on 1.21.5+; via asLookup on 1.21.1). */
   public static net.minecraft.core.HolderGetter<Block> blockLookup() {
      //? if >= 1.21.10 {
      return BuiltInRegistries.BLOCK;
      //?} else {
      /*return BuiltInRegistries.BLOCK.asLookup();
      *///?}
   }

   @Nullable
   public static Block getBlock(Identifier id) {
      //? if >= 1.21.10 {
      return BuiltInRegistries.BLOCK.get(id).map(Holder::value).orElse(null);
      //?} else {
      /*return BuiltInRegistries.BLOCK.get(id);
      *///?}
   }

   public static Fluid bucketFluid(BucketItem bucket) {
      //? if >= 1.21.10 {
      return bucket.getContent();
      //?} else {
      /*return ((buildcraft.lib.fabric.mixin.BucketItemAccessor) (Object) bucket).buildcraft$getContent();
      *///?}
   }

   public static Item fluidBucketItem(Fluid fluid) {
      if (fluid.isSame(Fluids.EMPTY)) {
         return Items.BUCKET;
      }

      // No lock-free fast path: IdentityHashMap reads racing a rehashing put can corrupt the map. The lookup is
      // rare enough that taking the lock every time is the simple correct answer.
      synchronized (FLUID_BUCKET_CACHE) {
         Item cached = FLUID_BUCKET_CACHE.get(fluid);
         if (cached != null) {
            return cached;
         }

         for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof BucketItem bucket && bucketFluid(bucket).isSame(fluid)) {
               FLUID_BUCKET_CACHE.put(fluid, item);
               return item;
            }
         }

         FLUID_BUCKET_CACHE.put(fluid, Items.AIR);
         return Items.AIR;
      }
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
      // Client-level registry access lives in a client-only helper so this common class never names a
      // client type (the verifier would otherwise resolve it and crash a dedicated server). Guard ensures
      // the helper is never loaded outside the client environment.
      Provider client = null;
      if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
         client = buildcraft.lib.fabric.client.BcClientRegistryAccess.levelRegistryAccess();
      }
      return (DynamicOps<Tag>)(client != null ? RegistryOps.create(NbtOps.INSTANCE, client) : NbtOps.INSTANCE);
   }

   public static DynamicOps<Tag> registryAwareOps(Level level) {
      return RegistryOps.create(NbtOps.INSTANCE, level.registryAccess());
   }
}
