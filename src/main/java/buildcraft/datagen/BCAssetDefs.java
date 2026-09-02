package buildcraft.datagen;

import java.util.List;
import java.util.Map;

final class BCAssetDefs {
   record Dispatch(String id, String fallback, Map<Integer, String> entries) {
   }

   record FlatModel(String id, String parent, String texture) {
   }

   record BlockModel(String id, String parent, Map<String, String> textures) {
   }

   record Tag(String id, List<String> values) {
   }

   static final Map<String, String> ITEM_MODELS = Map.ofEntries(
      Map.entry("buildcraftbuilders:architect", "buildcraftbuilders:item/architect"),
      Map.entry("buildcraftbuilders:blueprint_clean", "buildcraftbuilders:item/blueprint_clean"),
      Map.entry("buildcraftbuilders:blueprint_used", "buildcraftbuilders:item/blueprint_used"),
      Map.entry("buildcraftbuilders:builder", "buildcraftbuilders:item/builder"),
      Map.entry("buildcraftbuilders:construction_marker", "buildcraftbuilders:item/construction_marker"),
      Map.entry("buildcraftbuilders:filler", "buildcraftbuilders:item/filler"),
      Map.entry("buildcraftbuilders:filler_planner", "buildcraftbuilders:item/filler_planner"),
      Map.entry("buildcraftbuilders:frame", "buildcraftbuilders:item/frame"),
      Map.entry("buildcraftbuilders:library", "buildcraftbuilders:item/library"),
      Map.entry("buildcraftbuilders:quarry", "buildcraftbuilders:item/quarry"),
      Map.entry("buildcraftbuilders:replacer", "buildcraftbuilders:item/replacer"),
      Map.entry("buildcraftbuilders:schematic_single_clean", "buildcraftbuilders:item/schematic_single_clean"),
      Map.entry("buildcraftbuilders:schematic_single_used", "buildcraftbuilders:item/schematic_single_used"),
      Map.entry("buildcraftbuilders:template_clean", "buildcraftbuilders:item/template_clean"),
      Map.entry("buildcraftbuilders:template_used", "buildcraftbuilders:item/template_used"),
      Map.entry("buildcraftcore:decorated_blueprint", "buildcraftcore:block/decorated/blueprint"),
      Map.entry("buildcraftcore:decorated_destroy", "buildcraftcore:block/decorated/destroy"),
      Map.entry("buildcraftcore:decorated_laser", "buildcraftcore:block/decorated/laser_back"),
      Map.entry("buildcraftcore:decorated_template", "buildcraftcore:block/decorated/template"),
      Map.entry("buildcraftcore:diamond_shard", "buildcraftcore:item/diamond_shard"),
      Map.entry("buildcraftcore:engine_creative", "buildcraftcore:item/engine_creative"),
      Map.entry("buildcraftcore:engine_redstone", "buildcraftcore:item/engine_redstone"),
      Map.entry("buildcraftcore:gear_diamond", "buildcraftcore:item/gear_diamond"),
      Map.entry("buildcraftcore:gear_gold", "buildcraftcore:item/gear_gold"),
      Map.entry("buildcraftcore:gear_iron", "buildcraftcore:item/gear_iron"),
      Map.entry("buildcraftcore:gear_stone", "buildcraftcore:item/gear_stone"),
      Map.entry("buildcraftcore:gear_wood", "buildcraftcore:item/gear_wood"),
      Map.entry("buildcraftcore:marker_connector", "buildcraftcore:item/marker_connector"),
      Map.entry("buildcraftcore:marker_path", "buildcraftcore:item/marker_path"),
      Map.entry("buildcraftcore:marker_volume", "buildcraftcore:item/marker_volume"),
      Map.entry("buildcraftcore:power_tester", "buildcraftcore:block/power_tester"),
      Map.entry("buildcraftcore:spring_oil", "buildcraftcore:item/spring_oil"),
      Map.entry("buildcraftcore:spring_water", "buildcraftcore:item/spring_water"),
      Map.entry("buildcraftcore:volume_box", "buildcraftcore:item/volume_box"),
      Map.entry("buildcraftcore:wrench", "buildcraftcore:item/wrench"),
      Map.entry("buildcraftenergy:engine_iron", "buildcraftenergy:item/engine_iron"),
      Map.entry("buildcraftenergy:engine_rf", "buildcraftenergy:item/engine_rf"),
      Map.entry("buildcraftenergy:engine_stone", "buildcraftenergy:item/engine_stone"),
      Map.entry("buildcraftenergy:fuel_dense_bucket", "buildcraftenergy:item/fluid_buckets/fuel_dense_bucket"),
      Map.entry("buildcraftenergy:fuel_dense_heat_1_bucket", "buildcraftenergy:item/fluid_buckets/fuel_dense_heat_1_bucket"),
      Map.entry("buildcraftenergy:fuel_dense_heat_2_bucket", "buildcraftenergy:item/fluid_buckets/fuel_dense_heat_2_bucket"),
      Map.entry("buildcraftenergy:fuel_gaseous_bucket", "buildcraftenergy:item/fluid_buckets/fuel_gaseous_bucket"),
      Map.entry("buildcraftenergy:fuel_gaseous_heat_1_bucket", "buildcraftenergy:item/fluid_buckets/fuel_gaseous_heat_1_bucket"),
      Map.entry("buildcraftenergy:fuel_gaseous_heat_2_bucket", "buildcraftenergy:item/fluid_buckets/fuel_gaseous_heat_2_bucket"),
      Map.entry("buildcraftenergy:fuel_light_bucket", "buildcraftenergy:item/fluid_buckets/fuel_light_bucket"),
      Map.entry("buildcraftenergy:fuel_light_heat_1_bucket", "buildcraftenergy:item/fluid_buckets/fuel_light_heat_1_bucket"),
      Map.entry("buildcraftenergy:fuel_light_heat_2_bucket", "buildcraftenergy:item/fluid_buckets/fuel_light_heat_2_bucket"),
      Map.entry("buildcraftenergy:fuel_mixed_heavy_bucket", "buildcraftenergy:item/fluid_buckets/fuel_mixed_heavy_bucket"),
      Map.entry("buildcraftenergy:fuel_mixed_heavy_heat_1_bucket", "buildcraftenergy:item/fluid_buckets/fuel_mixed_heavy_heat_1_bucket"),
      Map.entry("buildcraftenergy:fuel_mixed_heavy_heat_2_bucket", "buildcraftenergy:item/fluid_buckets/fuel_mixed_heavy_heat_2_bucket"),
      Map.entry("buildcraftenergy:fuel_mixed_light_bucket", "buildcraftenergy:item/fluid_buckets/fuel_mixed_light_bucket"),
      Map.entry("buildcraftenergy:fuel_mixed_light_heat_1_bucket", "buildcraftenergy:item/fluid_buckets/fuel_mixed_light_heat_1_bucket"),
      Map.entry("buildcraftenergy:fuel_mixed_light_heat_2_bucket", "buildcraftenergy:item/fluid_buckets/fuel_mixed_light_heat_2_bucket"),
      Map.entry("buildcraftenergy:glob_of_oil", "buildcraftenergy:item/glob_of_oil"),
      Map.entry("buildcraftenergy:mj_dynamo", "buildcraftenergy:item/mj_dynamo"),
      Map.entry("buildcraftenergy:oil_bucket", "buildcraftenergy:item/fluid_buckets/oil_bucket"),
      Map.entry("buildcraftenergy:oil_dense_bucket", "buildcraftenergy:item/fluid_buckets/oil_dense_bucket"),
      Map.entry("buildcraftenergy:oil_dense_heat_1_bucket", "buildcraftenergy:item/fluid_buckets/oil_dense_heat_1_bucket"),
      Map.entry("buildcraftenergy:oil_dense_heat_2_bucket", "buildcraftenergy:item/fluid_buckets/oil_dense_heat_2_bucket"),
      Map.entry("buildcraftenergy:oil_distilled_bucket", "buildcraftenergy:item/fluid_buckets/oil_distilled_bucket"),
      Map.entry("buildcraftenergy:oil_distilled_heat_1_bucket", "buildcraftenergy:item/fluid_buckets/oil_distilled_heat_1_bucket"),
      Map.entry("buildcraftenergy:oil_distilled_heat_2_bucket", "buildcraftenergy:item/fluid_buckets/oil_distilled_heat_2_bucket"),
      Map.entry("buildcraftenergy:oil_heat_1_bucket", "buildcraftenergy:item/fluid_buckets/oil_heat_1_bucket"),
      Map.entry("buildcraftenergy:oil_heat_2_bucket", "buildcraftenergy:item/fluid_buckets/oil_heat_2_bucket"),
      Map.entry("buildcraftenergy:oil_heavy_bucket", "buildcraftenergy:item/fluid_buckets/oil_heavy_bucket"),
      Map.entry("buildcraftenergy:oil_heavy_heat_1_bucket", "buildcraftenergy:item/fluid_buckets/oil_heavy_heat_1_bucket"),
      Map.entry("buildcraftenergy:oil_heavy_heat_2_bucket", "buildcraftenergy:item/fluid_buckets/oil_heavy_heat_2_bucket"),
      Map.entry("buildcraftenergy:oil_residue_bucket", "buildcraftenergy:item/fluid_buckets/oil_residue_bucket"),
      Map.entry("buildcraftenergy:oil_residue_heat_1_bucket", "buildcraftenergy:item/fluid_buckets/oil_residue_heat_1_bucket"),
      Map.entry("buildcraftenergy:oil_residue_heat_2_bucket", "buildcraftenergy:item/fluid_buckets/oil_residue_heat_2_bucket"),
      Map.entry("buildcraftfactory:autoworkbench_item", "buildcraftfactory:block/autoworkbench_item"),
      Map.entry("buildcraftfactory:chute", "buildcraftfactory:item/chute"),
      Map.entry("buildcraftfactory:distiller", "buildcraftfactory:item/distiller"),
      Map.entry("buildcraftfactory:flood_gate", "buildcraftfactory:item/flood_gate"),
      Map.entry("buildcraftfactory:gelled_water", "buildcraftfactory:item/gelled_water"),
      Map.entry("buildcraftfactory:heat_exchange", "buildcraftfactory:item/heat_exchange"),
      Map.entry("buildcraftfactory:mining_well", "buildcraftfactory:item/mining_well"),
      Map.entry("buildcraftfactory:pump", "buildcraftfactory:item/pump"),
      Map.entry("buildcraftfactory:tank", "buildcraftfactory:item/tank"),
      Map.entry("buildcraftfactory:water_gel_spawn", "buildcraftfactory:item/water_gel_spawn"),
      Map.entry("buildcraftlib:debugger", "buildcraftlib:item/debugger"),
      Map.entry("buildcraftlib:guide", "buildcraftlib:item/guide"),
      Map.entry("buildcraftlib:guide_config", "buildcraftlib:item/guide"),
      Map.entry("buildcraftlib:guide_note", "buildcraftlib:item/guide_note"),
      Map.entry("buildcraftrobotics:requester", "buildcraftrobotics:item/requester"),
      Map.entry("buildcraftrobotics:robot", "buildcraftrobotics:item/robot"),
      Map.entry("buildcraftrobotics:robot_station", "buildcraftrobotics:item/robot_station"),
      Map.entry("buildcraftrobotics:zone_planner", "buildcraftrobotics:item/zone_planner"),
      Map.entry("buildcraftsilicon:advanced_crafting_table", "buildcraftsilicon:item/advanced_crafting_table"),
      Map.entry("buildcraftsilicon:assembly_table", "buildcraftsilicon:item/assembly_table"),
      Map.entry("buildcraftsilicon:charging_table", "buildcraftsilicon:item/charging_table"),
      Map.entry("buildcraftsilicon:chipset_diamond", "buildcraftsilicon:item/chipset_diamond"),
      Map.entry("buildcraftsilicon:chipset_gold", "buildcraftsilicon:item/chipset_gold"),
      Map.entry("buildcraftsilicon:chipset_iron", "buildcraftsilicon:item/chipset_iron"),
      Map.entry("buildcraftsilicon:chipset_quartz", "buildcraftsilicon:item/chipset_quartz"),
      Map.entry("buildcraftsilicon:chipset_redstone", "buildcraftsilicon:item/chipset_redstone"),
      Map.entry("buildcraftsilicon:integration_table", "buildcraftsilicon:item/integration_table"),
      Map.entry("buildcraftsilicon:laser", "buildcraftsilicon:item/laser"),
      Map.entry("buildcraftsilicon:package", "buildcraftsilicon:item/package"),
      Map.entry("buildcraftsilicon:packager", "buildcraftsilicon:item/packager"),
      Map.entry("buildcraftsilicon:plug_facade", "buildcraftsilicon:item/plug_facade"),
      Map.entry("buildcraftsilicon:plug_gate", "buildcraftsilicon:item/plug_gate"),
      Map.entry("buildcraftsilicon:plug_lens", "buildcraftsilicon:item/plug_lens"),
      Map.entry("buildcraftsilicon:plug_light_sensor", "buildcraftsilicon:item/plug_light_sensor"),
      Map.entry("buildcraftsilicon:plug_pulsar", "buildcraftsilicon:item/plug_pulsar"),
      Map.entry("buildcraftsilicon:plug_timer", "buildcraftsilicon:item/plug_timer"),
      Map.entry("buildcraftsilicon:programming_table", "buildcraftsilicon:item/programming_table"),
      Map.entry("buildcraftsilicon:stamping_table", "buildcraftsilicon:item/stamping_table"),
      Map.entry("buildcrafttransport:filtered_buffer", "buildcrafttransport:item/filtered_buffer"),
      Map.entry("buildcrafttransport:pipe_clay_fluid", "buildcrafttransport:item/pipe_clay_fluid"),
      Map.entry("buildcrafttransport:pipe_clay_item", "buildcrafttransport:item/pipe_clay_item"),
      Map.entry("buildcrafttransport:pipe_cobble_fluid", "buildcrafttransport:item/pipe_cobble_fluid"),
      Map.entry("buildcrafttransport:pipe_cobble_item", "buildcrafttransport:item/pipe_cobble_item"),
      Map.entry("buildcrafttransport:pipe_cobble_power", "buildcrafttransport:item/pipe_cobble_power"),
      Map.entry("buildcrafttransport:pipe_cobble_rf", "buildcrafttransport:item/pipe_cobble_rf"),
      Map.entry("buildcrafttransport:pipe_daizuli_item", "buildcrafttransport:item/pipe_daizuli_item"),
      Map.entry("buildcrafttransport:pipe_diamond_fluid", "buildcrafttransport:item/pipe_diamond_fluid"),
      Map.entry("buildcrafttransport:pipe_diamond_item", "buildcrafttransport:item/pipe_diamond_item"),
      Map.entry("buildcrafttransport:pipe_diamond_power", "buildcrafttransport:item/pipe_diamond_power"),
      Map.entry("buildcrafttransport:pipe_diamond_rf", "buildcrafttransport:item/pipe_diamond_rf"),
      Map.entry("buildcrafttransport:pipe_diamond_wood_fluid", "buildcrafttransport:item/pipe_diamond_wood_fluid"),
      Map.entry("buildcrafttransport:pipe_diamond_wood_item", "buildcrafttransport:item/pipe_diamond_wood_item"),
      Map.entry("buildcrafttransport:pipe_diamond_wood_power", "buildcrafttransport:item/pipe_diamond_wood_power"),
      Map.entry("buildcrafttransport:pipe_diamond_wood_rf", "buildcrafttransport:item/pipe_diamond_wood_rf"),
      Map.entry("buildcrafttransport:pipe_emzuli_item", "buildcrafttransport:item/pipe_emzuli_item"),
      Map.entry("buildcrafttransport:pipe_gold_fluid", "buildcrafttransport:item/pipe_gold_fluid"),
      Map.entry("buildcrafttransport:pipe_gold_item", "buildcrafttransport:item/pipe_gold_item"),
      Map.entry("buildcrafttransport:pipe_gold_power", "buildcrafttransport:item/pipe_gold_power"),
      Map.entry("buildcrafttransport:pipe_gold_rf", "buildcrafttransport:item/pipe_gold_rf"),
      Map.entry("buildcrafttransport:pipe_iron_fluid", "buildcrafttransport:item/pipe_iron_fluid"),
      Map.entry("buildcrafttransport:pipe_iron_item", "buildcrafttransport:item/pipe_iron_item"),
      Map.entry("buildcrafttransport:pipe_iron_power", "buildcrafttransport:item/pipe_iron_power"),
      Map.entry("buildcrafttransport:pipe_iron_rf", "buildcrafttransport:item/pipe_iron_rf"),
      Map.entry("buildcrafttransport:pipe_lapis_item", "buildcrafttransport:item/pipe_lapis_item"),
      Map.entry("buildcrafttransport:pipe_obsidian_item", "buildcrafttransport:item/pipe_obsidian_item"),
      Map.entry("buildcrafttransport:pipe_quartz_fluid", "buildcrafttransport:item/pipe_quartz_fluid"),
      Map.entry("buildcrafttransport:pipe_quartz_item", "buildcrafttransport:item/pipe_quartz_item"),
      Map.entry("buildcrafttransport:pipe_quartz_power", "buildcrafttransport:item/pipe_quartz_power"),
      Map.entry("buildcrafttransport:pipe_quartz_rf", "buildcrafttransport:item/pipe_quartz_rf"),
      Map.entry("buildcrafttransport:pipe_sandstone_fluid", "buildcrafttransport:item/pipe_sandstone_fluid"),
      Map.entry("buildcrafttransport:pipe_sandstone_item", "buildcrafttransport:item/pipe_sandstone_item"),
      Map.entry("buildcrafttransport:pipe_sandstone_power", "buildcrafttransport:item/pipe_sandstone_power"),
      Map.entry("buildcrafttransport:pipe_sandstone_rf", "buildcrafttransport:item/pipe_sandstone_rf"),
      Map.entry("buildcrafttransport:pipe_stone_fluid", "buildcrafttransport:item/pipe_stone_fluid"),
      Map.entry("buildcrafttransport:pipe_stone_item", "buildcrafttransport:item/pipe_stone_item"),
      Map.entry("buildcrafttransport:pipe_stone_power", "buildcrafttransport:item/pipe_stone_power"),
      Map.entry("buildcrafttransport:pipe_stone_rf", "buildcrafttransport:item/pipe_stone_rf"),
      Map.entry("buildcrafttransport:pipe_stripes_item", "buildcrafttransport:item/pipe_stripes_item"),
      Map.entry("buildcrafttransport:pipe_structure", "buildcrafttransport:item/pipe_structure"),
      Map.entry("buildcrafttransport:pipe_void_fluid", "buildcrafttransport:item/pipe_void_fluid"),
      Map.entry("buildcrafttransport:pipe_void_item", "buildcrafttransport:item/pipe_void_item"),
      Map.entry("buildcrafttransport:pipe_wood_fluid", "buildcrafttransport:item/pipe_wood_fluid"),
      Map.entry("buildcrafttransport:pipe_wood_item", "buildcrafttransport:item/pipe_wood_item"),
      Map.entry("buildcrafttransport:pipe_wood_power", "buildcrafttransport:item/pipe_wood_power"),
      Map.entry("buildcrafttransport:pipe_wood_rf", "buildcrafttransport:item/pipe_wood_rf"),
      Map.entry("buildcrafttransport:plug_blocker", "buildcrafttransport:item/plug_blocker"),
      Map.entry("buildcrafttransport:plug_power_adaptor", "buildcrafttransport:item/plug_power_adaptor"),
      Map.entry("buildcrafttransport:waterproof", "buildcrafttransport:item/waterproof"),
      Map.entry("buildcrafttransport:wire_black", "buildcrafttransport:item/wire_black"),
      Map.entry("buildcrafttransport:wire_blue", "buildcrafttransport:item/wire_blue"),
      Map.entry("buildcrafttransport:wire_brown", "buildcrafttransport:item/wire_brown"),
      Map.entry("buildcrafttransport:wire_cyan", "buildcrafttransport:item/wire_cyan"),
      Map.entry("buildcrafttransport:wire_gray", "buildcrafttransport:item/wire_gray"),
      Map.entry("buildcrafttransport:wire_green", "buildcrafttransport:item/wire_green"),
      Map.entry("buildcrafttransport:wire_light_blue", "buildcrafttransport:item/wire_light_blue"),
      Map.entry("buildcrafttransport:wire_light_gray", "buildcrafttransport:item/wire_light_gray"),
      Map.entry("buildcrafttransport:wire_lime", "buildcrafttransport:item/wire_lime"),
      Map.entry("buildcrafttransport:wire_magenta", "buildcrafttransport:item/wire_magenta"),
      Map.entry("buildcrafttransport:wire_orange", "buildcrafttransport:item/wire_orange"),
      Map.entry("buildcrafttransport:wire_pink", "buildcrafttransport:item/wire_pink"),
      Map.entry("buildcrafttransport:wire_purple", "buildcrafttransport:item/wire_purple"),
      Map.entry("buildcrafttransport:wire_red", "buildcrafttransport:item/wire_red"),
      Map.entry("buildcrafttransport:wire_white", "buildcrafttransport:item/wire_white"),
      Map.entry("buildcrafttransport:wire_yellow", "buildcrafttransport:item/wire_yellow")
   );

