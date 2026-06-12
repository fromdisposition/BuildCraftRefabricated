package buildcraft.lib.fabric.mixin;

import buildcraft.factory.collision.MinerShaftCollisionRegistry;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMinerShaftCollisionMixin {
   @Inject(
      method = "collectColliders(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/Level;Ljava/util/List;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;",
      at = @At("RETURN"),
      cancellable = true
   )
   private static void buildcraft$appendMinerShaftColliders(
      @Nullable Entity source, Level level, List<VoxelShape> entityColliders, AABB boundingBox, CallbackInfoReturnable<List<VoxelShape>> cir
   ) {
      cir.setReturnValue(MinerShaftCollisionRegistry.intersecting(level, boundingBox, cir.getReturnValue()));
   }
}
