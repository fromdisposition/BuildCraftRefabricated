package buildcraft.lib.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;

/**
 * Intercepts Minecraft's vanilla model pipeline for BuildCraft's expression-based model format.
 *
 * BC engine models (models/compat/engine_*.json) use string expressions in "from"/"to" arrays
 * (e.g. "4 + progress_size") which the vanilla CuboidModelElement deserializer cannot parse.
 * These files are loaded correctly by ModelHolderVariable via JsonVariableModel.deserialize();
 * this deserializer exists solely to prevent the vanilla pipeline from crashing on them.
 *
 * JSON files opt in by declaring "type": "buildcraftlib:variable".
 */
public final class VariableModelDeserializer implements UnbakedModelDeserializer {

    public static final Identifier TYPE_ID = Identifier.fromNamespaceAndPath("buildcraftlib", "variable");
    public static final VariableModelDeserializer INSTANCE = new VariableModelDeserializer();

    private VariableModelDeserializer() {}

    @Override
    public UnbakedModel deserialize(JsonObject json, JsonDeserializationContext ctx) {
        return Stub.INSTANCE;
    }

    /** No-op placeholder — BC loads these via ModelHolderVariable, not the vanilla bake pipeline. */
    public static final class Stub implements UnbakedModel {
        public static final Stub INSTANCE = new Stub();

        private Stub() {}
    }
}
