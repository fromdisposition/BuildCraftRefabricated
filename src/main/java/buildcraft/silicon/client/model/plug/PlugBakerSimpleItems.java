package buildcraft.silicon.client.model.plug;

import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.silicon.client.model.key.KeyPlugSimple;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public enum PlugBakerSimpleItems implements IPluggableStaticBaker<KeyPlugSimple> {
   INSTANCE;

   private static final Map<KeyPlugSimple, List<BakedQuad>> cached = new HashMap<>();

   public static void onModelBake() {
      cached.clear();
   }

   private static void addBox(
      List<MutableQuad> quads, TextureAtlasSprite sprite, float x0, float y0, float z0, float x1, float y1, float z1, ModelUtil.UvFaceData[] faceUvs
   ) {
      Vector3f center = new Vector3f((x0 + x1) / 2.0F, (y0 + y1) / 2.0F, (z0 + z1) / 2.0F);
      Vector3f radius = new Vector3f((x1 - x0) / 2.0F, (y1 - y0) / 2.0F, (z1 - z0) / 2.0F);
      AABB box = new AABB(x0, y0, z0, x1, y1, z1);

      for (Direction face : Direction.values()) {
         ModelUtil.UvFaceData uvs = faceUvs != null ? faceUvs[face.ordinal()] : new ModelUtil.UvFaceData();
         if (faceUvs == null) {
            ModelUtil.mapBoxToUvs(box, face, uvs);
         }

         MutableQuad q = ModelUtil.createFace(face, center, radius, uvs);
         q.setSprite(sprite);
         q.vertex_0.texFromSprite(sprite);
         q.vertex_1.texFromSprite(sprite);
         q.vertex_2.texFromSprite(sprite);
         q.vertex_3.texFromSprite(sprite);
         quads.add(q);
      }
   }

   public List<BakedQuad> bake(KeyPlugSimple key) {
      if (!cached.containsKey(key)) {
         List<MutableQuad> rawQuads = new ArrayList<>();
         String layerName = key.layer != null ? key.layer.toString().toLowerCase() : "";
         if (layerName.contains("cutout")) {
            String texturePath = "block/plugs/" + key.identifier;
            if (key.identifier.equals("pulsar")) {
               texturePath = "block/plugs/pulsar_static";
            }

            TextureAtlasSprite sprite = SpriteUtil.getSprite(Identifier.fromNamespaceAndPath("buildcraftsilicon", texturePath));
            if (sprite == null) {
               sprite = SpriteUtil.missingSprite();
            }

            ModelUtil.UvFaceData[] explicitUvs;
            if (key.identifier.equals("pulsar")) {
               explicitUvs = new ModelUtil.UvFaceData[]{
                  ModelUtil.UvFaceData.from16(2, 5, 5, 11),
                  ModelUtil.UvFaceData.from16(2, 5, 5, 11),
                  ModelUtil.UvFaceData.from16(2, 5, 5, 11),
                  ModelUtil.UvFaceData.from16(2, 5, 5, 11),
                  ModelUtil.UvFaceData.from16(5, 5, 11, 11),
                  ModelUtil.UvFaceData.from16(5, 5, 11, 11)
               };
            } else {
               explicitUvs = new ModelUtil.UvFaceData[]{
                  ModelUtil.UvFaceData.from16(3, 5, 5, 11),
                  ModelUtil.UvFaceData.from16(3, 5, 5, 11),
                  ModelUtil.UvFaceData.from16(3, 5, 5, 11),
                  ModelUtil.UvFaceData.from16(3, 5, 5, 11),
                  ModelUtil.UvFaceData.from16(5, 5, 11, 11),
                  ModelUtil.UvFaceData.from16(5, 5, 11, 11)
               };
            }

            addBox(rawQuads, sprite, 0.125F, 0.3125F, 0.3125F, 0.25F, 0.6875F, 0.6875F, explicitUvs);
         }

         List<BakedQuad> baked = new ArrayList<>();

         for (MutableQuad q : rawQuads) {
            q.rotate(Direction.WEST, key.side, 0.5F, 0.5F, 0.5F);
            q.multShade();
            baked.add(q.toBakedBlock());
         }

         cached.put(key, baked);
      }

      return cached.get(key);
   }
}
