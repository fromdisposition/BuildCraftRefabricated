package buildcraft.lib.platform.menu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

//? if >= 1.21.11 {
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;

public interface BlockEntityExtendedMenu extends ExtendedMenuProvider<BlockPos> {
   default BlockEntity asBlockEntity() {
      return (BlockEntity)this;
   }

   default BlockPos getScreenOpeningData(ServerPlayer player) {
      return this.asBlockEntity().getBlockPos();
   }
}
//?} else {
/*import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.network.FriendlyByteBuf;

public interface BlockEntityExtendedMenu extends ExtendedMenuProvider {
   default BlockEntity asBlockEntity() {
      return (BlockEntity)this;
   }

   @Override
   default void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
      buf.writeBlockPos(this.asBlockEntity().getBlockPos());
   }
}
*///?}
