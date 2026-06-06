package buildcraft.fabric.client.event;

import buildcraft.fabric.event.Event;
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BCClientEvents {
   private BCClientEvents() {
   }

   public static class AddClientReloadListenersEvent extends Event {
      public void addListener(Object id, Object listener) {
      }
   }

   public static class ClientPlayerNetworkEvent extends Event {
      public static class LoggingIn extends BCClientEvents.ClientPlayerNetworkEvent {
      }

      public static class LoggingOut extends BCClientEvents.ClientPlayerNetworkEvent {
      }
   }

   public static class ClientTickEvent extends Event {
      public static class Post extends BCClientEvents.ClientTickEvent {
      }
   }

   public static class EntityRenderersEvent extends Event {
      public static class RegisterRenderers extends Event {
         public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
         }

         public void registerBlockEntityRenderer(BlockEntityType<?> type, BlockEntityRendererProvider<?, ?> provider) {
         }
      }
   }

   public static class ModelEvent extends Event {
      public static class BakingCompleted extends BCClientEvents.ModelEvent {
      }
   }

   public static class RegisterColorHandlersEvent extends Event {
      public static class ItemTintSources extends Event {
         public void register(Object id, Object codec) {
         }
      }
   }

   public static class RegisterGuiLayersEvent extends Event {
      public void registerAboveAll(Object id, Object renderer) {
      }
   }

   public static class RegisterMenuScreensEvent extends Event {
      public void register(MenuType<?> type, ScreenConstructor<?, ?> factory) {
      }
   }

   public static class RegisterRenderPipelinesEvent extends Event {
      public void registerPipeline(Object pipeline) {
      }
   }

   public static class RegisterSpriteSourcesEvent extends Event {
      public void register(Object id, Object codec) {
      }
   }

   public static class RenderLevelStageEvent extends Event {
      public static class AfterTranslucentBlocks extends BCClientEvents.RenderLevelStageEvent {
      }
   }
}
