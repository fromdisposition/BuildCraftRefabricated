package buildcraft.transport.stripes;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public enum StripesHandlerHoe implements IStripesHandlerItem {
   INSTANCE;

   @Override
   public boolean handle(Level world, BlockPos pos, Direction direction, ItemStack stack, Player player, IStripesActivator activator) {
      if (!(stack.getItem() instanceof HoeItem)) {
         return false;
      }

      BlockPos target = pos.relative(direction);
      BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(target), Direction.UP, target, false);
      UseOnContext ctx = new UseOnContext(world, player, InteractionHand.MAIN_HAND, stack, hit);
      if (stack.getItem().useOn(ctx).consumesAction()) {
         return true;
      }

      if (direction != Direction.UP) {
         BlockPos below = target.below();
         hit = new BlockHitResult(Vec3.atCenterOf(below), Direction.UP, below, false);
         ctx = new UseOnContext(world, player, InteractionHand.MAIN_HAND, stack, hit);
         if (stack.getItem().useOn(ctx).consumesAction()) {
            return true;
         }
      }

      return false;
   }
}
