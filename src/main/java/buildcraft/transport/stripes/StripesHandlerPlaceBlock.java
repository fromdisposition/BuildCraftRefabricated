package buildcraft.transport.stripes;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;
import buildcraft.lib.misc.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public enum StripesHandlerPlaceBlock implements IStripesHandlerItem {
   INSTANCE;

   @Override
   public boolean handle(Level world, BlockPos pos, Direction direction, ItemStack stack, Player player, IStripesActivator activator) {
      if (!(stack.getItem() instanceof BlockItem)) {
         return false;
      } else {
         BlockPos target = pos.relative(direction);
         if (!world.isEmptyBlock(target)) {
            return false;
         } else if (world instanceof ServerLevel serverLevel && !BlockUtil.canMachinePlace(serverLevel, target, player.getGameProfile(), pos)) {
            return false;
         } else {
            BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(target), direction.getOpposite(), target, false);
            UseOnContext ctx = new UseOnContext(world, player, InteractionHand.MAIN_HAND, stack, hitResult);
            stack.getItem().useOn(ctx);
            return true;
         }
      }
   }
}
