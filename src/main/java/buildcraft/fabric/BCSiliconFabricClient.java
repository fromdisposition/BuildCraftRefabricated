package buildcraft.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.gui.screens.MenuScreens;

import buildcraft.fabric.client.event.RenderLevelStageEvent;
import buildcraft.lib.fabric.client.FabricModelModifyHooks;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.client.BCSiliconClient;
import buildcraft.silicon.client.render.RenderLaser;
import buildcraft.silicon.gui.GuiAdvancedCraftingTable;
import buildcraft.silicon.gui.GuiAssemblyTable;
import buildcraft.silicon.gui.GuiGate;
import buildcraft.silicon.gui.GuiIntegrationTable;

public final class BCSiliconFabricClient {
    private BCSiliconFabricClient() {}

    public static void init() {
        FabricModelModifyHooks.register(BCSiliconClient::onModifyBakingResult);

        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(context ->
                RenderLaser.onRenderLevel(new RenderLevelStageEvent.AfterTranslucentBlocks(
                        context.poseStack(), context.levelState())));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                BCSiliconClient.GameBus.onClientLoggingIn(
                        new buildcraft.fabric.client.event.ClientPlayerNetworkEvent.LoggingIn()));

        MenuScreens.register(BCSiliconMenuTypes.ASSEMBLY_TABLE, GuiAssemblyTable::new);
        MenuScreens.register(BCSiliconMenuTypes.INTEGRATION_TABLE, GuiIntegrationTable::new);
        MenuScreens.register(BCSiliconMenuTypes.ADVANCED_CRAFTING_TABLE, GuiAdvancedCraftingTable::new);
        MenuScreens.register(BCSiliconMenuTypes.GATE, GuiGate::new);
    }
}

