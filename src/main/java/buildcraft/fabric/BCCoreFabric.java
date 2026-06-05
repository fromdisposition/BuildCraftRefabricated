package buildcraft.fabric;

import buildcraft.api.mj.MjAPI;
import buildcraft.core.BCCore;
import buildcraft.core.BCCoreBlockEntities;
import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemFragileFluidContainer;
import buildcraft.lib.attachments.Attachments;
import buildcraft.lib.attachments.RegisterAttachmentsEvent;
import buildcraft.lib.transfer.fluid.ItemAccessFluidHandler;

public final class BCCoreFabric {
    private BCCoreFabric() {}

    public static void register() {
        registerCapabilities();
    }

    private static void registerCapabilities() {
        RegisterAttachmentsEvent event = new RegisterAttachmentsEvent();

        if (BCCoreItems.FRAGILE_FLUID_CONTAINER != null) {
            event.registerItem(
                    Attachments.Fluid.ITEM,
                    (stack, access) -> new ItemAccessFluidHandler(
                            access, BCCore.FLUID_CONTENT, ItemFragileFluidContainer.MAX_FLUID_HELD),
                    BCCoreItems.FRAGILE_FLUID_CONTAINER);
        }

        if (BCCoreBlockEntities.ENGINE_REDSTONE != null) {
            event.registerBlockEntity(
                    MjAPI.CAP_CONNECTOR,
                    BCCoreBlockEntities.ENGINE_REDSTONE,
                    (engine, direction) -> engine.getMjConnector());
        }

        if (BCCoreBlockEntities.ENGINE_CREATIVE != null) {
            event.registerBlockEntity(
                    MjAPI.CAP_CONNECTOR,
                    BCCoreBlockEntities.ENGINE_CREATIVE,
                    (engine, direction) -> engine.getMjConnector());
        }

        if (BCCoreBlockEntities.POWER_TESTER != null) {
            event.registerBlockEntity(
                    MjAPI.CAP_RECEIVER,
                    BCCoreBlockEntities.POWER_TESTER,
                    (tester, direction) -> tester);
        }
    }
}
