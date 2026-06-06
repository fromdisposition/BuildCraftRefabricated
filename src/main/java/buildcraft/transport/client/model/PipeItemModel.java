package buildcraft.transport.client.model;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.transport.BCTransportItems;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class PipeItemModel implements ItemModel {
   private static final Field QUADS_FIELD;
   private static final Field PROPERTIES_FIELD;
   private static final Field TINTS_FIELD;
   private static final Field EXTENTS_FIELD;
   private final ItemModel vanillaDelegate;
   private final PipeDefinition definition;
   private final @Nullable QuadCollection vanillaQuads;
   private final @Nullable ModelRenderProperties renderProperties;
   private final Supplier<Vector3fc[]> extents;

   public PipeItemModel(ItemModel vanillaDelegate, PipeDefinition definition) {
      this.vanillaDelegate = vanillaDelegate;
      this.definition = definition;
      if (vanillaDelegate instanceof CuboidItemModelWrapper wrapper) {
         try {
            this.vanillaQuads = (QuadCollection)QUADS_FIELD.get(wrapper);
            this.renderProperties = (ModelRenderProperties)PROPERTIES_FIELD.get(wrapper);
            this.extents = (Supplier<Vector3fc[]>)EXTENTS_FIELD.get(wrapper);
         } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to read CuboidItemModelWrapper fields", e);
         }
      } else {
         this.vanillaQuads = null;
         this.renderProperties = null;
         this.extents = null;
      }
   }

   public void update(
      ItemStackRenderState renderState,
      ItemStack stack,
      ItemModelResolver modelResolver,
      ItemDisplayContext displayContext,
      @Nullable ClientLevel level,
      @Nullable ItemOwner owner,
      int seed
   ) {
      DyeColor colour = (DyeColor)stack.get(BCTransportItems.PIPE_COLOUR);
      if (this.vanillaQuads == null || this.renderProperties == null) {
         this.vanillaDelegate.update(renderState, stack, modelResolver, displayContext, level, owner, seed);
      } else if (colour == null) {
         this.vanillaDelegate.update(renderState, stack, modelResolver, displayContext, level, owner, seed);
      } else {
         renderState.appendModelIdentityElement(this);
         renderState.appendModelIdentityElement(colour);
         LayerRenderState baseLayer = renderState.newLayer();
         baseLayer.prepareQuadList().addAll(this.vanillaQuads.getAll());
         if (this.extents != null) {
            baseLayer.setExtents(this.extents);
         }

         this.renderProperties.applyToLayer(baseLayer, displayContext);
         TextureAtlasSprite[] maskArray = PipeBaseModelGenStandard.ensureMaskSprites(this.definition);
         TextureAtlasSprite maskSprite = maskArray != null && maskArray.length > 0 ? maskArray[0] : null;
         if (maskSprite != null && maskSprite != SpriteUtil.missingSprite()) {
            List<BakedQuad> overlayQuads = generateOverlayQuads(maskSprite);
            if (!overlayQuads.isEmpty()) {
               LayerRenderState overlayLayer = renderState.newLayer();
               overlayLayer.prepareQuadList().addAll(overlayQuads);
               if (this.extents != null) {
                  overlayLayer.setExtents(this.extents);
               }

               this.renderProperties.applyToLayer(overlayLayer, displayContext);
               // Fluid pipes paint the waterproofing band opaquely (alpha 0xFF), matching the old
               // baked dyed sprite; other colourable pipes keep the translucent tint film (alpha 0x4C).
               int alpha = this.definition.flowType == PipeApi.flowFluids ? 0xFF000000 : 0x4C000000;
               int tintColour = alpha | ColourUtil.getLightHex(colour);
               overlayLayer.tintLayers().add(tintColour);
            }
         }
      }
   }

   private static List<BakedQuad> generateOverlayQuads(TextureAtlasSprite maskSprite) {
      List<BakedQuad> quads = new ArrayList<>();
      ModelUtil.UvFaceData bottomSideUvs = ModelUtil.UvFaceData.from16(4, 12, 12, 16);
      ModelUtil.UvFaceData centerUvs = ModelUtil.UvFaceData.from16(4, 4, 12, 12);
      ModelUtil.UvFaceData topSideUvs = ModelUtil.UvFaceData.from16(4, 0, 12, 4);
      ModelUtil.UvFaceData capFaceUvs = ModelUtil.UvFaceData.from16(4, 4, 12, 12);
      addOverlayFaces(quads, maskSprite, new Vector3f(0.5F, 0.125F, 0.5F), new Vector3f(0.25F, 0.125F, 0.25F), new Direction[]{Direction.DOWN}, capFaceUvs);
      addOverlayFaces(
         quads,
         maskSprite,
         new Vector3f(0.5F, 0.125F, 0.5F),
         new Vector3f(0.25F, 0.125F, 0.25F),
         new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST},
         bottomSideUvs
      );
      addOverlayFaces(
         quads,
         maskSprite,
         new Vector3f(0.5F, 0.5F, 0.5F),
         new Vector3f(0.25F, 0.25F, 0.25F),
         new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST},
         centerUvs
      );
      addOverlayFaces(quads, maskSprite, new Vector3f(0.5F, 0.875F, 0.5F), new Vector3f(0.25F, 0.125F, 0.25F), new Direction[]{Direction.UP}, capFaceUvs);
      addOverlayFaces(
         quads,
         maskSprite,
         new Vector3f(0.5F, 0.875F, 0.5F),
         new Vector3f(0.25F, 0.125F, 0.25F),
         new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST},
         topSideUvs
      );
      return quads;
   }

   private static void addOverlayFaces(
      List<BakedQuad> quads, TextureAtlasSprite sprite, Vector3f center, Vector3f radius, Direction[] faces, ModelUtil.UvFaceData uvs
   ) {
      for (Direction face : faces) {
         MutableQuad quad = ModelUtil.createFace(face, center, radius, uvs);
         quad.setSprite(sprite);
         quad.texFromSprite(sprite);
         quad.setTint(0);
         quads.add(quad.toBakedTranslucent());
      }
   }

   static {
      try {
         QUADS_FIELD = CuboidItemModelWrapper.class.getDeclaredField("quads");
         QUADS_FIELD.setAccessible(true);
         PROPERTIES_FIELD = CuboidItemModelWrapper.class.getDeclaredField("properties");
         PROPERTIES_FIELD.setAccessible(true);
         TINTS_FIELD = CuboidItemModelWrapper.class.getDeclaredField("tints");
         TINTS_FIELD.setAccessible(true);
         EXTENTS_FIELD = CuboidItemModelWrapper.class.getDeclaredField("extents");
         EXTENTS_FIELD.setAccessible(true);
      } catch (NoSuchFieldException e) {
         throw new RuntimeException("Failed to access CuboidItemModelWrapper fields", e);
      }
   }
}