   static final List<Dispatch> ITEM_DISPATCH = List.of(
      new Dispatch("buildcraftcore:list", "buildcraftcore:item/list_clean", mapOf(1, "buildcraftcore:item/list_used")),
      new Dispatch("buildcraftcore:map_location", "buildcraftcore:item/map_location/clean", mapOf(1, "buildcraftcore:item/map_location/spot", 2, "buildcraftcore:item/map_location/area", 3, "buildcraftcore:item/map_location/path", 4, "buildcraftcore:item/map_location/zone", 5, "buildcraftcore:item/map_location/path_repeating")),
      new Dispatch("buildcraftcore:paintbrush", "buildcraftcore:item/paintbrush", mapOf(1, "buildcraftcore:item/paintbrush_white", 2, "buildcraftcore:item/paintbrush_orange", 3, "buildcraftcore:item/paintbrush_magenta", 4, "buildcraftcore:item/paintbrush_light_blue", 5, "buildcraftcore:item/paintbrush_yellow", 6, "buildcraftcore:item/paintbrush_lime", 7, "buildcraftcore:item/paintbrush_pink", 8, "buildcraftcore:item/paintbrush_gray", 9, "buildcraftcore:item/paintbrush_light_gray", 10, "buildcraftcore:item/paintbrush_cyan", 11, "buildcraftcore:item/paintbrush_purple", 12, "buildcraftcore:item/paintbrush_blue", 13, "buildcraftcore:item/paintbrush_brown", 14, "buildcraftcore:item/paintbrush_green", 15, "buildcraftcore:item/paintbrush_red", 16, "buildcraftcore:item/paintbrush_black")),
      new Dispatch("buildcraftrobotics:redstone_board", "buildcraftrobotics:item/board/clean", mapOf(1, "buildcraftrobotics:item/board/green", 2, "buildcraftrobotics:item/board/blue", 3, "buildcraftrobotics:item/board/red", 4, "buildcraftrobotics:item/board/yellow")),
      new Dispatch("buildcraftsilicon:gate_copier", "buildcraftsilicon:item/gate_copier", mapOf(1, "buildcraftsilicon:item/gate_copier_full"))
   );

