package buildcraft.lib.fabric.mixin;

import buildcraft.factory.collision.MinerShaftCollisionRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMinerShaftCollisionMixin {
   /** Entity movement already calls {@code getBlockCollisions} from {@code Entity.collectColliders}. */
   @Inject(
      method = "getBlockCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/lang/Iterable;",
      at = @At("RETURN"),
      cancellable = true,
      require = 0
   )
   private void buildcraft$appendMinerShaftBlockCollisions(
      @Nullable Entity entity, AABB box, CallbackInfoReturnable<Iterable<VoxelShape>> cir
   ) {
      Level level = (Level)(Object)this;
      cir.setReturnValue(MinerShaftCollisionRegistry.concat(level, cir.getReturnValue(), box));
   }
}
