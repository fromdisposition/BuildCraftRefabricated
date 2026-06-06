package buildcraft.silicon.client.model;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.silicon.client.model.plug.PlugGateBaker;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.item.ItemPluggableGate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class GateItemModel implements ItemModel {
   private static final GateItemModel.ContextXform DEFAULT_XFORM = new GateItemModel.ContextXform(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.8F);
   private static final EnumMap<ItemDisplayContext, GateItemModel.ContextXform> XFORMS = new EnumMap<>(ItemDisplayContext.class);
   private static final LoadingCache<GateItemModel.CacheKey, List<BakedQuad>> cache = CacheBuilder.newBuilder()
      .expireAfterAccess(1L, TimeUnit.MINUTES)
      .build(CacheLoader.from(key -> {
         GateItemModel.ContextXform x = XFORMS.getOrDefault(key.context(), DEFAULT_XFORM);
         List<BakedQuad> quads = new ArrayList<>();

         for (MutableQuad mq : PlugGateBaker.INSTANCE.bakeForItem(key.variant())) {
            mq.setShade(false);
            applyXform(mq, x);
            mq.setCalculatedNormal();
            quads.add(mq.toBakedItem());
         }

         return quads;
      }));

   private static void applyXform(MutableQuad mq, GateItemModel.ContextXform x) {
      mq.translatef(-0.5F, -0.5F, -0.5F);
      if (x.rotX() != 0.0F) {
         mq.rotateX((float)Math.toRadians(x.rotX()));
      }

      if (x.rotY() != 0.0F) {
         mq.rotateY((float)Math.toRadians(x.rotY()));
      }

      if (x.rotZ() != 0.0F) {
         mq.rotateZ((float)Math.toRadians(x.rotZ()));
      }

      if (x.scale() != 1.0F) {
         mq.scalef(x.scale());
      }

      mq.translatef(0.5F, 0.5F, 0.5F);
      if (x.tx() != 0.0F || x.ty() != 0.0F || x.tz() != 0.0F) {
         mq.translatef(x.tx() / 16.0F, x.ty() / 16.0F, x.tz() / 16.0F);
      }
   }

   public static void onModelBake() {
      cache.invalidateAll();
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
      GateVariant variant = ItemPluggableGate.getVariant(stack);
      if (variant != null) {
         GateItemModel.CacheKey key = new GateItemModel.CacheKey(variant, displayContext);
         List<BakedQuad> quads = (List<BakedQuad>)cache.getUnchecked(key);
         if (!quads.isEmpty()) {
            renderState.appendModelIdentityElement(this);
            renderState.appendModelIdentityElement(variant);
            renderState.appendModelIdentityElement(displayContext);
            LayerRenderState layer = renderState.newLayer();
            layer.prepareQuadList().addAll(quads);
         }
      }
   }

   static {
      XFORMS.put(ItemDisplayContext.GUI, new GateItemModel.ContextXform(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.8F));
      XFORMS.put(ItemDisplayContext.GROUND, new GateItemModel.ContextXform(0.0F, 0.0F, 0.0F, 0.0F, 3.0F, 0.0F, 0.9F));
      XFORMS.put(ItemDisplayContext.HEAD, new GateItemModel.ContextXform(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.8F));
      XFORMS.put(ItemDisplayContext.FIXED, new GateItemModel.ContextXform(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.53F));
      XFORMS.put(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new GateItemModel.ContextXform(0.0F, 45.0F, 0.0F, 0.0F, 0.0F, -4.0F, 0.72F));
      XFORMS.put(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, new GateItemModel.ContextXform(0.0F, 225.0F, 0.0F, 0.0F, 0.0F, -4.0F, 0.72F));
      XFORMS.put(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new GateItemModel.ContextXform(75.0F, 225.0F, 0.0F, 0.0F, 2.5F, 0.0F, 0.675F));
      XFORMS.put(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, new GateItemModel.ContextXform(75.0F, 45.0F, 0.0F, 0.0F, 2.5F, 0.0F, 0.675F));
   }

   private record CacheKey(GateVariant variant, ItemDisplayContext context) {
   }

   private record ContextXform(float rotX, float rotY, float rotZ, float tx, float ty, float tz, float scale) {
   }
}
