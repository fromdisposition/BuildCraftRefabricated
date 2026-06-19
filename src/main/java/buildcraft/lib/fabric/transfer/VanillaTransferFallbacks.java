package buildcraft.lib.fabric.transfer;

//? if >= 26.1 {
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
//?} else {
/*import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
*///?}
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class VanillaTransferFallbacks {
   private static boolean registered;

   private VanillaTransferFallbacks() {
   }

   public static synchronized void register() {
      if (!registered) {
         registered = true;
         ItemStorage.SIDED.registerFallback(VanillaTransferFallbacks::resolveItemStorage);
      }
   }

   private static @Nullable Storage<ItemVariant> resolveItemStorage(
      Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction side
   ) {
      if (state == null) {
         state = level.getBlockState(pos);
      }

      Container container = null;
      if (blockEntity instanceof Container be) {
         container = be;
      } else if (state.getBlock() instanceof ChestBlock chestBlock) {
         container = ChestBlock.getContainer(chestBlock, state, level, pos, true);
      }

      //? if >= 26.1 {
      return container == null ? null : ContainerStorage.of(container, side);
      //?} else {
      /*return container == null ? null : InventoryStorage.of(container, side);
      *///?}
   }
}
