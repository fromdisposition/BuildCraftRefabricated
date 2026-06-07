package buildcraft.robotics;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;

public class BCRoboticsSprites {
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_ROBOT_SLEEP = getHolder("triggers/trigger_robot_sleep");
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_ROBOT_IN_STATION = getHolder("triggers/trigger_robot_in_station");
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_ROBOT_LINKED = getHolder("triggers/trigger_robot_linked");
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_ROBOT_RESERVED = getHolder("triggers/trigger_robot_reserved");
   public static final SpriteHolderRegistry.SpriteHolder ACTION_STATION_PROVIDE_ITEMS = getHolder("triggers/action_station_provide_items");
   public static final SpriteHolderRegistry.SpriteHolder ACTION_STATION_REQUEST_ITEMS = getHolder("triggers/action_station_request_items");
   public static final SpriteHolderRegistry.SpriteHolder ACTION_STATION_ACCEPT_ITEMS = getHolder("triggers/action_station_accept_items");
   public static final SpriteHolderRegistry.SpriteHolder ACTION_STATION_DROP_IN_PIPE = getHolder("triggers/action_station_drop_in_pipe");
   public static final SpriteHolderRegistry.SpriteHolder ACTION_STATION_PROVIDE_FLUIDS = getHolder("triggers/action_station_provide_fluids");
   public static final SpriteHolderRegistry.SpriteHolder ACTION_STATION_ACCEPT_FLUIDS = getHolder("triggers/action_station_accept_fluids");
   public static final SpriteHolderRegistry.SpriteHolder ACTION_STATION_MACHINE_REQUEST = getHolder("triggers/action_station_machine_request");
   public static final SpriteHolderRegistry.SpriteHolder ACTION_ROBOT_WAKEUP = getHolder("triggers/action_robot_wakeup");

   private static SpriteHolderRegistry.SpriteHolder getHolder(String loc) {
      return SpriteHolderRegistry.getHolder("buildcraftrobotics:" + loc);
   }

   public static void preInit() {
   }
}
