package buildcraft.lib.fabric.mixin;

import buildcraft.fabric.fluid.BcFluidTags;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Entity.class)
public class EntityBcFluidInteractionMixin {
   @ModifyArgs(
      method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/EntityFluidInteraction;<init>(Ljava/util/Set;)V"
      )
   )
   private void buildcraft$trackBcFluids(Args args) {
      @SuppressWarnings("unchecked")
      Set<TagKey<Fluid>> tags = new HashSet<>((Set<TagKey<Fluid>>) args.get(0));
      tags.add(BcFluidTags.BC_FLUIDS);
      args.set(0, Set.copyOf(tags));
   }
}
