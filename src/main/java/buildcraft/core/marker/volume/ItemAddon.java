package buildcraft.core.marker.volume;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

public abstract class ItemAddon extends Item {
   public ItemAddon(Properties properties) {
      super(properties);
   }

   public abstract Addon createAddon();

   public InteractionResult useOn(UseOnContext context) {
      return (InteractionResult)(context.getLevel().isClientSide()
         ? InteractionResult.PASS
         : this.tryAttach(context.getLevel(), context.getPlayer(), context.getHand()));
   }

   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      return (InteractionResult)(level.isClientSide() ? InteractionResult.PASS : this.tryAttach(level, player, hand));
   }

   private InteractionResult tryAttach(Level level, Player player, InteractionHand hand) {
      WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(level);
      Pair<VolumeBox, EnumAddonSlot> targeted = EnumAddonSlot.getSelectingVolumeBoxAndSlot(player, volumeBoxes.volumeBoxes);
      VolumeBox volumeBox = (VolumeBox)targeted.getLeft();
      EnumAddonSlot slot = (EnumAddonSlot)targeted.getRight();
      if (volumeBox == null || slot == null) {
         return InteractionResult.PASS;
      }

      if (volumeBox.addons.containsKey(slot)) {
         return InteractionResult.PASS;
      }

      Addon addon = this.createAddon();
      if (!addon.canBePlaceInto(volumeBox)) {
         return InteractionResult.PASS;
      }

      addon.volumeBox = volumeBox;
      volumeBox.addons.put(slot, addon);
      addon.onAdded();
      volumeBoxes.markDirtyAndBroadcast();
      ItemStack held = player.getItemInHand(hand);
      if (!player.getAbilities().instabuild) {
         held.shrink(1);
      }

      return InteractionResult.SUCCESS;
   }
}
