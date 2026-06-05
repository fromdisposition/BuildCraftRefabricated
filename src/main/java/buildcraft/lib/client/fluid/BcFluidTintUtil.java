package buildcraft.lib.client.fluid;

import java.io.IOException;
import java.io.InputStream;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import org.slf4j.Logger;

/**
 * BC fluid color pipeline (26.1.2):
 * <ul>
 *   <li><b>World / BER / GUI / blueprint</b> — {@link #bakedStillSpriteId} sprites from
 *       {@code BcFluidBakeSpriteSource}; draw with {@link #RENDER_TINT_WHITE} vertex/block tint
 *       (colors baked into atlas, no runtime multiply).</li>
 *   <li><b>Pipes</b> — {@link #heatStillWhiteSpriteId} + per-vertex {@link #vertexColorFromTemplate}
 *       sampled at normalized sprite UV ({@link #normalizedU}/{@link #normalizedV}).</li>
 * </ul>
 * Regression: {@code scripts/compare-fluid-colors.py} (pixel + mesh16 gates).
 */
public final class BcFluidTintUtil {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final int RENDER_TINT_WHITE = 0xFFFFFFFF;

    /** Frame-0 average gray per heat template ({@code heat_N_still}). */
    private static final int[] TEMPLATE_AVG_GRAY = {43, 43, 43};

    private static final int[][][] HEAT_STILL_LUMINANCE = new int[3][][];
    private static final int[][][] HEAT_STILL_ALPHA = new int[3][][];
    private static final int[][][] HEAT_FLOW_LUMINANCE = new int[3][][];
    private static final int[][][] HEAT_FLOW_ALPHA = new int[3][][];
    private static boolean clientTemplatesLoaded;

    private BcFluidTintUtil() {}

    public static Identifier heatStillSpriteId(int heat) {
        int h = Math.clamp(heat, 0, 2);
        return Identifier.fromNamespaceAndPath("buildcraftenergy", "block/fluids/heat_" + h + "_still");
    }

    public static Identifier heatFlowSpriteId(int heat) {
        int h = Math.clamp(heat, 0, 2);
        return Identifier.fromNamespaceAndPath("buildcraftenergy", "block/fluids/heat_" + h + "_flow");
    }

    public static Identifier heatStillWhiteSpriteId(int heat) {
        return heatStillSpriteId(heat).withSuffix("_white");
    }

    /** Baked world sprite id — BC 8.0 {@code AtlasSpriteFluid} output per fluid. */
    public static Identifier bakedStillSpriteId(String fluidRegName) {
        return Identifier.fromNamespaceAndPath("buildcraftenergy", "block/fluids/baked/" + fluidRegName);
    }

    public static Identifier bakedFlowSpriteId(String fluidRegName) {
        return Identifier.fromNamespaceAndPath("buildcraftenergy", "block/fluids/baked/" + fluidRegName + "_flow");
    }

