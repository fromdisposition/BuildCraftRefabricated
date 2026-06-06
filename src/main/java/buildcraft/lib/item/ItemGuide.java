package buildcraft.lib.item;

import buildcraft.lib.fabric.BCLibClientBridge;
import buildcraft.lib.misc.AdvancementUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class ItemGuide extends Item {
   private static final Identifier ADVANCEMENT = Identifier.parse("buildcraftcore:guide");
   private final String bookName;

   public ItemGuide(Properties properties, String bookName) {
      super(properties);
      this.bookName = bookName;
   }

   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      if (level.isClientSide()) {
         BCLibClientBridge.openGuideScreen(this.bookName);
      } else {
         AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
      }

      return InteractionResult.SUCCESS;
   }
}
