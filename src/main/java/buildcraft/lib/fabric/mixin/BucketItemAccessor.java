package buildcraft.lib.fabric.mixin;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// 1.21.1 has no public BucketItem.getContent(); only that node reads through this accessor.
@Mixin(BucketItem.class)
public interface BucketItemAccessor {
   @Accessor("content")
   Fluid buildcraft$getContent();
}
