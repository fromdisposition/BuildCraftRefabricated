package buildcraft.api.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.GsonBuilder;

import buildcraft.fabric.event.Event;

public abstract class EventBuildCraftReload extends Event {

    private static final List<Consumer<BeforeClear>> BEFORE_CLEAR = new ArrayList<>();
    private static final List<Consumer<PreLoad>> PRE_LOAD = new ArrayList<>();
    private static final List<Consumer<PopulateGson>> POPULATE_GSON = new ArrayList<>();
    private static final List<Consumer<PostLoad>> POST_LOAD = new ArrayList<>();
    private static final List<Consumer<FinishLoad>> FINISH_LOAD = new ArrayList<>();

    public static void onBeforeClear(Consumer<BeforeClear> listener) {
        BEFORE_CLEAR.add(listener);
    }

    public static void onPreLoad(Consumer<PreLoad> listener) {
        PRE_LOAD.add(listener);
    }

    public static void onPopulateGson(Consumer<PopulateGson> listener) {
        POPULATE_GSON.add(listener);
    }

    public static void onPostLoad(Consumer<PostLoad> listener) {
        POST_LOAD.add(listener);
    }

    public static void onFinishLoad(Consumer<FinishLoad> listener) {
        FINISH_LOAD.add(listener);
    }

    public static void fireBeforeClear(BeforeClear event) {
        BEFORE_CLEAR.forEach(listener -> listener.accept(event));
    }

    public static void firePreLoad(PreLoad event) {
        PRE_LOAD.forEach(listener -> listener.accept(event));
    }

    public static void firePopulateGson(PopulateGson event) {
        POPULATE_GSON.forEach(listener -> listener.accept(event));
    }

    public static void firePostLoad(PostLoad event) {
        POST_LOAD.forEach(listener -> listener.accept(event));
    }

    public static void fireFinishLoad(FinishLoad event) {
        FINISH_LOAD.forEach(listener -> listener.accept(event));
    }

    public final IReloadableRegistryManager manager;

    public final Set<IReloadableRegistry<?>> reloadingRegistries;

    public EventBuildCraftReload(IReloadableRegistryManager manager, Set<IReloadableRegistry<?>> reloadingRegistries) {
        this.manager = manager;
        this.reloadingRegistries = reloadingRegistries;
    }

    public static class BeforeClear extends EventBuildCraftReload {
        public BeforeClear(IReloadableRegistryManager manager,
            @Nullable Set<IReloadableRegistry<?>> reloadingRegistries) {
            super(manager, reloadingRegistries);
        }
    }

    public static class PreLoad extends EventBuildCraftReload {
        public PreLoad(IReloadableRegistryManager manager, @Nullable Set<IReloadableRegistry<?>> reloadingRegistries) {
            super(manager, reloadingRegistries);
        }
    }

    public static class PopulateGson extends EventBuildCraftReload {

        public final GsonBuilder gsonBuilder;

        public PopulateGson(IReloadableRegistryManager manager,
            @Nullable Set<IReloadableRegistry<?>> reloadingRegistries, GsonBuilder gsonBuilder) {
            super(manager, reloadingRegistries);
            this.gsonBuilder = gsonBuilder;
        }
    }

    public static class PostLoad extends EventBuildCraftReload {
        public PostLoad(IReloadableRegistryManager manager, @Nullable Set<IReloadableRegistry<?>> reloadingRegistries) {
            super(manager, reloadingRegistries);
        }
    }

    public static class FinishLoad extends EventBuildCraftReload {
        public FinishLoad(IReloadableRegistryManager manager,
            @Nullable Set<IReloadableRegistry<?>> reloadingRegistries) {
            super(manager, reloadingRegistries);
        }
    }
}

