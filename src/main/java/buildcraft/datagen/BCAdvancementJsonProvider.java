package buildcraft.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

/** Emits the two advancements whose criteria JSON differs per node's registry format; the rest stay
 * handwritten and format-neutral. */
public final class BCAdvancementJsonProvider implements DataProvider {
   private final PackOutput.PathProvider pathProvider;

   public BCAdvancementJsonProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
      this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "advancement");
   }

   @Override
   public CompletableFuture<?> run(CachedOutput cache) {
      return CompletableFuture.allOf(
         DataProvider.saveStable(cache, sealingFluids(), this.pathProvider.json(Identifier.parse("buildcrafttransport:sealing_fluids"))),
         DataProvider.saveStable(cache, stickyDipping(), this.pathProvider.json(Identifier.parse("buildcraftenergy:sticky_dipping")))
      );
   }

   @Override
   public String getName() {
      return "BuildCraft Advancements";
   }

   private static JsonObject sealingFluids() {
      JsonObject root = new JsonObject();
      root.add("display", display("buildcrafttransport:waterproof", "sealing_fluids", false));
      root.addProperty("parent", "buildcrafttransport:pipe_dream");
      JsonObject criteria = new JsonObject();
      criteria.add("crafted_sealant", recipeCrafted("buildcrafttransport:pipe_sealant"));
      criteria.add("crafted_sealant_dye", recipeCrafted("buildcrafttransport:pipe_sealant_from_green_dye"));
      criteria.add("crafted_sealant_residue", recipeCrafted("buildcrafttransport:residue_to_pipe_sealant"));
      root.add("criteria", criteria);
      JsonArray anyOf = new JsonArray();
      anyOf.add("crafted_sealant");
      anyOf.add("crafted_sealant_dye");
      anyOf.add("crafted_sealant_residue");
      JsonArray requirements = new JsonArray();
      requirements.add(anyOf);
      root.add("requirements", requirements);
      return root;
   }

   private static JsonObject stickyDipping() {
      JsonObject root = new JsonObject();
      root.add("display", display("buildcraftenergy:glob_of_oil", "sticky_dipping", true));
      root.addProperty("parent", "buildcraftenergy:fine_riches");
      JsonObject blocks = new JsonObject();
      blocks.addProperty("blocks", "#buildcraftenergy:oil_fluids");
      JsonObject predicate = new JsonObject();
      predicate.add("block", blocks);
      JsonObject check = new JsonObject();
      //? if >= 26.3-pre-1 {
      check.addProperty("type", "minecraft:location_check");
      //?} else {
      /*check.addProperty("condition", "minecraft:location_check");
      *///?}
      check.add("predicate", predicate);
      JsonObject conditions = new JsonObject();
      //? if >= 26.3-pre-1 {
      conditions.add("player", check);
      //?} else {
      /*JsonArray player = new JsonArray();
      player.add(check);
      conditions.add("player", player);
      *///?}
      JsonObject inOil = new JsonObject();
      inOil.addProperty("trigger", "minecraft:location");
      inOil.add("conditions", conditions);
      JsonObject criteria = new JsonObject();
      criteria.add("in_oil", inOil);
      root.add("criteria", criteria);
      return root;
   }

   private static JsonObject recipeCrafted(String recipeId) {
      JsonObject conditions = new JsonObject();
      //? if >= 26.3-pre-1 {
      JsonArray recipes = new JsonArray();
      recipes.add(recipeId);
      conditions.add("recipes", recipes);
      //?} else {
      /*conditions.addProperty("recipe_id", recipeId);
      *///?}
      JsonObject criterion = new JsonObject();
      criterion.addProperty("trigger", "minecraft:recipe_crafted");
      criterion.add("conditions", conditions);
      return criterion;
   }

   private static JsonObject display(String iconId, String key, boolean hidden) {
      JsonObject icon = new JsonObject();
      icon.addProperty("id", iconId);
      JsonObject title = new JsonObject();
      title.addProperty("translate", "advancements.buildcraft." + key + ".title");
      JsonObject description = new JsonObject();
      description.addProperty("translate", "advancements.buildcraft." + key + ".description");
      JsonObject display = new JsonObject();
      display.add("icon", icon);
      display.add("title", title);
      display.add("description", description);
      if (hidden) {
         display.addProperty("hidden", true);
      }
      return display;
   }
}
