package buildcraft.lib.transfer.fluid;

import buildcraft.lib.common.SoundActions;
import buildcraft.lib.fabric.Mc26Compat;
import buildcraft.lib.fabric.transfer.FluidStorageOps;
import buildcraft.lib.fabric.transfer.TriggerTransferAccess;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.fluids.FluidType;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.transfer.fabric.TransferConvert;
import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public final class FluidUtil {
   private static final Logger LOGGER = LogUtils.getLogger();

   private FluidUtil() {
   }

   public static FluidStack getFirstStackContained(ItemStack stack) {
      if (stack.isEmpty()) {
         return FluidStack.EMPTY;
      }

      Storage<FluidVariant> storage = TriggerTransferAccess.itemFluidStorage(stack);
      if (storage == null) {
         return FluidStack.EMPTY;
      }

      for (StorageView<FluidVariant> view : storage) {
         if (!view.isResourceBlank() && view.getAmount() > 0L) {
            return TransferConvert.toFluidStack((FluidVariant)view.getResource(), view.getAmount());
         }
      }

      return FluidStack.EMPTY;
   }

   public static boolean interactWithFluidHandler(Player player, InteractionHand hand, Level level, BlockPos pos, @Nullable Direction side) {
      Preconditions.checkNotNull(level);
      Preconditions.checkNotNull(pos);
      Storage<FluidVariant> storage = TriggerTransferAccess.blockFluidStorage(level, pos, side);
      return storage != null && interactWithFluidStorage(player, hand, pos, storage);
   }

   public static boolean interactWithFluidStorage(Player player, InteractionHand hand, @Nullable BlockPos pos, Storage<FluidVariant> tank) {
      ContainerItemContext handContext = ContainerItemContext.forPlayerInteraction(player, hand);
      if (handContext.getItemVariant().isBlank()) {
         return false;
      }

      ItemStack held = handContext.getItemVariant().toStack((int)Math.min(handContext.getAmount(), 2147483647L));
      Storage<FluidVariant> handStorage = TriggerTransferAccess.itemFluidStorage(held, handContext);
      return handStorage == null
         ? false
         : moveStorageWithSound(tank, handStorage, player.level(), pos, player, true)
            || moveStorageWithSound(handStorage, tank, player.level(), pos, player, false);
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
         Transaction tx = Transaction.openOuter();

         FluidStack movedStack;
         label73: {
            FluidStack var13;
            try {
               long moved = FluidStorageOps.move(from, to, maxDroplets, tx);
               if (moved <= 0L) {
                  movedStack = null;
                  break label73;
               }

               tx.commit();
               movedStack = TransferConvert.toFluidStack(movedVariant, moved);
               playSoundAndGameEvent(movedStack, level, pos, player, pickup);
               var13 = movedStack;
            } catch (Throwable var15) {
               if (tx != null) {
                  try {
                     tx.close();
                  } catch (Throwable var14) {
                     var15.addSuppressed(var14);
                  }
               }

               throw var15;
            }

            if (tx != null) {
               tx.close();
            }

            return var13;
         }

         if (tx != null) {
            tx.close();
         }

         return movedStack;
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

      Transaction tx = Transaction.openOuter();

      boolean var15;
      label67: {
         try {
            long moved = FluidStorageOps.move(from, to, Long.MAX_VALUE, tx);
            if (moved <= 0L) {
               var15 = false;
               break label67;
            }

            tx.commit();
            if (!soundVariant.isBlank()) {
               playSoundAndGameEvent(TransferConvert.toFluidStack(soundVariant), level, pos, player, pickup);
            }

            var15 = true;
         } catch (Throwable var12) {
            if (tx != null) {
               try {
                  tx.close();
               } catch (Throwable var11) {
                  var12.addSuppressed(var11);
               }
            }

            throw var12;
         }

         if (tx != null) {
            tx.close();
         }

         return var15;
      }

      if (tx != null) {
         tx.close();
      }

      return var15;
   }

   private static int saturateMb(long millibuckets) {
      return millibuckets > 2147483647L ? Integer.MAX_VALUE : (int)millibuckets;
   }

   private static void playSoundAndGameEvent(FluidStack stack, Level level, @Nullable BlockPos blockPos, @Nullable Player player, boolean pickup) {
      if (player == null && blockPos == null) {
         throw new IllegalArgumentException("Either player or blockPos must be provided.");
      }

      Vec3 position = blockPos != null ? Vec3.atCenterOf(blockPos) : new Vec3(player.getX(), player.getY() + 0.5, player.getZ());
      triggerSoundAndGameEvent(stack, level, position, player, pickup);
   }

   public static void triggerSoundAndGameEvent(FluidStack stack, Level level, Vec3 position, @Nullable Player player, boolean pickup) {
      FluidType fluidType = stack.getFluidType();
      SoundEvent soundEvent = fluidType.getSound(pickup ? SoundActions.BUCKET_FILL : SoundActions.BUCKET_EMPTY);
      if (soundEvent != null) {
         level.playSound(null, position.x, position.y, position.z, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
      }

      level.gameEvent(player, pickup ? GameEvent.FLUID_PICKUP : GameEvent.FLUID_PLACE, position);
   }

   public static FluidStack tryPickupFluid(
      @Nullable Storage<FluidVariant> destination, @Nullable Player player, Level level, BlockPos pos, @Nullable Direction side
   ) {
      if (destination == null) {
         return FluidStack.EMPTY;
      }

      BlockState state = level.getBlockState(pos);
      if (!(state.getBlock() instanceof BucketPickup bucketPickup)) {
         Storage<FluidVariant> blockStorage = TriggerTransferAccess.blockFluidStorage(level, pos, state, null, side);
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

         FluidStack resource = FluidUtilBC.canonicalFluidStack(new FluidStack(fluid, 1));
         FluidVariant variant = TransferConvert.toVariant(resource);
         long bucketDroplets = TransferConvert.mbToDroplets(1000L);
         Transaction tx = Transaction.openOuter();

         FluidStack var26;
         label107: {
            label108: {
               FluidStack var29;
               label109: {
                  label110: {
                     FluidStack var27;
                     try {
                        long inserted = destination.insert(variant, bucketDroplets, tx);
                        if (inserted != bucketDroplets) {
                           var26 = FluidStack.EMPTY;
                           break label107;
                        }

                        if (level.getFluidState(pos).getType() != fluid) {
                           var26 = FluidStack.EMPTY;
                           break label108;
                        }

                        ItemStack pickedUpStack = bucketPickup.pickupBlock(player, level, pos, level.getBlockState(pos));
                        if (pickedUpStack.getItem() instanceof BucketItem bucket) {
                           Fluid bucketFluid = Mc26Compat.bucketFluid(bucket);
                           FluidStack extracted = new FluidStack(bucketFluid, 1000);
                           if (!FluidUtilBC.areEquivalentFluidStacks(resource, extracted.copyWithAmount(1))) {
                              LOGGER.warn(
                                 "Fluid removed without successfully being picked up. Fluid {} at {} in {} matched requested type, but after performing pickup was {}.",
                                 new Object[]{
                                    BuiltInRegistries.FLUID.getKey(fluid), pos, level.dimension().identifier(), BuiltInRegistries.FLUID.getKey(bucketFluid)
                                 }
                              );
                              var29 = FluidStack.EMPTY;
                              break label109;
                           }

                           tx.commit();
                           playSoundAndGameEvent(resource, level, pos, player, true);
                           var29 = extracted;
                           break label110;
                        }

                        if (!pickedUpStack.isEmpty()) {
                           LOGGER.warn(
                              "Picked up stack is not a bucket. Fluid {} at {} in {} picked up as {}.",
                              new Object[]{BuiltInRegistries.FLUID.getKey(fluid), pos, level.dimension().identifier(), pickedUpStack}
                           );
                        }

                        var27 = FluidStack.EMPTY;
                     } catch (Throwable var22) {
                        if (tx != null) {
                           try {
                              tx.close();
                           } catch (Throwable var21) {
                              var22.addSuppressed(var21);
                           }
                        }

                        throw var22;
                     }

                     if (tx != null) {
                        tx.close();
                     }

                     return var27;
                  }

                  if (tx != null) {
                     tx.close();
                  }

                  return var29;
               }

               if (tx != null) {
                  tx.close();
               }

               return var29;
            }

            if (tx != null) {
               tx.close();
            }

            return var26;
         }

         if (tx != null) {
            tx.close();
         }

         return var26;
      }
   }

   public static FluidStack tryPlaceFluid(@Nullable Storage<FluidVariant> source, @Nullable Player player, Level level, InteractionHand hand, BlockPos pos) {
      if (source == null) {
         return FluidStack.EMPTY;
      }

      long bucketDroplets = TransferConvert.mbToDroplets(1000L);
      Iterator var7 = source.iterator();

      Transaction tx;
      FluidStack var13;
      while (true) {
         if (!var7.hasNext()) {
            return FluidStack.EMPTY;
         }

         StorageView<FluidVariant> view = (StorageView<FluidVariant>)var7.next();
         if (!view.isResourceBlank()) {
            FluidStack resource = TransferConvert.toFluidStack((FluidVariant)view.getResource());
            tx = Transaction.openOuter();

            label58: {
               try {
                  long extracted = view.extract((FluidVariant)view.getResource(), bucketDroplets, tx);
                  if (extracted == bucketDroplets) {
                     if (!tryPlaceFluid(resource, player, level, hand, pos)) {
                        break label58;
                     }

                     tx.commit();
                     var13 = resource.copyWithAmount(1000);
                     break;
                  }
               } catch (Throwable var15) {
                  if (tx != null) {
                     try {
                        tx.close();
                     } catch (Throwable var14) {
                        var15.addSuppressed(var14);
                     }
                  }

                  throw var15;
               }

               if (tx != null) {
                  tx.close();
               }
               continue;
            }

            if (tx != null) {
               tx.close();
            }
         }
      }

      if (tx != null) {
         tx.close();
      }

      return var13;
   }

   public static boolean tryPlaceFluid(FluidStack resource, @Nullable Player player, Level level, InteractionHand hand, BlockPos pos) {
      FluidStack stack = resource.copyWithAmount(1000);
      FluidType fluidType = resource.getFluidType();
      if (!stack.isEmpty() && fluidType.canBePlacedInLevel(level, pos, stack)) {
         if (level instanceof ServerLevel serverLevel
            && player != null
            && !BlockUtil.canMachinePlace(serverLevel, pos, player.getGameProfile(), player.blockPosition())) {
            return false;
         } else {
            ItemStack handItem = player == null ? ItemStack.EMPTY : player.getItemInHand(hand);
            BlockPlaceContext context = new BlockPlaceContext(level, player, hand, handItem, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
            BlockState destBlockState = level.getBlockState(pos);
            boolean isDestReplaceable = destBlockState.canBeReplaced(context);
            boolean canDestContainFluid = destBlockState.getBlock() instanceof LiquidBlockContainer lbc
               && lbc.canPlaceLiquid(player, level, pos, destBlockState, stack.getFluid());
            if (!destBlockState.isAir() && !isDestReplaceable && !canDestContainFluid) {
               return false;
            }

            if (fluidType.isVaporizedOnPlacement(level, pos, stack)) {
               fluidType.onVaporize(player, level, pos, stack);
               return true;
            }

            if (canDestContainFluid) {
               LiquidBlockContainer lbc = (LiquidBlockContainer)destBlockState.getBlock();
               lbc.placeLiquid(level, pos, destBlockState, stack.getFluid().defaultFluidState());
            } else {
               if (!level.isClientSide() && isDestReplaceable && !destBlockState.liquid()) {
                  level.destroyBlock(pos, true);
               }

               BlockState state = fluidType.getBlockForFluidState(level, pos, stack.getFluid().defaultFluidState());
               level.setBlock(pos, state, 11);
            }

            playSoundAndGameEvent(stack, level, pos, player, false);
            return true;
         }
      } else {
         return false;
      }
   }
}