   static final List<FlatModel> FLAT_ITEM_MODELS = List.of(
      new FlatModel("buildcraftbuilders:blueprint_clean", "minecraft:item/generated", "buildcraftbuilders:item/blueprint_clean"),
      new FlatModel("buildcraftbuilders:blueprint_used", "minecraft:item/generated", "buildcraftbuilders:item/blueprint_used"),
      new FlatModel("buildcraftbuilders:schematic_single_clean", "minecraft:item/generated", "buildcraftbuilders:item/schematic_single_clean"),
      new FlatModel("buildcraftbuilders:schematic_single_used", "minecraft:item/generated", "buildcraftbuilders:item/schematic_single_used"),
      new FlatModel("buildcraftbuilders:template_clean", "minecraft:item/generated", "buildcraftbuilders:item/template_clean"),
      new FlatModel("buildcraftbuilders:template_used", "minecraft:item/generated", "buildcraftbuilders:item/template_used"),
      new FlatModel("buildcraftcore:diamond_shard", "minecraft:item/generated", "buildcraftcore:item/diamond_shard"),
      new FlatModel("buildcraftcore:gear_diamond", "minecraft:item/generated", "buildcraftcore:item/gear_diamond"),
      new FlatModel("buildcraftcore:gear_gold", "minecraft:item/generated", "buildcraftcore:item/gear_gold"),
      new FlatModel("buildcraftcore:gear_iron", "minecraft:item/generated", "buildcraftcore:item/gear_iron"),
      new FlatModel("buildcraftcore:gear_stone", "minecraft:item/generated", "buildcraftcore:item/gear_stone"),
      new FlatModel("buildcraftcore:gear_wood", "minecraft:item/generated", "buildcraftcore:item/gear_wood"),
      new FlatModel("buildcraftcore:list_clean", "minecraft:item/generated", "buildcraftcore:item/list/clean"),
      new FlatModel("buildcraftcore:list_used", "minecraft:item/generated", "buildcraftcore:item/list/used"),
      new FlatModel("buildcraftcore:marker_connector", "minecraft:item/generated", "buildcraftcore:item/marker_connector"),
      new FlatModel("buildcraftcore:marker_path", "minecraft:item/generated", "buildcraftcore:item/marker_path"),
      new FlatModel("buildcraftcore:marker_volume", "minecraft:item/generated", "buildcraftcore:item/marker_volume"),
      new FlatModel("buildcraftcore:paintbrush", "minecraft:item/handheld", "buildcraftcore:item/paintbrush/clean"),
      new FlatModel("buildcraftcore:paintbrush_black", "minecraft:item/handheld", "buildcraftcore:item/paintbrush/black"),
      new FlatModel("buildcraftcore:paintbrush_blue", "minecraft:item/handheld", "buildcraftcore:item/paintbrush/blue"),
      new FlatModel("buildcraftcore:paintbrush_brown", "minecraft:item/handheld", "buildcraftcore:item/paintbrush/brown"),
      new FlatModel("buildcraftcore:paintbrush_cyan", "minecraft:item/handheld", "buildcraftcore:item/paintbrush/cyan"),
      new FlatModel("buildcraftcore:paintbrush_gray", "minecraft:item/handheld", "buildcraftcore:item/paintbrush/gray"),
      new FlatModel("buildcraftcore:paintbrush_green", "minecraft:item/handheld", "buildcraftcore:item/paintbrush/green"),
      new FlatModel("buildcraftcore:paintbrush_light_blue", "minecraft:item/handheld", "buildcraftcore:item/paintbrush/light_blue"),
      new FlatModel("buildcraftcore:paintbrush_light_gray", "minecraft:item/handheld", "buildcraftcore:item/paintbrush/light_gray"),
      new FlatModel("buildcraftcore:paintbrush_lime", "minecraft:item/handheld", "buildcraftcore:item/paintbrush/lime"),
      new FlatModel("buildcraftcore:paintbrush_magenta", "minecraft:item/handheld", "buildcraftcore:item/paintbrush/magenta"),
      new FlatModel("buildcraftcore:paintbrush_orange", "minecraft:item/handheld", "buildcraftcore:item/paintbrush/orange"),
      new FlatModel("buildcraftcore:paintbrush_pink", "minecraft:item/handheld", "buildcraftcore:item/paintbrush/pink"),
      new FlatModel("buildcraftcore:paintbrush_purple", "minecraft:item/handheld", "buildcraftcore:item/paintbrush/purple"),
      new FlatModel("buildcraftcore:paintbrush_red", "minecraft:item/handheld", "buildcraftcore:item/paintbrush/red"),
      new FlatModel("buildcraftcore:paintbrush_white", "minecraft:item/handheld", "buildcraftcore:item/paintbrush/white"),
      new FlatModel("buildcraftcore:paintbrush_yellow", "minecraft:item/handheld", "buildcraftcore:item/paintbrush/yellow"),
      new FlatModel("buildcraftcore:volume_box", "minecraft:item/generated", "buildcraftcore:lasers/marker_volume_connected"),
      new FlatModel("buildcraftcore:wrench", "minecraft:item/handheld", "buildcraftcore:item/wrench"),
      new FlatModel("buildcraftfactory:gelled_water", "minecraft:item/generated", "buildcraftfactory:item/gelled_water"),
      new FlatModel("buildcraftfactory:water_gel_spawn", "minecraft:item/generated", "buildcraftfactory:item/water_gel_spawn"),
      new FlatModel("buildcraftrobotics:robot", "minecraft:item/generated", "buildcraftrobotics:item/board/clean"),
      new FlatModel("buildcraftrobotics:robot_station", "minecraft:item/generated", "buildcraftrobotics:block/requester/top"),
      new FlatModel("buildcraftsilicon:plug_gate", "minecraft:item/generated", "minecraft:item/barrier")
   );

