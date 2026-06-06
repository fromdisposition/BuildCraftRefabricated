package buildcraft.fabric.client.block;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public interface ClientBlockExtensions {
   default boolean addHitEffects(BlockState state, Level level, @Nullable HitResult target, ParticleEngine manager) {
      return false;
   }

   default boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
      return false;
   }
}
