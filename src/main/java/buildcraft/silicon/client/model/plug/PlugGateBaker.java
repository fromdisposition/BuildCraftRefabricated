package buildcraft.silicon.client.model.plug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import net.minecraft.client.resources.model.geometry.BakedQuad;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;

import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.silicon.client.model.key.KeyPlugGate;

@SuppressWarnings("deprecation")
public class PlugGateBaker implements IPluggableStaticBaker<KeyPlugGate> {
    public static final PlugGateBaker INSTANCE = new PlugGateBaker();

    private static final Map<KeyPlugGate, List<BakedQuad>> cached = new java.util.concurrent.ConcurrentHashMap<>();

    public static void onModelBake() {
        cached.clear();
    }

    private TextureAtlasSprite getSprite(String path) {
        net.minecraft.client.renderer.texture.TextureAtlas atlas = (net.minecraft.client.renderer.texture.TextureAtlas) Minecraft.getInstance()
                .getTextureManager().getTexture(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS);
        return atlas.getSprite(Identifier.parse(path));
    }

    private String getMaterialSpritePath(buildcraft.silicon.gate.EnumGateMaterial material) {
        switch (material) {
            case CLAY_BRICK: return "minecraft:block/bricks";
            case IRON: return "minecraft:block/iron_block";
            case NETHER_BRICK: return "minecraft:block/nether_bricks";
            case GOLD: return "minecraft:block/gold_block";
            default: return "minecraft:block/bricks";
        }
    }

    private String getModifierSpritePath(buildcraft.silicon.gate.EnumGateModifier mod) {
        switch (mod) {
            case LAPIS: return "minecraft:block/lapis_block";
            case QUARTZ: return "minecraft:block/quartz_block_top";
            case DIAMOND: return "minecraft:block/diamond_block";
            default: return "minecraft:block/stone";
        }
    }

    @Override
    public List<BakedQuad> bake(KeyPlugGate key) {

        return java.util.Collections.emptyList();
    }

    public List<MutableQuad> bakeForItem(buildcraft.silicon.gate.GateVariant variant) {
        List<MutableQuad> quads = new ArrayList<>();

        float baseZMin = 2f / 16f, baseZMax = 4.01f / 16f;
        float baseYMin = 5f / 16f, baseYMax = 11f / 16f;
        float baseXMin = 5f / 16f, baseXMax = 11f / 16f;

        TextureAtlasSprite matSprite = getSprite(getMaterialSpritePath(variant.material));
        buildBoxMutableNorth(quads, baseXMin, baseYMin, baseZMin, baseXMax, baseYMax, baseZMax, matSprite, true);

        if (variant.material != buildcraft.silicon.gate.EnumGateMaterial.CLAY_BRICK) {
            TextureAtlasSprite logicSprite = getSprite("buildcraftsilicon:block/gates/gate_" + variant.logic.tag);
            buildBoxMutableNorth(quads, 7f / 16f, 7f / 16f, 1.8f / 16f, 9f / 16f, 9f / 16f, 4.2f / 16f, logicSprite, false);
        }

        TextureAtlasSprite dynSprite = getSprite("buildcraftsilicon:block/gates/gate_off");
        buildBoxMutableNorth(quads, 6f / 16f, 6f / 16f, 1.9f / 16f, 10f / 16f, 10f / 16f, 4.1f / 16f, dynSprite, false);

        if (variant.modifier != buildcraft.silicon.gate.EnumGateModifier.NO_MODIFIER) {
            TextureAtlasSprite modSprite = getSprite(getModifierSpritePath(variant.modifier));
            buildBoxMutableNorth(quads, 5.5f / 16f, 5.5f / 16f, 1.8f / 16f, 6.5f / 16f, 6.5f / 16f, 4.2f / 16f, modSprite, false);
            buildBoxMutableNorth(quads, 5.5f / 16f, 9.5f / 16f, 1.8f / 16f, 6.5f / 16f, 10.5f / 16f, 4.2f / 16f, modSprite, false);
            buildBoxMutableNorth(quads, 9.5f / 16f, 5.5f / 16f, 1.8f / 16f, 10.5f / 16f, 6.5f / 16f, 4.2f / 16f, modSprite, false);
            buildBoxMutableNorth(quads, 9.5f / 16f, 9.5f / 16f, 1.8f / 16f, 10.5f / 16f, 10.5f / 16f, 4.2f / 16f, modSprite, false);
        }

        return quads;
    }

    private void buildBox(List<BakedQuad> list, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, TextureAtlasSprite sprite, Direction targetSide, boolean shade, int light) {
        AABB box = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        Vector3f center = new Vector3f((minX + maxX) / 2f, (minY + maxY) / 2f, (minZ + maxZ) / 2f);
        Vector3f radius = new Vector3f(maxX - center.x, maxY - center.y, maxZ - center.z);

        for (Direction face : Direction.values()) {

            if (face == Direction.EAST) continue;

            ModelUtil.UvFaceData uv = makeGateUVs(face);

            MutableQuad q = ModelUtil.createFace(face, center, radius, uv);
            q.texFromSprite(sprite);
            q.setTint(-1);
            q.setShade(shade);
            if (light > 0) {
                q.setLightEmission(light);
            }
            q.rotate(Direction.WEST, targetSide, 0.5f, 0.5f, 0.5f);

            q.setCalculatedNormal();

            q.setCalculatedDiffuse();

            list.add(q.toBakedBlock());
        }
    }

    private void buildBoxMutableNorth(List<MutableQuad> list, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, TextureAtlasSprite sprite, boolean shade) {
        Vector3f center = new Vector3f((minX + maxX) / 2f, (minY + maxY) / 2f, (minZ + maxZ) / 2f);
        Vector3f radius = new Vector3f(maxX - center.x, maxY - center.y, maxZ - center.z);

        for (Direction face : Direction.values()) {
            ModelUtil.UvFaceData uv = makeGateItemUVs(face);

            MutableQuad q = ModelUtil.createFace(face, center, radius, uv);
            q.texFromSprite(sprite);
            q.setTint(-1);
            q.setShade(shade);
            q.setCalculatedNormal();
            list.add(q);
        }
    }

    private static ModelUtil.UvFaceData makeGateUVs(Direction face) {
        ModelUtil.UvFaceData uv = new ModelUtil.UvFaceData();
        if (face == Direction.WEST || face == Direction.EAST) {
            uv.minU = 5f / 16f;
            uv.maxU = 11f / 16f;
            uv.minV = 5f / 16f;
            uv.maxV = 11f / 16f;
        } else {
            uv.minU = 2f / 16f;
            uv.maxU = 4f / 16f;
            uv.minV = 5f / 16f;
            uv.maxV = 11f / 16f;
        }
        return uv;
    }

    private static ModelUtil.UvFaceData makeGateItemUVs(Direction face) {
        ModelUtil.UvFaceData uv = new ModelUtil.UvFaceData();
        if (face == Direction.NORTH || face == Direction.SOUTH) {
            uv.minU = 5f / 16f;
            uv.maxU = 11f / 16f;
            uv.minV = 5f / 16f;
            uv.maxV = 11f / 16f;
        } else if (face == Direction.WEST || face == Direction.EAST) {
            uv.minU = 2f / 16f;
            uv.maxU = 4f / 16f;
            uv.minV = 5f / 16f;
            uv.maxV = 11f / 16f;
        } else {
            uv.minU = 5f / 16f;
            uv.maxU = 11f / 16f;
            uv.minV = 2f / 16f;
            uv.maxV = 4f / 16f;
        }
        return uv;
    }
}
