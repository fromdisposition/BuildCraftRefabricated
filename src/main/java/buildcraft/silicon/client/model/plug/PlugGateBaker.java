package buildcraft.silicon.client.model.plug;

import buildcraft.lib.client.texture.BcTextureAtlases;
import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.silicon.client.model.key.KeyPlugGate;
import buildcraft.silicon.gate.GateVariant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;

public class PlugGateBaker implements IPluggableStaticBaker<KeyPlugGate> {
   public static final PlugGateBaker INSTANCE = new PlugGateBaker();
   private static final Map<KeyPlugGate, List<BakedQuad>> cached = new ConcurrentHashMap<>();

   public static void onModelBake() {
      cached.clear();
   }

   private TextureAtlasSprite getSprite(String path) {
      return BcTextureAtlases.getBlockSprite(Identifier.parse(path));
   }

   public List<BakedQuad> bake(KeyPlugGate key) {
      return cached.computeIfAbsent(key, this::bakeUncached);
   }

   private List<BakedQuad> bakeUncached(KeyPlugGate key) {
      List<BakedQuad> quads = new ArrayList<>();
      GateQuadGeometry.appendStaticBaked(quads, key.variant, key.side, this::getSprite, true, 0);
      String dynPath = key.active ? "buildcraftsilicon:block/gates/gate_on" : "buildcraftsilicon:block/gates/gate_off";
      GateQuadGeometry.addRotatedBakedBox(
         quads, 0.11875F, 0.375F, 0.375F, 0.25625F, 0.625F, 0.625F, this.getSprite(dynPath), key.side, false, key.active ? 15 : 0
      );
      return quads;
   }

   public List<MutableQuad> bakeForItem(GateVariant variant) {
      List<MutableQuad> quads = new ArrayList<>();
      GateQuadGeometry.appendItemNorthFacing(quads, variant, this::getSprite, true);
      return quads;
   }
}
