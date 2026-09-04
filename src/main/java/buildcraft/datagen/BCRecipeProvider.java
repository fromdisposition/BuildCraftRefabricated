package buildcraft.datagen;

import buildcraft.lib.fabric.BcRegistryUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
//? if >= 1.21.10 {
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.minecraft.nbt.TagParser;
//?}
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
//? if >= 26.1 {
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe;
//?} else {
/*import net.minecraft.world.item.ItemStack;
*///?}
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;

/** Emits vanilla-type recipes through each node's own Recipe codec; BC-typed recipes (fuel/coolant/pipe colour/
 * facade swap) are format-neutral and stay handwritten. */
public final class BCRecipeProvider implements DataProvider {
   record Ing(String spec, String nbt) {
   }

   record Def(
      String id,
      String category,
      String group,
      String conditions,
      List<String> rows,
      Map<Character, Ing> keys,
      List<Ing> ingredients,
      String resultId,
      int count,
      String components
   ) {
   }

   static Ing i(String spec) {
      return new Ing(spec, null);
   }

   static Ing cd(String spec, String nbt) {
      return new Ing(spec, nbt);
   }

   static Map<Character, Ing> keys(Object... pairs) {
      Map<Character, Ing> map = new LinkedHashMap<>();
      for (int n = 0; n < pairs.length; n += 2) {
         map.put((Character) pairs[n], (Ing) pairs[n + 1]);
      }
      return map;
   }

   static Def shaped(String id, String category, String group, String conditions, List<String> rows, Map<Character, Ing> keys, String resultId, int count, String components) {
      return new Def(id, category, group, conditions, rows, keys, null, resultId, count, components);
   }

   static Def shapeless(String id, String category, String group, String conditions, List<Ing> ingredients, String resultId, int count, String components) {
      return new Def(id, category, group, conditions, null, null, ingredients, resultId, count, components);
   }

   private final PackOutput.PathProvider pathProvider;
   private final CompletableFuture<HolderLookup.Provider> registries;

