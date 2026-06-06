package buildcraft.silicon;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.ColourUtil;
import net.minecraft.world.item.DyeColor;

public class BCSiliconSprites {
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_LIGHT_LOW = getHolder("triggers/trigger_light_dark");
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_LIGHT_HIGH = getHolder("triggers/trigger_light_bright");
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_TIMER_SHORT = getHolder("triggers/trigger_timer_short");
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_TIMER_MEDIUM = getHolder("triggers/trigger_timer_medium");
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_TIMER_LONG = getHolder("triggers/trigger_timer_long");
   public static final SpriteHolderRegistry.SpriteHolder ACTION_PULSAR_CONSTANT = getHolder("triggers/action_pulsar_on");
   public static final SpriteHolderRegistry.SpriteHolder ACTION_PULSAR_SINGLE = getHolder("triggers/action_pulsar_single");
   public static final SpriteHolderRegistry.SpriteHolder[] ACTION_PIPE_COLOUR = new SpriteHolderRegistry.SpriteHolder[ColourUtil.COLOURS.length];

   private static SpriteHolderRegistry.SpriteHolder getHolder(String loc) {
      return SpriteHolderRegistry.getHolder("buildcraftsilicon:" + loc);
   }

   private static SpriteHolderRegistry.SpriteHolder getHolder(String module, String loc) {
      return SpriteHolderRegistry.getHolder("buildcraftsilicon:" + loc);
   }

   public static void preInit() {
   }

   static {
      for (DyeColor colour : ColourUtil.COLOURS) {
         ACTION_PIPE_COLOUR[colour.ordinal()] = getHolder("core", "item/paintbrush/" + colour.getName());
      }
   }
}
