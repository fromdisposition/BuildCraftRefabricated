package buildcraft.lib.fabric.mixin;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes BucketItem's {@code content} field. On 1.21.5+ BucketItem.getContent() is public, but on 1.21.1
 * the field has no public getter, so BcRegistryUtil reads it through this accessor on that node. The
 * {@code content} field is long-standing vanilla, present on every targeted version.
 */
@Mixin(BucketItem.class)
public interface BucketItemAccessor {
   @Accessor("content")
   Fluid buildcraft$getContent();
}
