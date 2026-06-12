package buildcraft.lib.fabric.transfer.fluid;


import buildcraft.lib.fabric.transfer.fluid.FluidStorageOps;
import buildcraft.lib.fabric.Mc26Compat;
import buildcraft.lib.fabric.transfer.BcTransfers;
import buildcraft.lib.fluid.identity.FluidIdentity;
import buildcraft.lib.fluid.interaction.FluidBlockPlacement;
import buildcraft.lib.fluid.interaction.FluidWorldFeedback;
import buildcraft.lib.fluid.stack.FluidStack;
import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public final class FluidStorageInteractions {
   private static final Logger LOGGER = LogUtils.getLogger();

   private FluidStorageInteractions() {
   }

   public static boolean isFluidContainerInHand(Player player, InteractionHand hand) {
      ContainerItemContext context = ContainerItemContext.forPlayerInteraction(player, hand);
      if (context.getItemVariant().isBlank()) {
         return false;
      }

      ItemStack held = context.getItemVariant().toStack((int)Math.min(context.getAmount(), 2147483647L));
      return !held.isEmpty() && ItemFluidLookup.storage(held, context) != null;
   }

   public static boolean onTankActivated(Player player, BlockPos pos, InteractionHand hand, Storage<FluidVariant> storage) {
      ItemStack held = player.getItemInHand(hand);
      if (held.isEmpty()) {
         return false;
      }

      if (!isFluidContainerInHand(player, hand)) {
         return false;
      }

      Level world = player.level();
      return world.isClientSide() ? true : interactWithFluidStorage(player, hand, pos, storage);
   }

   public static boolean interactWithFluidHandler(Player player, InteractionHand hand, Level level, BlockPos pos, @Nullable Direction side) {
      Preconditions.checkNotNull(level);
      Preconditions.checkNotNull(pos);
      Storage<FluidVariant> storage = BcTransfers.fluid(level, pos, side);
      return storage != null && interactWithFluidStorage(player, hand, pos, storage);
   }

   public static boolean interactWithFluidStorage(Player player, InteractionHand hand, @Nullable BlockPos pos, Storage<FluidVariant> tank) {
      ContainerItemContext handContext = ContainerItemContext.forPlayerInteraction(player, hand);
      if (handContext.getItemVariant().isBlank()) {
         return false;
      }

      ItemStack held = handContext.getItemVariant().toStack((int)Math.min(handContext.getAmount(), 2147483647L));
      Storage<FluidVariant> handStorage = ItemFluidLookup.storage(held, handContext);
      return handStorage == null
         ? false
         : moveStorageWithSound(tank, handStorage, player.level(), pos, player, true)
            || moveStorageWithSound(handStorage, tank, player.level(), pos, player, false);
   }

   public static FluidStack tryPickupFluid(
      @Nullable Storage<FluidVariant> destination, @Nullable Player player, Level level, BlockPos pos, @Nullable Direction side
   ) {
      if (destination == null) {
         return FluidStack.EMPTY;
      }

      BlockState state = level.getBlockState(pos);
      if (!(state.getBlock() instanceof BucketPickup bucketPickup)) {
         Storage<FluidVariant> blockStorage = BcTransfers.fluid(level, pos, state, null, side);
         if (blockStorage == null) {
            return FluidStack.EMPTY;
         }

         FluidStack moved = moveStorageWithSoundReturning(blockStorage, destination, level, pos, player, true);
         return moved != null ? moved : FluidStack.EMPTY;
      } else {
         Fluid fluid = level.getFluidState(pos).getType();
         if (fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
         }

         FluidStack resource = FluidIdentity.canonicalFluidStack(new FluidStack(fluid, 1));
         FluidVariant variant = FluidVariants.toVariant(resource);
         long bucketDroplets = FluidVariants.mbToDroplets(1000L);
         try (Transaction transaction = Transaction.openOuter()) {
            long inserted = destination.insert(variant, bucketDroplets, transaction);
            if (inserted != bucketDroplets || level.getFluidState(pos).getType() != fluid) {
               return FluidStack.EMPTY;
            }

            ItemStack pickedUpStack = bucketPickup.pickupBlock(player, level, pos, level.getBlockState(pos));
            if (pickedUpStack.getItem() instanceof BucketItem bucket) {
               Fluid bucketFluid = Mc26Compat.bucketFluid(bucket);
               FluidStack extracted = new FluidStack(bucketFluid, 1000);
               if (!FluidIdentity.areEquivalentFluidStacks(resource, extracted.copyWithAmount(1))) {
                  LOGGER.warn(
                     "Fluid removed without successfully being picked up. Fluid {} at {} in {} matched requested type, but after performing pickup was {}.",
                     new Object[]{BuiltInRegistries.FLUID.getKey(fluid), pos, level.dimension().identifier(), BuiltInRegistries.FLUID.getKey(bucketFluid)}
                  );
                  return FluidStack.EMPTY;
               }

               transaction.commit();
               FluidWorldFeedback.playAtBlockOrPlayer(resource, level, pos, player, true);
               return extracted;
            }

            if (!pickedUpStack.isEmpty()) {
               LOGGER.warn(
                  "Picked up stack is not a bucket. Fluid {} at {} in {} picked up as {}.",
                  new Object[]{BuiltInRegistries.FLUID.getKey(fluid), pos, level.dimension().identifier(), pickedUpStack}
               );
            }

            return FluidStack.EMPTY;
         }
      }
   }

   public static FluidStack tryPlaceFluid(@Nullable Storage<FluidVariant> source, @Nullable Player player, Level level, InteractionHand hand, BlockPos pos) {
      if (source == null) {
         return FluidStack.EMPTY;
      }

      for (StorageView<FluidVariant> view : source) {
         if (!view.isResourceBlank()) {
            FluidStack resource = FluidVariants.toStack((FluidVariant)view.getResource());
            try (Transaction transaction = Transaction.openOuter()) {
               long extracted = view.extract((FluidVariant)view.getResource(), FluidVariants.mbToDroplets(1000L), transaction);
               if (extracted == FluidVariants.mbToDroplets(1000L) && FluidBlockPlacement.tryPlaceFluid(resource, player, level, hand, pos)) {
                  transaction.commit();
                  return resource.copyWithAmount(1000);
               }
            }
         }
      }

      return FluidStack.EMPTY;
   }

   private static @Nullable FluidStack moveStorageWithSoundReturning(
      Storage<FluidVariant> from, Storage<FluidVariant> to, Level level, @Nullable BlockPos pos, @Nullable Player player, boolean pickup
   ) {
      if (player == null && pos == null) {
         throw new IllegalArgumentException("Either player or pos must be provided.");
      }

      FluidVariant movedVariant = FluidVariant.blank();
      long maxDroplets = 0L;

      for (StorageView<FluidVariant> view : from) {
         if (!view.isResourceBlank() && view.getAmount() > 0L) {
            movedVariant = (FluidVariant)view.getResource();
            maxDroplets = view.getAmount();
            break;
         }
      }

      if (!movedVariant.isBlank() && maxDroplets > 0L) {
         try (Transaction transaction = Transaction.openOuter()) {
            long moved = FluidStorageOps.move(from, to, maxDroplets, transaction);
            if (moved <= 0L) {
               return null;
            }

            transaction.commit();
            FluidStack movedStack = FluidVariants.toStack(movedVariant, moved);
            FluidWorldFeedback.playAtBlockOrPlayer(movedStack, level, pos, player, pickup);
            return movedStack;
         }
      } else {
         return null;
      }
   }

   private static boolean moveStorageWithSound(
      Storage<FluidVariant> from, Storage<FluidVariant> to, Level level, @Nullable BlockPos pos, @Nullable Player player, boolean pickup
   ) {
      if (player == null && pos == null) {
         throw new IllegalArgumentException("Either player or pos must be provided.");
      }

      FluidVariant soundVariant = FluidVariant.blank();

      for (StorageView<FluidVariant> view : from) {
         if (!view.isResourceBlank()) {
            soundVariant = (FluidVariant)view.getResource();
            break;
         }
      }

      try (Transaction transaction = Transaction.openOuter()) {
         long moved = FluidStorageOps.move(from, to, Long.MAX_VALUE, transaction);
         if (moved <= 0L) {
            return false;
         }

         transaction.commit();
         if (!soundVariant.isBlank()) {
            FluidWorldFeedback.playAtBlockOrPlayer(FluidVariants.toStack(soundVariant), level, pos, player, pickup);
         }

         return true;
      }
   }
}
