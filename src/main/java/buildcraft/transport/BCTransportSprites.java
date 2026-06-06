package buildcraft.transport;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli;
import java.util.EnumMap;
import java.util.Locale;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

public class BCTransportSprites {
   public static final SpriteHolderRegistry.SpriteHolder PIPE_COLOUR = getHolder("pipes/overlay_stained");
   public static final SpriteHolderRegistry.SpriteHolder COLOUR_ITEM_BOX = getHolder("pipes/colour_item_box");
   public static final SpriteHolderRegistry.SpriteHolder PIPE_COLOUR_BORDER_OUTER = getHolder("pipes/colour_border_outer");
   public static final SpriteHolderRegistry.SpriteHolder PIPE_COLOUR_BORDER_INNER = getHolder("pipes/colour_border_inner");
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_ITEMS_TRAVERSING;
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_FLUIDS_TRAVERSING;
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_PIPE_EMPTY;
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_POWER_REQUESTED;
   public static final SpriteHolderRegistry.SpriteHolder[] POWER_LIMIT;
   public static final SpriteHolderRegistry.SpriteHolder[] POWER_LIMIT_RF;
   public static final SpriteHolderRegistry.SpriteHolder[] ACTION_PIPE_COLOUR = new SpriteHolderRegistry.SpriteHolder[ColourUtil.COLOURS.length];
   public static final EnumMap<PipeBehaviourEmzuli.SlotIndex, SpriteHolderRegistry.SpriteHolder> ACTION_EXTRACTION_PRESET;
   private static final EnumMap<DyeColor, SpriteHolderRegistry.SpriteHolder> PIPE_SIGNAL_ON;
   private static final EnumMap<DyeColor, SpriteHolderRegistry.SpriteHolder> PIPE_SIGNAL_OFF;
   private static final EnumMap<Direction, SpriteHolderRegistry.SpriteHolder> ACTION_PIPE_DIRECTION;
   public static final SpriteHolderRegistry.SpriteHolder POWER_FLOW;
   public static final SpriteHolderRegistry.SpriteHolder POWER_FLOW_OVERLOAD;

   private static SpriteHolderRegistry.SpriteHolder getHolder(String loc) {
      return SpriteHolderRegistry.getHolder("buildcrafttransport:" + loc);
   }

   private static SpriteHolderRegistry.SpriteHolder getHolder(String module, String loc) {
      return SpriteHolderRegistry.getHolder("buildcrafttransport:" + loc);
   }

   public static SpriteHolderRegistry.SpriteHolder getPipeSignal(boolean active, DyeColor colour) {
      return (active ? PIPE_SIGNAL_ON : PIPE_SIGNAL_OFF).get(colour);
   }

   public static SpriteHolderRegistry.SpriteHolder getPipeDirection(Direction face) {
      return ACTION_PIPE_DIRECTION.get(face);
   }

   static {
      for (DyeColor colour : ColourUtil.COLOURS) {
         ACTION_PIPE_COLOUR[colour.ordinal()] = getHolder("core", "item/paintbrush/" + colour.getName());
      }

      PIPE_SIGNAL_OFF = new EnumMap<>(DyeColor.class);
      PIPE_SIGNAL_ON = new EnumMap<>(DyeColor.class);

      for (DyeColor colour : ColourUtil.COLOURS) {
         String pre = "triggers/trigger_pipesignal_" + colour.getName().toLowerCase(Locale.ROOT) + "_";
         PIPE_SIGNAL_OFF.put(colour, getHolder(pre + "inactive"));
         PIPE_SIGNAL_ON.put(colour, getHolder(pre + "active"));
      }

      ACTION_EXTRACTION_PRESET = new EnumMap<>(PipeBehaviourEmzuli.SlotIndex.class);

      for (PipeBehaviourEmzuli.SlotIndex index : PipeBehaviourEmzuli.SlotIndex.VALUES) {
         ACTION_EXTRACTION_PRESET.put(index, getHolder("triggers/extraction_preset_" + index.colour.getName()));
      }

      ACTION_PIPE_DIRECTION = new EnumMap<>(Direction.class);

      for (Direction face : Direction.values()) {
         ACTION_PIPE_DIRECTION.put(face, getHolder("core", "triggers/trigger_dir_" + face.getName().toLowerCase(Locale.ROOT)));
      }

      POWER_FLOW = getHolder("pipes/power_flow");
      POWER_FLOW_OVERLOAD = getHolder("pipes/power_flow_overload");
      TRIGGER_ITEMS_TRAVERSING = getHolder("transport", "triggers/trigger_pipecontents_containsitems");
      TRIGGER_FLUIDS_TRAVERSING = getHolder("transport", "triggers/trigger_pipecontents_containsfluids");
      TRIGGER_PIPE_EMPTY = getHolder("transport", "triggers/trigger_pipecontents_empty");
      TRIGGER_POWER_REQUESTED = getHolder("transport", "triggers/trigger_pipecontents_requestsenergy");
      int numLevels = 7;
      POWER_LIMIT = new SpriteHolderRegistry.SpriteHolder[numLevels];
      POWER_LIMIT_RF = new SpriteHolderRegistry.SpriteHolder[numLevels];
      String[] limiterMasks = new String[]{"m256", "m128", "m64", "m16", "m8", "m2", "m0"};

      for (int i = 0; i < numLevels; i++) {
         POWER_LIMIT[i] = getHolder("transport", "triggers/trigger_limiter_" + limiterMasks[i]);
         POWER_LIMIT_RF[i] = getHolder("transport", "triggers/trigger_rf_limiter_" + limiterMasks[i]);
      }
   }
}