   public BCRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
      this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
      this.registries = registries;
   }

   @Override
   public CompletableFuture<?> run(CachedOutput cache) {
      return this.registries.thenCompose(provider -> {
         RegistryOps<JsonElement> ops = provider.createSerializationContext(JsonOps.INSTANCE);
         HolderGetter<Item> items = provider.lookupOrThrow(Registries.ITEM);
         List<CompletableFuture<?>> futures = new ArrayList<>();
         for (Def def : BCRecipeDefs.ALL) {
            //? if >= 26.3-pre-1 {
            JsonElement json = Recipe.DIRECT_CODEC.encodeStart(ops, build(def, items, ops)).getOrThrow();
            //?} else {
            /*JsonElement json = Recipe.CODEC.encodeStart(ops, build(def, items, ops)).getOrThrow();
            *///?}
            restoreStringComponents(json, def.components());
            if (def.conditions() != null) {
               ((JsonObject) json).add("fabric:load_conditions", JsonParser.parseString(def.conditions()));
            }
            futures.add(DataProvider.saveStable(cache, json, this.pathProvider.json(Identifier.parse(def.id()))));
         }
         return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
      });
   }

   @Override
   public String getName() {
      return "BuildCraft Recipes";
   }

   private static Recipe<?> build(Def def, HolderGetter<Item> items, RegistryOps<JsonElement> ops) {
      CraftingBookCategory category = CraftingBookCategory.valueOf(def.category().toUpperCase(Locale.ROOT));
      String group = def.group() == null ? "" : def.group();
      DataComponentPatch patch = patchOf(def.components(), ops);
      Holder<Item> resultItem = BcRegistryUtil.itemHolder(itemOf(def.resultId()));
      //? if >= 26.1 {
      ItemStackTemplate result = new ItemStackTemplate(resultItem, def.count(), patch);
      Recipe.CommonInfo common = new Recipe.CommonInfo(true);
      CraftingRecipe.CraftingBookInfo book = new CraftingRecipe.CraftingBookInfo(category, group);
      if (def.rows() != null) {
         return new ShapedRecipe(common, book, pattern(def, items), result);
      }
      return new ShapelessRecipe(common, book, result, ingredientList(def, items));
      //?} else {
      /*ItemStack result = new ItemStack(resultItem, def.count());
      result.applyComponents(patch);
      if (def.rows() != null) {
         return new ShapedRecipe(group, category, pattern(def, items), result, true);
      }
      net.minecraft.core.NonNullList<Ingredient> list = net.minecraft.core.NonNullList.create();
      list.addAll(ingredientList(def, items));
      return new ShapelessRecipe(group, category, result, list);
      *///?}
   }

   private static ShapedRecipePattern pattern(Def def, HolderGetter<Item> items) {
      Map<Character, Ingredient> resolved = new LinkedHashMap<>();
      def.keys().forEach((symbol, ing) -> resolved.put(symbol, ingredientOf(ing, items)));
      return ShapedRecipePattern.of(resolved, def.rows());
   }

   private static List<Ingredient> ingredientList(Def def, HolderGetter<Item> items) {
      List<Ingredient> list = new ArrayList<>();
      for (Ing ing : def.ingredients()) {
         list.add(ingredientOf(ing, items));
      }
      return list;
   }

   private static Ingredient ingredientOf(Ing ing, HolderGetter<Item> items) {
      Ingredient base;
      if (ing.spec().startsWith("#")) {
         TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(ing.spec().substring(1)));
         //? if >= 1.21.10 {
         base = Ingredient.of(items.getOrThrow(tag));
         //?} else {
         /*base = Ingredient.of(tag);
         *///?}
      } else {
         base = Ingredient.of(itemOf(ing.spec()));
      }

      if (ing.nbt() == null) {
         return base;
      }
      //? if >= 1.21.10 {
      try {
         return DefaultCustomIngredients.customData(base, TagParser.parseCompoundFully(ing.nbt()));
      } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
         throw new IllegalStateException(ing.nbt(), e);
      }
      //?} else {
      /*// 1.21.1 keeps the converter-era behaviour: the gate recipes accept any base item there.
      return base;
      *///?}
   }

   /** The patch codec re-encodes SNBT-string components as JSON objects, turning NBT bytes into ints and
    * breaking exact-NBT matching; restore the authored string form. */
   private static void restoreStringComponents(JsonElement json, String rawComponents) {
      if (rawComponents == null) {
         return;
      }
      JsonObject result = json.getAsJsonObject().getAsJsonObject("result");
      JsonObject components = result == null ? null : result.getAsJsonObject("components");
      if (components == null) {
         return;
      }
      for (Map.Entry<String, JsonElement> e : JsonParser.parseString(rawComponents).getAsJsonObject().entrySet()) {
         if (e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isString() && components.has(e.getKey())) {
            components.add(e.getKey(), e.getValue());
         }
      }
   }

   private static Item itemOf(String id) {
      return Objects.requireNonNull(BcRegistryUtil.getItem(Identifier.parse(id)), id);
   }

   private static DataComponentPatch patchOf(String rawComponents, RegistryOps<JsonElement> ops) {
      if (rawComponents == null) {
         return DataComponentPatch.EMPTY;
      }

      JsonObject obj = JsonParser.parseString(rawComponents).getAsJsonObject();
      //? if < 1.21.10 {
      /*JsonElement cmd = obj.get("minecraft:custom_model_data");
      if (cmd != null && cmd.isJsonObject()) {
         JsonObject cmdObj = cmd.getAsJsonObject();
         if (cmdObj.has("floats") && !cmdObj.getAsJsonArray("floats").isEmpty()) {
            obj.addProperty("minecraft:custom_model_data", cmdObj.getAsJsonArray("floats").get(0).getAsInt());
         } else {
            obj.remove("minecraft:custom_model_data");
         }
      }
      *///?}
      return DataComponentPatch.CODEC.parse(ops, obj).getOrThrow();
   }
}
