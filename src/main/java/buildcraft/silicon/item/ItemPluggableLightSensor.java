package buildcraft.silicon.item;

import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.silicon.BCSiliconPlugs;
import buildcraft.silicon.plug.PluggableLightSensor;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class ItemPluggableLightSensor extends Item implements IItemPluggable {
   public ItemPluggableLightSensor(Properties properties) {
      super(properties);
   }

   @Nullable
   @Override
   public PipePluggable onPlace(ItemStack stack, IPipeHolder holder, Direction side, Player player, InteractionHand hand) {
      PluggableDefinition def = BCSiliconPlugs.lightSensor;
      if (def != null && def.creator != null) {
         SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos());
         return new PluggableLightSensor(def, holder, side);
      } else {
         return null;
      }
   }
}
