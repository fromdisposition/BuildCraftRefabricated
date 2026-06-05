package buildcraft.core;

public final class BCUnifiedClientConfig {
    private BCUnifiedClientConfig() {}

    public static void onDisplayConfigReloaded() {
        try {
            Class<?> cls = Class.forName("buildcraft.transport.client.model.PipeBaseModelGenStandard");
            java.lang.reflect.Method method = cls.getMethod("onColorBlindToggle");
            method.invoke(null);
        } catch (ClassNotFoundException ignored) {

        } catch (ReflectiveOperationException ignored) {

        }
    }
}
