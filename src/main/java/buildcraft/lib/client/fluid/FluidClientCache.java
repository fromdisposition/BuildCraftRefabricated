package buildcraft.lib.client.fluid;

import buildcraft.lib.client.texture.BcTextureAtlases;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.misc.FluidUtilBC;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public final class FluidClientCache {
   private static final Map<Fluid, FluidClientCache.Appearance> CACHE = new ConcurrentHashMap<>();

   private FluidClientCache() {
   }

   public static FluidClientCache.@Nullable Appearance get(FluidStack stack) {
      return stack != null && !stack.isEmpty() && stack.getFluid() != null
         ? CACHE.computeIfAbsent(FluidUtilBC.canonicalFluid(stack.getFluid()), f -> build(stack))
         : null;
   }

   private static FluidClientCache.Appearance build(FluidStack stack) {
      Identifier id = FluidUtilBC.getFluidTexture(stack);
      TextureAtlasSprite sprite = BcTextureAtlases.getBlockSprite(id);
      return new FluidClientCache.Appearance(sprite, FluidUtilBC.getFluidColor(stack), FluidUtilBC.shouldRenderTranslucent(stack));
   }

   public static RenderType renderType(FluidClientCache.Appearance appearance) {
      return appearance.translucent() ? RenderTypes.entityTranslucent(BcTextureAtlases.BLOCKS_TEXTURE) : RenderTypes.entityCutout(BcTextureAtlases.BLOCKS_TEXTURE);
   }

   public static void clear() {
      CACHE.clear();
   }

   public record Appearance(TextureAtlasSprite sprite, int tint, boolean translucent) {
   }
}