   static final Map<String, String> BLOCKSTATES_SINGLE = Map.ofEntries(
      Map.entry("buildcraftcore:decorated_blueprint", "buildcraftcore:block/decorated/blueprint"),
      Map.entry("buildcraftcore:decorated_destroy", "buildcraftcore:block/decorated/destroy"),
      Map.entry("buildcraftcore:decorated_laser", "buildcraftcore:block/decorated/laser_back"),
      Map.entry("buildcraftcore:decorated_template", "buildcraftcore:block/decorated/template"),
      Map.entry("buildcraftcore:power_tester", "buildcraftcore:block/power_tester"),
      Map.entry("buildcraftcore:spring_oil", "buildcraftcore:block/spring_oil"),
      Map.entry("buildcraftcore:spring_water", "buildcraftcore:block/spring_water"),
      Map.entry("buildcraftfactory:autoworkbench_item", "buildcraftfactory:block/autoworkbench_item"),
      Map.entry("buildcraftfactory:pump", "buildcraftfactory:block/pump"),
      Map.entry("buildcraftsilicon:advanced_crafting_table", "buildcraftsilicon:block/table/advanced_crafting"),
      Map.entry("buildcraftsilicon:assembly_table", "buildcraftsilicon:block/table/assembly"),
      Map.entry("buildcraftsilicon:charging_table", "buildcraftsilicon:block/table/charging"),
      Map.entry("buildcraftsilicon:integration_table", "buildcraftsilicon:block/table/integration"),
      Map.entry("buildcraftsilicon:packager", "buildcraftsilicon:block/packager"),
      Map.entry("buildcraftsilicon:programming_table", "buildcraftsilicon:block/table/programming"),
      Map.entry("buildcraftsilicon:stamping_table", "buildcraftsilicon:block/table/stamping"),
      Map.entry("buildcrafttransport:filtered_buffer", "buildcrafttransport:block/filtered_buffer"),
      Map.entry("buildcrafttransport:pipe_holder", "buildcrafttransport:block/pipe_holder")
   );

