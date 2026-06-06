package buildcraft.core.item;

import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ItemVolumeBox extends Item {
   public ItemVolumeBox(Properties properties) {
      super(properties);
   }

   public InteractionResult useOn(UseOnContext context) {
      Level level = context.getLevel();
      BlockPos offset = context.getClickedPos().relative(context.getClickedFace());
      if (level.isClientSide()) {
         return InteractionResult.SUCCESS;
      } else {
         WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(level);
         VolumeBox current = volumeBoxes.getVolumeBoxAt(offset);
         if (current == null) {
            volumeBoxes.addVolumeBox(offset);
            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.FAIL;
         }
      }
   }
}
