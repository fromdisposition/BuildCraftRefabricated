package buildcraft.lib.mj;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjPassiveProvider;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.Nullable;

public final class MjBlockCapabilities {
   private static final Map<BlockEntityType<?>, MjBlockCapabilities.SidedProvider<?, IMjReceiver>> RECEIVERS = new IdentityHashMap<>();
   private static final Map<BlockEntityType<?>, MjBlockCapabilities.SidedProvider<?, IMjConnector>> CONNECTORS = new IdentityHashMap<>();
   private static final Map<BlockEntityType<?>, MjBlockCapabilities.SidedProvider<?, IMjRedstoneReceiver>> REDSTONE_RECEIVERS = new IdentityHashMap<>();
   private static final Map<BlockEntityType<?>, MjBlockCapabilities.SidedProvider<?, IMjPassiveProvider>> PASSIVE_PROVIDERS = new IdentityHashMap<>();

   private MjBlockCapabilities() {
   }

   public static <BE extends BlockEntity> void registerReceiver(BlockEntityType<BE> type, MjBlockCapabilities.SidedProvider<BE, IMjReceiver> provider) {
      RECEIVERS.put(type, provider);
   }

   public static <BE extends BlockEntity> void registerConnector(BlockEntityType<BE> type, MjBlockCapabilities.SidedProvider<BE, IMjConnector> provider) {
      CONNECTORS.put(type, provider);
   }

   public static <BE extends BlockEntity> void registerRedstoneReceiver(
      BlockEntityType<BE> type, MjBlockCapabilities.SidedProvider<BE, IMjRedstoneReceiver> provider
   ) {
      REDSTONE_RECEIVERS.put(type, provider);
   }

   public static <BE extends BlockEntity> void registerPassiveProvider(
      BlockEntityType<BE> type, MjBlockCapabilities.SidedProvider<BE, IMjPassiveProvider> provider
   ) {
      PASSIVE_PROVIDERS.put(type, provider);
   }

   public static @Nullable IMjReceiver getReceiver(BlockEntity blockEntity, @Nullable Direction side) {
      return side == null ? null : getFromMap(RECEIVERS, blockEntity, side);
   }

   public static @Nullable IMjReceiver getReceiver(Level level, BlockPos pos, @Nullable Direction side) {
      BlockEntity blockEntity = level.getBlockEntity(pos);
      return blockEntity == null ? null : getReceiver(blockEntity, side);
   }

   public static @Nullable IMjConnector getConnector(BlockEntity blockEntity, @Nullable Direction side) {
      return side == null ? null : getFromMap(CONNECTORS, blockEntity, side);
   }

   public static @Nullable IMjConnector getConnector(Level level, BlockPos pos, @Nullable Direction side) {
      BlockEntity blockEntity = level.getBlockEntity(pos);
      return blockEntity == null ? null : getConnector(blockEntity, side);
   }

   public static @Nullable IMjPassiveProvider getPassiveProvider(BlockEntity blockEntity, @Nullable Direction side) {
      return side == null ? null : getFromMap(PASSIVE_PROVIDERS, blockEntity, side);
   }

   public static @Nullable IMjPassiveProvider getPassiveProvider(Level level, BlockPos pos, @Nullable Direction side) {
      BlockEntity blockEntity = level.getBlockEntity(pos);
      return blockEntity == null ? null : getPassiveProvider(blockEntity, side);
   }

   public static @Nullable IMjRedstoneReceiver getRedstoneReceiver(BlockEntity blockEntity, @Nullable Direction side) {
      return side == null ? null : getFromMap(REDSTONE_RECEIVERS, blockEntity, side);
   }

   public static @Nullable IMjRedstoneReceiver getRedstoneReceiver(Level level, BlockPos pos, @Nullable Direction side) {
      BlockEntity blockEntity = level.getBlockEntity(pos);
      return blockEntity == null ? null : getRedstoneReceiver(blockEntity, side);
   }

   public static <T> @Nullable T get(Object capability, BlockEntity blockEntity, @Nullable Direction side) {
      if (blockEntity == null || capability == null || side == null) {
         return null;
      } else if (capability == MjAPI.CAP_RECEIVER) {
         return (T)getReceiver(blockEntity, side);
      } else if (capability == MjAPI.CAP_CONNECTOR) {
         return (T)getConnector(blockEntity, side);
      } else if (capability == MjAPI.CAP_REDSTONE_RECEIVER) {
         return (T)getRedstoneReceiver(blockEntity, side);
      } else {
         return (T)(capability == MjAPI.CAP_PASSIVE_PROVIDER ? getPassiveProvider(blockEntity, side) : null);
      }
   }

   private static <T> @Nullable T getFromMap(
      Map<BlockEntityType<?>, MjBlockCapabilities.SidedProvider<?, T>> map, BlockEntity blockEntity, @Nullable Direction side
   ) {
      MjBlockCapabilities.SidedProvider<BlockEntity, T> provider = castProvider(map.get(blockEntity.getType()));
      return provider == null ? null : provider.get(blockEntity, side);
   }

   private static <T> MjBlockCapabilities.SidedProvider<BlockEntity, T> castProvider(MjBlockCapabilities.@Nullable SidedProvider<?, T> provider) {
      return (MjBlockCapabilities.SidedProvider<BlockEntity, T>)provider;
   }

   @FunctionalInterface
   public interface SidedProvider<BE extends BlockEntity, T> {
      @Nullable T get(BE var1, @Nullable Direction var2);
   }
}