   static final Map<String, String> BLOCKSTATES_FACING = Map.ofEntries(
      Map.entry("buildcraftbuilders:architect", "buildcraftbuilders:block/architect"),
      Map.entry("buildcraftbuilders:construction_marker", "buildcraftbuilders:block/construction_marker"),
      Map.entry("buildcraftbuilders:filler", "buildcraftbuilders:block/filler/main"),
      Map.entry("buildcraftbuilders:library", "buildcraftbuilders:block/library/library"),
      Map.entry("buildcraftbuilders:quarry", "buildcraftbuilders:block/quarry"),
      Map.entry("buildcraftbuilders:replacer", "buildcraftbuilders:block/replacer/main"),
      Map.entry("buildcraftfactory:mining_well", "buildcraftfactory:block/mining_well")
   );

   static final List<BlockModel> BLOCK_MODELS = List.of(
      new BlockModel("buildcraftbuilders:construction_marker", "minecraft:block/cube_all", Map.of("all", "minecraft:block/lapis_block")),
      new BlockModel("buildcraftcore:power_tester", "minecraft:block/cube_all", Map.of("all", "buildcraftcore:block/power_tester")),
      new BlockModel("buildcraftfactory:autoworkbench_item", "minecraft:block/cube_bottom_top", Map.of("bottom", "buildcraftfactory:block/auto_workbench_item/top", "particle", "buildcraftfactory:block/auto_workbench_item/side", "side", "buildcraftfactory:block/auto_workbench_item/side", "top", "buildcraftfactory:block/auto_workbench_item/top"))
   );

