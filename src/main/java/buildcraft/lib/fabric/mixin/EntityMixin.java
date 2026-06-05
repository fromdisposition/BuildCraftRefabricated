package buildcraft.lib.fabric.mixin;

import net.minecraft.world.entity.Entity;
import buildcraft.lib.attachments.EntityAttachment;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Nullable
    public <T, C> T getCapability(EntityAttachment<T, C> capability, C context) {
        return capability.getCapability((Entity) (Object) this, context);
    }
}
