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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public final class BcRegistryUtil {
   private static final Map<Fluid, Item> FLUID_BUCKET_CACHE = new IdentityHashMap<>();
   private static final Identifier UNKNOWN_BIOME = Identifier.parse("minecraft:plains");

   private BcRegistryUtil() {
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
      return (holder.value()).isSame(Fluids.EMPTY);
   }

   public static boolean isChunkLoaded(Level level, BlockPos pos) {
      return level.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
   }

   public static boolean isChunkLoaded(ServerLevel level, int chunkX, int chunkZ) {
      return level.getChunkSource().hasChunk(chunkX, chunkZ);
   }

   public static DynamicOps<Tag> registryAwareOps() {
      // The client-level lookup lives in a client-only class: naming a client type here would crash the dedicated server's verifier.
      Provider client = null;
      if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
         client = buildcraft.lib.fabric.client.BcClientRegistryAccess.levelRegistryAccess();
      }
      return (client != null ? RegistryOps.create(NbtOps.INSTANCE, client) : NbtOps.INSTANCE);
   }

   public static DynamicOps<Tag> registryAwareOps(Level level) {
      return RegistryOps.create(NbtOps.INSTANCE, level.registryAccess());
   }
}
