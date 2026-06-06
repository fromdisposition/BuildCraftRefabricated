package buildcraft.builders.item;

import buildcraft.builders.addon.AddonFillerPlanner;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.ItemAddon;
import net.minecraft.world.item.Item.Properties;

public class ItemFillerPlanner extends ItemAddon {
   public ItemFillerPlanner(Properties properties) {
      super(properties);
   }

   @Override
   public Addon createAddon() {
      return new AddonFillerPlanner();
   }
}
