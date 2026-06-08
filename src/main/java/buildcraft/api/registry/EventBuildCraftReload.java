package buildcraft.api.registry;

import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public abstract class EventBuildCraftReload {
   private static final List<Consumer<EventBuildCraftReload.BeforeClear>> BEFORE_CLEAR = new ArrayList<>();
   private static final List<Consumer<EventBuildCraftReload.PreLoad>> PRE_LOAD = new ArrayList<>();
   private static final List<Consumer<EventBuildCraftReload.PopulateGson>> POPULATE_GSON = new ArrayList<>();
   private static final List<Consumer<EventBuildCraftReload.PostLoad>> POST_LOAD = new ArrayList<>();
   private static final List<Consumer<EventBuildCraftReload.FinishLoad>> FINISH_LOAD = new ArrayList<>();
   public final IReloadableRegistryManager manager;
   public final Set<IReloadableRegistry<?>> reloadingRegistries;

   public static void onBeforeClear(Consumer<EventBuildCraftReload.BeforeClear> listener) {
      BEFORE_CLEAR.add(listener);
   }

   public static void onPreLoad(Consumer<EventBuildCraftReload.PreLoad> listener) {
      PRE_LOAD.add(listener);
   }

   public static void onPopulateGson(Consumer<EventBuildCraftReload.PopulateGson> listener) {
      POPULATE_GSON.add(listener);
   }

   public static void onPostLoad(Consumer<EventBuildCraftReload.PostLoad> listener) {
      POST_LOAD.add(listener);
   }

   public static void onFinishLoad(Consumer<EventBuildCraftReload.FinishLoad> listener) {
      FINISH_LOAD.add(listener);
   }

   public static void fireBeforeClear(EventBuildCraftReload.BeforeClear event) {
      BEFORE_CLEAR.forEach(listener -> listener.accept(event));
   }

   public static void firePreLoad(EventBuildCraftReload.PreLoad event) {
      PRE_LOAD.forEach(listener -> listener.accept(event));
   }

   public static void firePopulateGson(EventBuildCraftReload.PopulateGson event) {
      POPULATE_GSON.forEach(listener -> listener.accept(event));
   }

   public static void firePostLoad(EventBuildCraftReload.PostLoad event) {
      POST_LOAD.forEach(listener -> listener.accept(event));
   }

   public static void fireFinishLoad(EventBuildCraftReload.FinishLoad event) {
      FINISH_LOAD.forEach(listener -> listener.accept(event));
   }

   public EventBuildCraftReload(IReloadableRegistryManager manager, Set<IReloadableRegistry<?>> reloadingRegistries) {
      this.manager = manager;
      this.reloadingRegistries = reloadingRegistries;
   }

   public static class BeforeClear extends EventBuildCraftReload {
      public BeforeClear(IReloadableRegistryManager manager, @Nullable Set<IReloadableRegistry<?>> reloadingRegistries) {
         super(manager, reloadingRegistries);
      }
   }

   public static class FinishLoad extends EventBuildCraftReload {
      public FinishLoad(IReloadableRegistryManager manager, @Nullable Set<IReloadableRegistry<?>> reloadingRegistries) {
         super(manager, reloadingRegistries);
      }
   }

   public static class PopulateGson extends EventBuildCraftReload {
      public final GsonBuilder gsonBuilder;

      public PopulateGson(IReloadableRegistryManager manager, @Nullable Set<IReloadableRegistry<?>> reloadingRegistries, GsonBuilder gsonBuilder) {
         super(manager, reloadingRegistries);
         this.gsonBuilder = gsonBuilder;
      }
   }

   public static class PostLoad extends EventBuildCraftReload {
      public PostLoad(IReloadableRegistryManager manager, @Nullable Set<IReloadableRegistry<?>> reloadingRegistries) {
         super(manager, reloadingRegistries);
      }
   }

   public static class PreLoad extends EventBuildCraftReload {
      public PreLoad(IReloadableRegistryManager manager, @Nullable Set<IReloadableRegistry<?>> reloadingRegistries) {
         super(manager, reloadingRegistries);
      }
   }
}
