package buildcraft.fabric.client.event;

import buildcraft.fabric.event.Event;

public final class BCClientEvents {
    private BCClientEvents() {}

    public static class RegisterRenderPipelinesEvent extends Event {
        public void registerPipeline(Object pipeline) {}
    }

    public static class RegisterSpriteSourcesEvent extends Event {
        public void register(Object id, Object codec) {}
    }

    public static class ModelEvent extends Event {
        public static class BakingCompleted extends ModelEvent {}
    }

    public static class AddClientReloadListenersEvent extends Event {
        public void addListener(Object id, Object listener) {}
    }

    public static class RegisterMenuScreensEvent extends Event {
        public void register(
                net.minecraft.world.inventory.MenuType<?> type,
                net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor<?, ?> factory) {}
    }

    public static class RegisterGuiLayersEvent extends Event {
        public void registerAboveAll(Object id, Object renderer) {}
    }

    public static class RegisterColorHandlersEvent extends Event {
        public static class ItemTintSources extends Event {
            public void register(Object id, Object codec) {}
        }
    }

    public static class EntityRenderersEvent extends Event {
        public static class RegisterRenderers extends Event {
            public <T extends net.minecraft.world.entity.Entity> void registerEntityRenderer(
                    net.minecraft.world.entity.EntityType<? extends T> type,
                    net.minecraft.client.renderer.entity.EntityRendererProvider<T> provider) {}

            public void registerBlockEntityRenderer(
                    net.minecraft.world.level.block.entity.BlockEntityType<?> type,
                    net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider<?, ?> provider) {}
        }
    }

    public static class ClientPlayerNetworkEvent extends Event {
        public static class LoggingIn extends ClientPlayerNetworkEvent {}
        public static class LoggingOut extends ClientPlayerNetworkEvent {}
    }

    public static class RenderLevelStageEvent extends Event {
        public static class AfterTranslucentBlocks extends RenderLevelStageEvent {}
    }

    public static class ClientTickEvent extends Event {
        public static class Post extends ClientTickEvent {}
    }
}

