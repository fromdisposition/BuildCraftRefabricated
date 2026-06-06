package buildcraft.transport.stripes;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerItem;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public enum StripesHandlerEntityInteract implements IStripesHandlerItem {
   INSTANCE;

   @Override
   public boolean handle(Level world, BlockPos pos, Direction direction, ItemStack stack, Player player, IStripesActivator activator) {
      BlockPos target = pos.relative(direction);
      List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, new AABB(target));
      Collections.shuffle(entities);

      for (LivingEntity entity : entities) {
         if (player.interactOn(entity, InteractionHand.MAIN_HAND, entity.position()).consumesAction()) {
            return true;
         }
      }

      return false;
   }
}