   static final List<Tag> TAGS_BLOCK = List.of(
      new Tag("buildcraftenergy:oil_fluids", List.of("buildcraftenergy:oil", "buildcraftenergy:oil_heat_1", "buildcraftenergy:oil_heat_2", "buildcraftenergy:oil_residue", "buildcraftenergy:oil_residue_heat_1", "buildcraftenergy:oil_residue_heat_2", "buildcraftenergy:oil_heavy", "buildcraftenergy:oil_heavy_heat_1", "buildcraftenergy:oil_heavy_heat_2", "buildcraftenergy:oil_dense", "buildcraftenergy:oil_dense_heat_1", "buildcraftenergy:oil_dense_heat_2", "buildcraftenergy:oil_distilled", "buildcraftenergy:oil_distilled_heat_1", "buildcraftenergy:oil_distilled_heat_2", "buildcraftenergy:fuel_dense", "buildcraftenergy:fuel_dense_heat_1", "buildcraftenergy:fuel_dense_heat_2", "buildcraftenergy:fuel_mixed_heavy", "buildcraftenergy:fuel_mixed_heavy_heat_1", "buildcraftenergy:fuel_mixed_heavy_heat_2", "buildcraftenergy:fuel_light", "buildcraftenergy:fuel_light_heat_1", "buildcraftenergy:fuel_light_heat_2", "buildcraftenergy:fuel_mixed_light", "buildcraftenergy:fuel_mixed_light_heat_1", "buildcraftenergy:fuel_mixed_light_heat_2", "buildcraftenergy:fuel_gaseous", "buildcraftenergy:fuel_gaseous_heat_1", "buildcraftenergy:fuel_gaseous_heat_2")),
      new Tag("minecraft:mineable/pickaxe", List.of("buildcraftcore:decorated_laser", "buildcraftcore:decorated_destroy", "buildcraftcore:decorated_blueprint", "buildcraftcore:decorated_template", "buildcraftcore:engine_redstone", "buildcraftcore:engine_creative", "buildcraftenergy:engine_stone", "buildcraftenergy:engine_iron", "?buildcraftenergy:engine_rf", "?buildcraftenergy:mj_dynamo", "buildcraftfactory:autoworkbench_item", "buildcraftfactory:mining_well", "buildcraftfactory:pump", "buildcraftfactory:flood_gate", "buildcraftfactory:tank", "buildcraftfactory:chute", "buildcraftfactory:distiller", "buildcraftfactory:heat_exchange", "buildcraftbuilders:frame", "buildcraftbuilders:filler", "buildcraftbuilders:builder", "buildcraftbuilders:architect", "buildcraftbuilders:library", "buildcraftbuilders:replacer", "buildcraftbuilders:quarry", "buildcraftsilicon:laser", "buildcraftsilicon:assembly_table", "buildcraftsilicon:advanced_crafting_table", "?buildcraftsilicon:integration_table", "?buildcraftsilicon:charging_table", "?buildcraftsilicon:programming_table", "?buildcraftsilicon:stamping_table", "?buildcraftsilicon:packager", "buildcrafttransport:filtered_buffer", "buildcrafttransport:pipe_holder", "?buildcraftrobotics:zone_planner", "?buildcraftrobotics:requester")),
      new Tag("minecraft:snow_layer_cannot_survive_on", List.of("buildcraftcore:engine_redstone", "buildcraftcore:engine_creative", "buildcraftenergy:engine_stone", "buildcraftenergy:engine_iron", "buildcraftfactory:heat_exchange"))
   );

