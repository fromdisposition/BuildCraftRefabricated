/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fabric.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
//? if < 26.1 {
/*import buildcraft.fabric.fluid.BcFluidTags;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.Set;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
*///?}

/**
 * Gives BuildCraft liquids (the {@link buildcraft.fabric.fluid.BcFluidTags#BC_LIQUIDS} tag) full water-like
 * entity physics on 1.21.x.
 *
 * <p>26.x gets this from Fabric's {@code EntityFluidInteractionRegistry} / {@code FluidBehavior.WATER_LIKE}
 * (the {@code fabric-content-registries} custom-interactable-fluid subsystem), which does not exist in the
 * 1.21.x Fabric API. Fabric implements it by making the entity's {@code FluidTags.WATER}-keyed checks also
 * answer for the registered fluid tag; this mixin does exactly the same thing natively for BC_LIQUIDS:
 * <ul>
 *   <li>{@code updateInWaterStateAndDoWaterCurrentPushing} — record fluid height + apply the water current
 *       push and flag the entity as touching water (drives {@code isInWater()} → travel buoyancy/drag/fall reset).</li>
 *   <li>{@code isEyeInFluid(WATER)} — also true when the eye is in a BC liquid (drives {@code isUnderWater()},
 *       breath loss / drowning in {@code LivingEntity.baseTick}, and the underwater fog).</li>
 *   <li>{@code getFluidHeight(WATER)} — includes BC liquid height (drives the swim-up jump and jump threshold).</li>
 * </ul>
 * The swim crawl ({@code updateSwimming}), sprinting, boats floating and ridden-mob floating are deliberately
 * NOT enabled — kept in parity with the 26.x registration which uses a custom non-swimming FluidBehavior.
 * Compiled out on 26.x (the Fabric registry handles it there), leaving an empty inert mixin.
 */
@Mixin(Entity.class)
public abstract class EntityBcFluidPhysicsMixin {
   //? if < 26.1 {
   /*@Shadow protected boolean wasTouchingWater;
   @Shadow @Final private Set<TagKey<Fluid>> fluidOnEyes;
   @Shadow protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;

   @Shadow public abstract boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> tag, double speed);

   @Shadow public abstract void resetFallDistance();

   @Inject(method = "updateInWaterStateAndDoWaterCurrentPushing", at = @At("TAIL"))
   private void buildcraft$bcLiquidCurrentPush(CallbackInfo ci) {
      if (this.updateFluidHeightAndDoFluidPushing(BcFluidTags.BC_LIQUIDS, 0.014)) {
         if (!this.wasTouchingWater) {
            this.resetFallDistance();
         }
         this.wasTouchingWater = true;
      }
   }

   @Inject(method = "isEyeInFluid", at = @At("HEAD"), cancellable = true)
   private void buildcraft$bcLiquidEye(TagKey<Fluid> tag, CallbackInfoReturnable<Boolean> cir) {
      if (tag == FluidTags.WATER && this.fluidOnEyes.contains(BcFluidTags.BC_LIQUIDS)) {
         cir.setReturnValue(true);
      }
   }

   @Inject(method = "getFluidHeight", at = @At("HEAD"), cancellable = true)
   private void buildcraft$bcLiquidHeight(TagKey<Fluid> tag, CallbackInfoReturnable<Double> cir) {
      if (tag == FluidTags.WATER) {
         double bcHeight = this.fluidHeight.getDouble(BcFluidTags.BC_LIQUIDS);
         if (bcHeight > 0.0) {
            cir.setReturnValue(Math.max(this.fluidHeight.getDouble(FluidTags.WATER), bcHeight));
         }
      }
   }
   *///?}
}
