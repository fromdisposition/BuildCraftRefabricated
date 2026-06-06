package buildcraft.builders.snapshot;

import buildcraft.lib.fabric.loader.FabricModResources;
import buildcraft.lib.misc.JsonUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.commons.lang3.tuple.Pair;

public class RulesLoader {
   private static final Gson GSON = JsonUtil.registerNbtSerializersDeserializers(new GsonBuilder())
      .registerTypeAdapter(
         BlockPos.class,
         (JsonDeserializer)(json, typeOfT, context) -> new BlockPos(
            json.getAsJsonArray().get(0).getAsInt(), json.getAsJsonArray().get(1).getAsInt(), json.getAsJsonArray().get(2).getAsInt()
         )
      )
      .registerTypeAdapter(RequiredExtractor.class, RequiredExtractor.DESERIALIZER)
      .registerTypeAdapter(EnumNbtCompareOperation.class, EnumNbtCompareOperation.DESERIALIZER)
      .registerTypeAdapter(NbtPath.class, NbtPath.DESERIALIZER)
      .registerTypeAdapterFactory(JsonSelector.TYPE_ADAPTER_FACTORY)
      .registerTypeAdapterFactory(NbtRef.TYPE_ADAPTER_FACTORY)
      .create();
   private static final List<JsonRule> RULES = new ArrayList<>();
   public static final Set<String> READ_DOMAINS = new HashSet<>();
   private static final LoadingCache<Pair<BlockState, CompoundTag>, Set<JsonRule>> BLOCK_RULES_CACHE = CacheBuilder.newBuilder()
      .expireAfterAccess(5L, TimeUnit.MINUTES)
      .build(CacheLoader.from(pair -> getBlockRulesInternal((BlockState)pair.getLeft(), (CompoundTag)pair.getRight())));
   private static final String COMPAT_ASSET_DOMAIN = "buildcraftbuilders";

   public static void loadAll() {
      RULES.clear();
      READ_DOMAINS.clear();

      for (String modId : FabricModResources.getModIds()) {
         loadDomain(modId);
      }

      loadDomain("buildcraftbuilders");
      READ_DOMAINS.add("minecraft");
   }

   private static void loadDomain(String domain) {
      if (!READ_DOMAINS.contains(domain)) {
         String base = "assets/" + domain + "/compat/buildcraft/builders/";
         InputStream inputStream = RulesLoader.class.getClassLoader().getResourceAsStream(base + "index.json");
         if (inputStream != null) {
            List<String> index = GSON.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), (new TypeToken<List<String>>() {}).getType());
            index.stream()
               .map(name -> base + name + ".json")
               .map(name -> {
                  InputStream resourceAsStream = RulesLoader.class.getClassLoader().getResourceAsStream(name);
                  if (resourceAsStream == null) {
                     throw new RuntimeException(new IOException("Can't read " + name));
                  } else {
                     return resourceAsStream;
                  }
               })
               .flatMap(localInputStream -> {
                  List<JsonRule> rules = GSON.fromJson(new InputStreamReader(localInputStream), (new TypeToken<List<JsonRule>>() {}).getType());
                  return rules.stream();
               })
               .forEach(RULES::add);
            READ_DOMAINS.add(domain);
         }
      }
   }

   private static Set<JsonRule> getBlockRulesInternal(BlockState blockState, CompoundTag tileNbt) {
      return RULES.stream()
         .filter(rule -> rule.selectors != null)
         .filter(
            rule -> rule.selectors
               .stream()
               .anyMatch(
                  selector -> selector.matches(
                     base -> {
                        boolean complex = base.contains("[");
                        Identifier blockId = Identifier.parse(complex ? base.substring(0, base.indexOf("[")) : base);
                        Block block = (Block)BuiltInRegistries.BLOCK.getValue(blockId);
                        return block == blockState.getBlock()
                           && (
                              !complex
                                 || Arrays.stream(base.substring(base.indexOf("[") + 1, base.indexOf("]")).split(", "))
                                    .map(nameValue -> nameValue.split("="))
                                    .allMatch(
                                       nameValue -> blockState.getProperties()
                                          .stream()
                                          .filter(property -> property.getName().equals(nameValue[0]))
                                          .findFirst()
                                          .map(property -> getPropertyStringValue(blockState, property))
                                          .map(nameValue[1]::equals)
                                          .orElse(false)
                                    )
                           );
                     },
                     tileNbt == null ? new CompoundTag() : tileNbt
                  )
               )
         )
         .collect(Collectors.toCollection(HashSet::new));
   }

   public static Set<JsonRule> getRules(BlockState blockState, CompoundTag tileNbt) {
      return (Set<JsonRule>)BLOCK_RULES_CACHE.getUnchecked(Pair.of(blockState, tileNbt));
   }

   public static Set<JsonRule> getRules(Identifier entityId, CompoundTag tileNbt) {
      return RULES.stream()
         .filter(rule -> rule.selectors != null)
         .filter(rule -> rule.selectors.stream().anyMatch(selector -> selector.matches(entityId.toString()::equals, tileNbt)))
         .collect(Collectors.toCollection(HashSet::new));
   }

   private static <T extends Comparable<T>> String getPropertyStringValue(BlockState state, Property<T> property) {
      return property.getName(state.getValue(property));
   }
}
