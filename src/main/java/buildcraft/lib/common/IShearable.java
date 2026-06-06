package buildcraft.lib.common;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IShearable {
   default boolean isShearable(Player player, ItemStack item, Level level, BlockPos pos) {
      return this.isShearable(level, pos);
   }

   default boolean isShearable(Level level, BlockPos pos) {
      return true;
   }

   default List<ItemStack> onSheared(Player player, ItemStack item, Level level, BlockPos pos) {
      return List.of();
   }
}
