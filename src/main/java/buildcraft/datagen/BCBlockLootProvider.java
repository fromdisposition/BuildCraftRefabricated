package buildcraft.datagen;

import buildcraft.lib.fabric.BcRegistryUtil;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
//? if >= 26.1 {
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
//?} else {
/*import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
*///?}
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;

public final class BCBlockLootProvider
   //? if >= 26.1 {
   extends FabricBlockLootSubProvider {
   //?} else {
   /*extends FabricBlockLootTableProvider {
   *///?}
   private static final List<String> DROP_SELF = List.of(
      "buildcraftbuilders:architect",
      "buildcraftbuilders:builder",
      "buildcraftbuilders:construction_marker",
      "buildcraftbuilders:filler",
      "buildcraftbuilders:frame",
      "buildcraftbuilders:library",
      "buildcraftbuilders:quarry",
      "buildcraftbuilders:replacer",
      "buildcraftcore:decorated_blueprint",
      "buildcraftcore:decorated_destroy",
      "buildcraftcore:decorated_laser",
      "buildcraftcore:decorated_template",
      "buildcraftcore:engine_creative",
      "buildcraftcore:engine_redstone",
      "buildcraftcore:power_tester",
      "buildcraftenergy:engine_iron",
      "buildcraftenergy:engine_stone",
      "buildcraftfactory:autoworkbench_item",
      "buildcraftfactory:chute",
      "buildcraftfactory:distiller",
      "buildcraftfactory:flood_gate",
      "buildcraftfactory:heat_exchange",
      "buildcraftfactory:mining_well",
      "buildcraftfactory:pump",
      "buildcraftfactory:tank",
      "buildcraftrobotics:requester",
      "buildcraftrobotics:zone_planner",
      "buildcraftsilicon:advanced_crafting_table",
      "buildcraftsilicon:assembly_table",
      "buildcraftsilicon:charging_table",
      "buildcraftsilicon:integration_table",
      "buildcraftsilicon:laser",
      "buildcraftsilicon:packager",
      "buildcraftsilicon:programming_table",
      "buildcraftsilicon:stamping_table",
      "buildcrafttransport:filtered_buffer"
   );

   public BCBlockLootProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
      super(output, registries);
   }

   @Override
   public void generate() {
      for (String id : DROP_SELF) {
         this.dropSelf(Objects.requireNonNull(BcRegistryUtil.getBlock(Identifier.parse(id)), id));
      }
   }
}
