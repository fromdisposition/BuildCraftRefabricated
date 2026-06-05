package buildcraft.fabric;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import buildcraft.lib.client.render.BCLibRenderTypes;

public final class BCRenderPipelinesFabric {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier LED_PIPELINE_ID =
            Identifier.fromNamespaceAndPath("buildcraftlib", "pipeline/led");
    private static boolean registered;

    private BCRenderPipelinesFabric() {}

    public static void register() {
        if (registered) {
            return;
        }
        try {
            if (registerViaStaticMethod()) {
                registered = true;
                return;
            }
            if (registerViaMappersOrMaps()) {
                registered = true;
                return;
            }
            LOGGER.warn("Could not hook custom render pipeline registry; LED pipeline may be unavailable");
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to register BuildCraft render pipeline", e);
        }
    }

    private static boolean registerViaStaticMethod() throws ReflectiveOperationException {
        for (Method method : RenderPipelines.class.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            Class<?>[] args = method.getParameterTypes();
            if (args.length == 1 && args[0].isInstance(BCLibRenderTypes.LED_PIPELINE)) {
                method.setAccessible(true);
                method.invoke(null, BCLibRenderTypes.LED_PIPELINE);
                return true;
            }
            if (args.length == 2 && args[0] == Identifier.class && args[1].isInstance(BCLibRenderTypes.LED_PIPELINE)) {
                method.setAccessible(true);
                method.invoke(null, LED_PIPELINE_ID, BCLibRenderTypes.LED_PIPELINE);
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static boolean registerViaMappersOrMaps() throws ReflectiveOperationException {
        for (Field field : RenderPipelines.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            Object value = field.get(null);
            if (value == null) {
                continue;
            }

            Class<?> type = value.getClass();

            if (type.getName().contains("LateBoundIdMapper")) {
                Method put = type.getMethod("put", Object.class, Object.class);
                put.invoke(value, LED_PIPELINE_ID, BCLibRenderTypes.LED_PIPELINE);
                return true;
            }
            if (value instanceof Map map) {
                map.put(LED_PIPELINE_ID, BCLibRenderTypes.LED_PIPELINE);
                return true;
            }
        }
        return false;
    }
}
