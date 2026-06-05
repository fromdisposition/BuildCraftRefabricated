package buildcraft.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import buildcraft.api.facades.FacadeAPI;
import buildcraft.api.mj.MjAPI;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.plug.FacadeStateManager;
import buildcraft.lib.attachments.Attachments;
import buildcraft.lib.attachments.RegisterAttachmentsEvent;
import buildcraft.lib.mj.MjBatteryEnergyHandler;
import buildcraft.silicon.BCSiliconBlockEntities;
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.BCSiliconCreativeTabs;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.BCSiliconPlugs;
import buildcraft.silicon.BCSiliconIntegrationRecipes;
import buildcraft.silicon.BCSiliconRecipes;
import buildcraft.silicon.BCSiliconStatements;
import buildcraft.silicon.plug.FacadeStateManager;

public final class BCSiliconFabric {
    private BCSiliconFabric() {}

    public static void register() {
        BCSiliconBlocks.register();
        BCSiliconItems.register();
        BCSiliconBlockEntities.register();
        BCSiliconMenuTypes.register();
        BCSiliconCreativeTabs.register();

        BCSiliconPlugs.preInit();
        BCSiliconStatements.preInit();
        registerCapabilities();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            BCSiliconPlugs.registerAll();
            FacadeAPI.facadeItem = BCSiliconItems.PLUG_FACADE.get();
            FacadeAPI.registry = FacadeStateManager.INSTANCE;
            FacadeStateManager.ensureInitialized();
            BCSiliconRecipes.init();
            BCSiliconIntegrationRecipes.init();
        });
    }

    private static void registerCapabilities() {
        RegisterAttachmentsEvent event = new RegisterAttachmentsEvent();

        event.registerBlockEntity(MjAPI.CAP_RECEIVER, BCSiliconBlockEntities.LASER,
                (laser, direction) -> laser.getMjReceiver());
        event.registerBlockEntity(MjAPI.CAP_CONNECTOR, BCSiliconBlockEntities.LASER,
                (laser, direction) -> laser.getMjReceiver());
        event.registerBlockEntity(Attachments.Energy.BLOCK, BCSiliconBlockEntities.LASER,
                (laser, direction) -> MjBatteryEnergyHandler.createIfRfEnabled(laser.getBattery()));

        event.registerBlockEntity(Attachments.Item.BLOCK, BCSiliconBlockEntities.ASSEMBLY_TABLE,
                (table, direction) -> table.getItemHandler(direction));
        event.registerBlockEntity(Attachments.Item.BLOCK, BCSiliconBlockEntities.ADVANCED_CRAFTING_TABLE,
                (table, direction) -> table.getItemHandler(direction));
        event.registerBlockEntity(Attachments.Item.BLOCK, BCSiliconBlockEntities.INTEGRATION_TABLE,
                (table, direction) -> table.getItemHandler(direction));
    }
}