   static final List<Tag> TAGS_FLUID = List.of(
      new Tag("buildcraftenergy:bc_fluids", List.of("buildcraftenergy:oil", "buildcraftenergy:oil_flowing", "buildcraftenergy:oil_heat_1", "buildcraftenergy:oil_heat_1_flowing", "buildcraftenergy:oil_heat_2", "buildcraftenergy:oil_heat_2_flowing", "buildcraftenergy:oil_residue", "buildcraftenergy:oil_residue_flowing", "buildcraftenergy:oil_residue_heat_1", "buildcraftenergy:oil_residue_heat_1_flowing", "buildcraftenergy:oil_residue_heat_2", "buildcraftenergy:oil_residue_heat_2_flowing", "buildcraftenergy:oil_heavy", "buildcraftenergy:oil_heavy_flowing", "buildcraftenergy:oil_heavy_heat_1", "buildcraftenergy:oil_heavy_heat_1_flowing", "buildcraftenergy:oil_heavy_heat_2", "buildcraftenergy:oil_heavy_heat_2_flowing", "buildcraftenergy:oil_dense", "buildcraftenergy:oil_dense_flowing", "buildcraftenergy:oil_dense_heat_1", "buildcraftenergy:oil_dense_heat_1_flowing", "buildcraftenergy:oil_dense_heat_2", "buildcraftenergy:oil_dense_heat_2_flowing", "buildcraftenergy:oil_distilled", "buildcraftenergy:oil_distilled_flowing", "buildcraftenergy:oil_distilled_heat_1", "buildcraftenergy:oil_distilled_heat_1_flowing", "buildcraftenergy:oil_distilled_heat_2", "buildcraftenergy:oil_distilled_heat_2_flowing", "buildcraftenergy:fuel_dense", "buildcraftenergy:fuel_dense_flowing", "buildcraftenergy:fuel_dense_heat_1", "buildcraftenergy:fuel_dense_heat_1_flowing", "buildcraftenergy:fuel_dense_heat_2", "buildcraftenergy:fuel_dense_heat_2_flowing", "buildcraftenergy:fuel_mixed_heavy", "buildcraftenergy:fuel_mixed_heavy_flowing", "buildcraftenergy:fuel_mixed_heavy_heat_1", "buildcraftenergy:fuel_mixed_heavy_heat_1_flowing", "buildcraftenergy:fuel_mixed_heavy_heat_2", "buildcraftenergy:fuel_mixed_heavy_heat_2_flowing", "buildcraftenergy:fuel_light", "buildcraftenergy:fuel_light_flowing", "buildcraftenergy:fuel_light_heat_1", "buildcraftenergy:fuel_light_heat_1_flowing", "buildcraftenergy:fuel_light_heat_2", "buildcraftenergy:fuel_light_heat_2_flowing", "buildcraftenergy:fuel_mixed_light", "buildcraftenergy:fuel_mixed_light_flowing", "buildcraftenergy:fuel_mixed_light_heat_1", "buildcraftenergy:fuel_mixed_light_heat_1_flowing", "buildcraftenergy:fuel_mixed_light_heat_2", "buildcraftenergy:fuel_mixed_light_heat_2_flowing", "buildcraftenergy:fuel_gaseous", "buildcraftenergy:fuel_gaseous_flowing", "buildcraftenergy:fuel_gaseous_heat_1", "buildcraftenergy:fuel_gaseous_heat_1_flowing", "buildcraftenergy:fuel_gaseous_heat_2", "buildcraftenergy:fuel_gaseous_heat_2_flowing")),
      new Tag("buildcraftenergy:bc_liquids", List.of("buildcraftenergy:oil", "buildcraftenergy:oil_flowing", "buildcraftenergy:oil_heat_1", "buildcraftenergy:oil_heat_1_flowing", "buildcraftenergy:oil_heat_2", "buildcraftenergy:oil_heat_2_flowing", "buildcraftenergy:oil_residue", "buildcraftenergy:oil_residue_flowing", "buildcraftenergy:oil_residue_heat_1", "buildcraftenergy:oil_residue_heat_1_flowing", "buildcraftenergy:oil_residue_heat_2", "buildcraftenergy:oil_residue_heat_2_flowing", "buildcraftenergy:oil_heavy", "buildcraftenergy:oil_heavy_flowing", "buildcraftenergy:oil_heavy_heat_1", "buildcraftenergy:oil_heavy_heat_1_flowing", "buildcraftenergy:oil_heavy_heat_2", "buildcraftenergy:oil_heavy_heat_2_flowing", "buildcraftenergy:oil_dense", "buildcraftenergy:oil_dense_flowing", "buildcraftenergy:oil_dense_heat_1", "buildcraftenergy:oil_dense_heat_1_flowing", "buildcraftenergy:oil_dense_heat_2", "buildcraftenergy:oil_dense_heat_2_flowing", "buildcraftenergy:oil_distilled", "buildcraftenergy:oil_distilled_flowing", "buildcraftenergy:oil_distilled_heat_1", "buildcraftenergy:oil_distilled_heat_1_flowing", "buildcraftenergy:fuel_dense", "buildcraftenergy:fuel_dense_flowing", "buildcraftenergy:fuel_dense_heat_1", "buildcraftenergy:fuel_dense_heat_1_flowing", "buildcraftenergy:fuel_mixed_heavy", "buildcraftenergy:fuel_mixed_heavy_flowing", "buildcraftenergy:fuel_mixed_heavy_heat_1", "buildcraftenergy:fuel_mixed_heavy_heat_1_flowing", "buildcraftenergy:fuel_light", "buildcraftenergy:fuel_light_flowing", "buildcraftenergy:fuel_mixed_light", "buildcraftenergy:fuel_mixed_light_flowing"))
   );

