package buildcraft.api.mj;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface IMjEffectManager {
   void createPowerLossEffect(Level var1, Vec3 var2, long var3);

   void createPowerLossEffect(Level var1, Vec3 var2, Direction var3, long var4);

   void createPowerLossEffect(Level var1, Vec3 var2, Vec3 var3, long var4);
}
