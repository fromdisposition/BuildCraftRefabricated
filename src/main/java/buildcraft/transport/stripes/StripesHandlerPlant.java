package buildcraft.transport.stripes;

import buildcraft.api.crops.CropManager;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public enum StripesHandlerPlant implements IStripesHandlerItem {
   INSTANCE;

   @Override
   public boolean handle(Level world, BlockPos pos, Direction direction, ItemStack stack, Player player, IStripesActivator activator) {
      return CropManager.plantCrop(world, player, stack, pos.relative(direction).below())
         || CropManager.plantCrop(world, player, stack, pos.relative(direction));
   }
}