    /**
     * BC 8.0 {@code AtlasSpriteFluid#recolourPixel}: per-channel gray from template, opaque alpha on visible pixels.
     */
    public static int bakeAtlasArgb(int srcArgb, int texLight, int texDark) {
        int a = (srcArgb >> 24) & 0xFF;
        if (a == 0) {
            return 0;
        }
        int r = recolorChannel(texDark >> 16 & 0xFF, texLight >> 16 & 0xFF, srcArgb >> 16 & 0xFF);
        int g = recolorChannel(texDark >> 8 & 0xFF, texLight >> 8 & 0xFF, srcArgb >> 8 & 0xFF);
        int b = recolorChannel(texDark & 0xFF, texLight & 0xFF, srcArgb & 0xFF);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static void bakeHeatImage(com.mojang.blaze3d.platform.NativeImage src, com.mojang.blaze3d.platform.NativeImage dst,
            int texLight, int texDark) {
        int w = src.getWidth();
        int h = src.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                dst.setPixel(x, y, bakeAtlasArgb(src.getPixel(x, y), texLight, texDark));
            }
        }
    }

    /** GUI / JEI average tint only — not used for world or pipe rendering. */
    public static int computeAverageGuiTint(int texLight, int texDark, int heat) {
        int h = Math.clamp(heat, 0, 2);
        int avgGray = Math.max(1, TEMPLATE_AVG_GRAY[h]);
        int r = recolorChannel(texDark >> 16 & 0xFF, texLight >> 16 & 0xFF, avgGray);
        int g = recolorChannel(texDark >> 8 & 0xFF, texLight >> 8 & 0xFF, avgGray);
        int b = recolorChannel(texDark & 0xFF, texLight & 0xFF, avgGray);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /** 8.0 {@code AtlasSpriteFluid} channel blend for gray value {@code v} (0–255). */
    public static int recolorChannel(int dark, int light, int v) {
        return (dark * (256 - v) + light * v) / 256;
    }

    public static int recolorRgb(int texLight, int texDark, int gray) {
        int r = recolorChannel(texDark >> 16 & 0xFF, texLight >> 16 & 0xFF, gray);
        int g = recolorChannel(texDark >> 8 & 0xFF, texLight >> 8 & 0xFF, gray);
        int b = recolorChannel(texDark & 0xFF, texLight & 0xFF, gray);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /** Direct vertex color for white-RGB sprites: final 8.0 recolor at template UV (0–1). */
    public static int vertexColorFromTemplate(int texLight, int texDark, int heat, float u, float v) {
        return vertexColorFromTemplate(texLight, texDark, heat, u, v, false);
    }

    /** @param flowing when true, uses the flow heat template instead of still. */
    public static int vertexColorFromTemplate(
            int texLight, int texDark, int heat, float u, float v, boolean flowing) {
        ensureClientTemplatesLoaded();
        int h = Math.clamp(heat, 0, 2);
        int lum = sampleLuminance(h, u, v, flowing);
        int alpha = sampleAlpha(h, u, v, flowing);
        if (lum <= 0 || alpha <= 0) {
            return 0;
        }
        int r = recolorChannel(texDark >> 16 & 0xFF, texLight >> 16 & 0xFF, lum);
        int g = recolorChannel(texDark >> 8 & 0xFF, texLight >> 8 & 0xFF, lum);
        int b = recolorChannel(texDark & 0xFF, texLight & 0xFF, lum);
        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }

    /** Map atlas sprite UV to normalized 0–1 coordinates within the sprite. */
    public static float normalizedU(float atlasU, float u0, float u1) {
        if (u1 <= u0) {
            return 0f;
        }
        return (atlasU - u0) / (u1 - u0);
    }

    public static float normalizedV(float atlasV, float v0, float v1) {
        if (v1 <= v0) {
            return 0f;
        }
        return (atlasV - v0) / (v1 - v0);
    }

    public static void reloadTemplates(ResourceManager manager) {
        for (int heat = 0; heat < 3; heat++) {
            loadTemplateFromManager(manager, heatStillSpriteId(heat),
                    HEAT_STILL_LUMINANCE, HEAT_STILL_ALPHA, heat, true);
            loadTemplateFromManager(manager, heatFlowSpriteId(heat),
                    HEAT_FLOW_LUMINANCE, HEAT_FLOW_ALPHA, heat, false);
        }
        clientTemplatesLoaded = true;
    }

    public static void ensureClientTemplatesLoaded() {
        if (clientTemplatesLoaded) {
            return;
        }
        for (int heat = 0; heat < 3; heat++) {
            loadTemplateFromClasspath(heatStillSpriteId(heat),
                    HEAT_STILL_LUMINANCE, HEAT_STILL_ALPHA, heat, true);
            loadTemplateFromClasspath(heatFlowSpriteId(heat),
                    HEAT_FLOW_LUMINANCE, HEAT_FLOW_ALPHA, heat, false);
        }
        clientTemplatesLoaded = true;
    }

    private static void loadTemplateFromManager(
            ResourceManager manager, Identifier id,
            int[][][] luminanceOut, int[][][] alphaOut,
            int heat, boolean updateAvgGray) {
        Identifier textureId = Identifier.fromNamespaceAndPath(
                id.getNamespace(), "textures/" + id.getPath() + ".png");
        try {
            Resource resource = manager.getResource(textureId).orElse(null);
            if (resource == null) {
                LOGGER.warn("Missing heat fluid template {}", textureId);
                luminanceOut[heat] = null;
                alphaOut[heat] = null;
                return;
            }
            try (InputStream in = resource.open()) {
                decodeTemplate(in, luminanceOut, alphaOut, heat, updateAvgGray);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to load heat fluid template {}", textureId, e);
            luminanceOut[heat] = null;
            alphaOut[heat] = null;
        }
    }

    private static void loadTemplateFromClasspath(
            Identifier id,
            int[][][] luminanceOut, int[][][] alphaOut,
            int heat, boolean updateAvgGray) {
        String path = "/assets/" + id.getNamespace() + "/textures/" + id.getPath() + ".png";
        try (InputStream in = BcFluidTintUtil.class.getResourceAsStream(path)) {
            if (in == null) {
                LOGGER.warn("Missing heat fluid template {}", path);
                luminanceOut[heat] = null;
                alphaOut[heat] = null;
                return;
            }
            decodeTemplate(in, luminanceOut, alphaOut, heat, updateAvgGray);
        } catch (IOException e) {
            LOGGER.warn("Failed to load heat fluid template for heat {}", heat, e);
            luminanceOut[heat] = null;
            alphaOut[heat] = null;
        }
    }

    private static void decodeTemplate(
            InputStream in,
            int[][][] luminanceOut, int[][][] alphaOut,
            int heat, boolean updateAvgGray) throws IOException {
        var img = javax.imageio.ImageIO.read(in);
        int w = img.getWidth();
        int frameH = w;
        int frames = Math.max(1, img.getHeight() / frameH);
        luminanceOut[heat] = new int[frames][w * frameH];
        alphaOut[heat] = new int[frames][w * frameH];
        long sum = 0;
        long count = 0;
        for (int f = 0; f < frames; f++) {
            int[] lumFrame = luminanceOut[heat][f];
            int[] alphaFrame = alphaOut[heat][f];
            int i = 0;
            for (int y = f * frameH; y < (f + 1) * frameH; y++) {
                for (int x = 0; x < w; x++) {
                    int argb = img.getRGB(x, y);
                    int a = (argb >> 24) & 0xFF;
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;
                    int lum = a == 0 ? 0 : (r + g + b) / 3;
                    lumFrame[i] = lum;
                    alphaFrame[i] = a;
                    if (updateAvgGray && f == 0 && lum > 0) {
                        sum += lum;
                        count++;
                    }
                    i++;
                }
            }
        }
        if (updateAvgGray && count > 0) {
            TEMPLATE_AVG_GRAY[heat] = (int) (sum / count);
        }
    }

    private static int sampleLuminance(int heat, float u, float v, boolean flowing) {
        return sampleChannel(
                flowing ? HEAT_FLOW_LUMINANCE : HEAT_STILL_LUMINANCE,
                heat, u, v, TEMPLATE_AVG_GRAY[Math.clamp(heat, 0, 2)]);
    }

    private static int sampleAlpha(int heat, float u, float v, boolean flowing) {
        return sampleChannel(
                flowing ? HEAT_FLOW_ALPHA : HEAT_STILL_ALPHA,
                heat, u, v, 0xFF);
    }

    private static int sampleChannel(
            int[][][] framesByHeat, int heat, float u, float v, int fallback) {
        int[][] frames = framesByHeat[heat];
        if (frames == null || frames.length == 0) {
            return fallback;
        }
        int frame = 0;
        int[] pixels = frames[frame];
        int size = (int) Math.sqrt(pixels.length);
        if (size <= 0) {
            return fallback;
        }
        float fu = u - (float) Math.floor(u);
        float fv = v - (float) Math.floor(v);
        int x = Math.clamp((int) (fu * size), 0, size - 1);
        int y = Math.clamp((int) (fv * size), 0, size - 1);
        return pixels[y * size + x];
    }
}
