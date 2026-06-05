package buildcraft.silicon;

import buildcraft.fabric.registry.DeferredRegister;
import buildcraft.fabric.registry.DeferredItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import buildcraft.silicon.item.ItemGateCopier;
import buildcraft.silicon.item.ItemPluggableFacade;
import buildcraft.silicon.item.ItemPluggableGate;
import buildcraft.silicon.item.ItemPluggableLens;
import buildcraft.silicon.item.ItemPluggablePulsar;

public class BCSiliconItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BCSilicon.MODID);

    public static final DeferredItem<BlockItem> LASER =
            ITEMS.registerSimpleBlockItem(BCSiliconBlocks.LASER);

    public static final DeferredItem<BlockItem> ASSEMBLY_TABLE =
            ITEMS.registerSimpleBlockItem(BCSiliconBlocks.ASSEMBLY_TABLE);

    public static final DeferredItem<BlockItem> ADVANCED_CRAFTING_TABLE =
            ITEMS.registerSimpleBlockItem(BCSiliconBlocks.ADVANCED_CRAFTING_TABLE);

    public static final DeferredItem<BlockItem> INTEGRATION_TABLE =
            ITEMS.registerSimpleBlockItem(BCSiliconBlocks.INTEGRATION_TABLE);

    public static final DeferredItem<Item> CHIPSET_REDSTONE =
            ITEMS.registerSimpleItem("chipset_redstone");

    public static final DeferredItem<Item> CHIPSET_IRON =
            ITEMS.registerSimpleItem("chipset_iron");

    public static final DeferredItem<Item> CHIPSET_GOLD =
            ITEMS.registerSimpleItem("chipset_gold");

    public static final DeferredItem<Item> CHIPSET_QUARTZ =
            ITEMS.registerSimpleItem("chipset_quartz");

    public static final DeferredItem<Item> CHIPSET_DIAMOND =
            ITEMS.registerSimpleItem("chipset_diamond");

    public static final DeferredItem<ItemGateCopier> GATE_COPIER =
            ITEMS.registerItem("gate_copier", ItemGateCopier::new);

    public static final DeferredItem<ItemPluggableFacade> PLUG_FACADE =
            ITEMS.registerItem("plug_facade", ItemPluggableFacade::new);

    public static final DeferredItem<ItemPluggableGate> PLUG_GATE =
            ITEMS.registerItem("plug_gate", ItemPluggableGate::new);

    public static final DeferredItem<ItemPluggablePulsar> PLUG_PULSAR =
            ITEMS.registerItem("plug_pulsar", ItemPluggablePulsar::new);

    public static final DeferredItem<ItemPluggableLens> PLUG_LENS =
            ITEMS.registerItem("plug_lens", ItemPluggableLens::new);

    public static final DeferredItem<Item> PLUG_LIGHT_SENSOR =
            ITEMS.registerItem("plug_light_sensor", props -> new buildcraft.lib.item.ItemPluggableSimple(props, buildcraft.silicon.BCSiliconPlugs.lightSensor, null));

    public static final DeferredItem<Item> PLUG_TIMER =
            ITEMS.registerItem("plug_timer", props -> new buildcraft.lib.item.ItemPluggableSimple(props, buildcraft.silicon.BCSiliconPlugs.timer, null));

    public static void register() {
        ITEMS.register();
    }
}