   static final List<Tag> TAGS_ITEM = List.of(
      new Tag("c:gears", List.of("#c:gears/wooden", "#c:gears/stone", "#c:gears/iron", "#c:gears/gold", "#c:gears/diamond")),
      new Tag("c:gears/diamond", List.of("buildcraftcore:gear_diamond")),
      new Tag("c:gears/gold", List.of("buildcraftcore:gear_gold")),
      new Tag("c:gears/iron", List.of("buildcraftcore:gear_iron")),
      new Tag("c:gears/stone", List.of("buildcraftcore:gear_stone")),
      new Tag("c:gears/wooden", List.of("buildcraftcore:gear_wood")),
      new Tag("c:hidden_from_recipe_viewers", List.of("buildcraftsilicon:plug_facade")),
      new Tag("c:tools/wrench", List.of("buildcraftcore:wrench")),
      new Tag("c:wrenches", List.of("buildcraftcore:wrench"))
   );

   private static Map<Integer, String> mapOf(Object... pairs) {
      Map<Integer, String> map = new java.util.LinkedHashMap<>();
      for (int n = 0; n < pairs.length; n += 2) {
         map.put((Integer) pairs[n], (String) pairs[n + 1]);
      }
      return map;
   }

   private BCAssetDefs() {
   }
}
