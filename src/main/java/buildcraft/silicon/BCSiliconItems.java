package buildcraft.silicon;

import buildcraft.fabric.BCRegistries;
import buildcraft.silicon.item.ItemGateCopier;
import buildcraft.silicon.item.ItemPackage;
import buildcraft.silicon.item.ItemPluggableFacade;
import buildcraft.silicon.item.ItemPluggableGate;
import buildcraft.silicon.item.ItemPluggableLens;
import buildcraft.silicon.item.ItemPluggableLightSensor;
import buildcraft.silicon.item.ItemPluggablePulsar;
import buildcraft.silicon.item.ItemPluggableTimer;
import java.util.Objects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class BCSiliconItems {
   public static BlockItem LASER;
   public static BlockItem ASSEMBLY_TABLE;
   public static BlockItem ADVANCED_CRAFTING_TABLE;
   public static BlockItem INTEGRATION_TABLE;
   public static BlockItem CHARGING_TABLE;
   public static BlockItem PROGRAMMING_TABLE;
   public static BlockItem STAMPING_TABLE;
   public static BlockItem PACKAGER;
   public static ItemPackage PACKAGE;
   public static Item CHIPSET_REDSTONE;
   public static Item CHIPSET_IRON;
   public static Item CHIPSET_GOLD;
   public static Item CHIPSET_QUARTZ;
   public static Item CHIPSET_DIAMOND;
   public static ItemGateCopier GATE_COPIER;
   public static ItemPluggableFacade PLUG_FACADE;
   public static ItemPluggableGate PLUG_GATE;
   public static ItemPluggablePulsar PLUG_PULSAR;
   public static ItemPluggableLens PLUG_LENS;
   public static ItemPluggableLightSensor PLUG_LIGHT_SENSOR;
   public static ItemPluggableTimer PLUG_TIMER;

   private BCSiliconItems() {
   }

   public static void register() {
      Objects.requireNonNull(BCSiliconPlugs.lightSensor, "BCSiliconPlugs.preInit() must run before BCSiliconItems.register()");
      Objects.requireNonNull(BCSiliconPlugs.timer, "BCSiliconPlugs.preInit() must run before BCSiliconItems.register()");
      LASER = BCRegistries.registerBlockItem("buildcraftsilicon", "laser", BCSiliconBlocks.LASER);
      ASSEMBLY_TABLE = BCRegistries.registerBlockItem("buildcraftsilicon", "assembly_table", BCSiliconBlocks.ASSEMBLY_TABLE);
      ADVANCED_CRAFTING_TABLE = BCRegistries.registerBlockItem("buildcraftsilicon", "advanced_crafting_table", BCSiliconBlocks.ADVANCED_CRAFTING_TABLE);
      INTEGRATION_TABLE = BCRegistries.registerBlockItem("buildcraftsilicon", "integration_table", BCSiliconBlocks.INTEGRATION_TABLE);
      CHARGING_TABLE = BCRegistries.registerBlockItem("buildcraftsilicon", "charging_table", BCSiliconBlocks.CHARGING_TABLE);
      PROGRAMMING_TABLE = BCRegistries.registerBlockItem("buildcraftsilicon", "programming_table", BCSiliconBlocks.PROGRAMMING_TABLE);
      STAMPING_TABLE = BCRegistries.registerBlockItem("buildcraftsilicon", "stamping_table", BCSiliconBlocks.STAMPING_TABLE);
      PACKAGER = BCRegistries.registerBlockItem("buildcraftsilicon", "packager", BCSiliconBlocks.PACKAGER);
      PACKAGE = BCRegistries.registerItem("buildcraftsilicon", "package", props -> new ItemPackage(props.stacksTo(1)));
      CHIPSET_REDSTONE = BCRegistries.registerItem("buildcraftsilicon", "chipset_redstone", Item::new);
      CHIPSET_IRON = BCRegistries.registerItem("buildcraftsilicon", "chipset_iron", Item::new);
      CHIPSET_GOLD = BCRegistries.registerItem("buildcraftsilicon", "chipset_gold", Item::new);
      CHIPSET_QUARTZ = BCRegistries.registerItem("buildcraftsilicon", "chipset_quartz", Item::new);
      CHIPSET_DIAMOND = BCRegistries.registerItem("buildcraftsilicon", "chipset_diamond", Item::new);
      GATE_COPIER = BCRegistries.registerItem("buildcraftsilicon", "gate_copier", ItemGateCopier::new);
      PLUG_FACADE = BCRegistries.registerItem("buildcraftsilicon", "plug_facade", ItemPluggableFacade::new);
      PLUG_GATE = BCRegistries.registerItem("buildcraftsilicon", "plug_gate", ItemPluggableGate::new);
      PLUG_PULSAR = BCRegistries.registerItem("buildcraftsilicon", "plug_pulsar", ItemPluggablePulsar::new);
      PLUG_LENS = BCRegistries.registerItem("buildcraftsilicon", "plug_lens", ItemPluggableLens::new);
      PLUG_LIGHT_SENSOR = BCRegistries.registerItem("buildcraftsilicon", "plug_light_sensor", ItemPluggableLightSensor::new);
      PLUG_TIMER = BCRegistries.registerItem("buildcraftsilicon", "plug_timer", ItemPluggableTimer::new);
   }
}
