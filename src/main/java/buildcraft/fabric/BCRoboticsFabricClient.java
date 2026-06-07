package buildcraft.fabric;

import buildcraft.robotics.BCRoboticsEntities;
import buildcraft.robotics.BCRoboticsMenuTypes;
import buildcraft.robotics.gui.GuiRequester;
import buildcraft.robotics.gui.GuiZonePlanner;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.gui.screens.MenuScreens;

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
         EntityRenderers.register(BCRoboticsEntities.ROBOT, NoopRenderer::new);
      }
   }
}
