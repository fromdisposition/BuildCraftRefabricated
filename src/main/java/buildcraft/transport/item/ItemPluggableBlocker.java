package buildcraft.transport.item;

import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.transport.BCTransportPlugs;
import buildcraft.transport.plug.PluggableBlocker;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.phys.AABB;

public class ItemPluggableBlocker extends Item implements IItemPluggable {
   public ItemPluggableBlocker(Properties properties) {
      super(properties);
   }

   @Nullable
   @Override
   public PipePluggable onPlace(ItemStack stack, IPipeHolder holder, Direction side, Player player, InteractionHand hand) {
      PluggableDefinition def = BCTransportPlugs.blocker;
      if (def != null && def.creator != null) {
         SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos());
         return new PluggableBlocker(def, holder, side);
      } else {
         return null;
      }
   }

   @Override
   public AABB getPlacementBoundingBox(ItemStack stack, Direction side) {
      return PluggableBlocker.boundingBoxFor(side);
   }
}
