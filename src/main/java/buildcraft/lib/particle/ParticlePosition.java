package buildcraft.lib.particle;

import net.minecraft.world.phys.Vec3;

public class ParticlePosition {
   public final Vec3 position;
   public final Vec3 motion;

   public ParticlePosition(Vec3 position, Vec3 motion) {
      this.position = position;
      this.motion = motion;
   }
}
