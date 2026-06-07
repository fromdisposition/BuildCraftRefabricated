package buildcraft.robotics.robot;

import buildcraft.api.robots.IRobotRegistry;
import buildcraft.api.robots.IRobotRegistryProvider;
import net.minecraft.world.level.Level;

public class RobotRegistryProvider implements IRobotRegistryProvider {
   @Override
   public IRobotRegistry getRegistry(Level world) {
      return RobotRegistry.get(world);
   }
}
