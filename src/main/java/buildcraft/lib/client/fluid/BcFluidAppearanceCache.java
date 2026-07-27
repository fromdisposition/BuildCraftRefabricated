package buildcraft.lib.client.fluid;

import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.lib.client.texture.BcTextureAtlases;
import buildcraft.lib.fluid.identity.FluidIdentity;
import buildcraft.lib.fluid.stack.FluidStack;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.rendertype.RenderType;
import buildcraft.lib.client.render.BCLibRenderTypes;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public final class BcFluidAppearanceCache {
   private static final Map<Fluid, BcFluidAppearance> CACHE = new ConcurrentHashMap<>();

   private BcFluidAppearanceCache() {
   }

   public static @Nullable BcFluidAppearance get(FluidStack stack) {
      return stack != null && !stack.isEmpty() && stack.getFluid() != null
         ? CACHE.computeIfAbsent(FluidIdentity.canonicalFluid(stack.getFluid()), f -> build(stack))
         : null;
   }

   public static @Nullable BcFluidAppearance get(Fluid fluid) {
      if (fluid == null || fluid.defaultFluidState().isEmpty()) {
         return null;
      }

      Fluid canonical = FluidIdentity.canonicalFluid(fluid);
      return CACHE.computeIfAbsent(canonical, f -> build(new FluidStack(f, 1)));
   }

   private static BcFluidAppearance build(FluidStack stack) {
      var sprite = BcFluidRenderLookup.resolveSprite(stack, BcFluidRenderLookup.SpriteKind.STILL);
      int tint = BcFluidRenderLookup.resolveTint(stack);
      BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(stack.getFluid());
      boolean translucent = entry != null ? entry.gaseous() : BcFluidRenderLookup.resolveTranslucent(stack);
      return entry != null
         ? BcFluidAppearance.fromEntry(entry, sprite, tint, translucent)
         : BcFluidAppearance.vanilla(sprite, tint, translucent);
   }

   public static RenderType renderType(BcFluidAppearance appearance) {
      return appearance.translucent()
         ? BCLibRenderTypes.entityTranslucent(BcTextureAtlases.BLOCKS_TEXTURE)
         : BCLibRenderTypes.entityCutout(BcTextureAtlases.BLOCKS_TEXTURE);
   }

   public static void clear() {
      CACHE.clear();
   }
}
