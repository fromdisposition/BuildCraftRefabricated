package buildcraft.robotics.platform;

import buildcraft.robotics.BCRoboticsEntities;
import buildcraft.robotics.BCRoboticsMenuTypes;
import buildcraft.robotics.gui.GuiRequester;
import buildcraft.robotics.gui.GuiZonePlanner;
import buildcraft.robotics.client.render.RenderRobot;
import net.minecraft.client.gui.screens.MenuScreens;
//? if >= 1.21.10 {
import net.minecraft.client.renderer.entity.EntityRenderers;
//?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
*///?}

public final class BCRoboticsFabricClient {
   private BCRoboticsFabricClient() {
   }

   public static void init() {
      if (BCRoboticsMenuTypes.ZONE_PLANNER != null) {
         MenuScreens.register(BCRoboticsMenuTypes.ZONE_PLANNER, GuiZonePlanner::new);
      }

      if (BCRoboticsMenuTypes.REQUESTER != null) {
         MenuScreens.register(BCRoboticsMenuTypes.REQUESTER, GuiRequester::new);
      }

      if (BCRoboticsEntities.ROBOT != null) {
         //? if >= 1.21.10 {
         EntityRenderers.register(BCRoboticsEntities.ROBOT, RenderRobot::new);
         //?} else {
         /*// vanilla EntityRenderers.register is private on 1.21.1; Fabric's EntityRendererRegistry is the
         // public entry point there (it is only deprecated on 26.x, where the vanilla method is public).
         EntityRendererRegistry.register(BCRoboticsEntities.ROBOT, RenderRobot::new);
         *///?}
      }
   }
}
