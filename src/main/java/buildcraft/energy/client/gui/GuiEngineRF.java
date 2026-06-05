package buildcraft.energy.client.gui;

import buildcraft.lib.gui.BCGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import buildcraft.energy.container.ContainerEngineRF;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.ledger.LedgerEngine;
import buildcraft.lib.gui.ledger.LedgerOwnership;
import buildcraft.lib.misc.LocaleUtil;

public class GuiEngineRF extends GuiBC8<ContainerEngineRF> {
    private static final Identifier TEXTURE = Identifier.parse("buildcraftenergy:textures/gui/rf_engine_gui.png");
    private static final int SIZE_X = 176, SIZE_Y = 177;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_RF = new GuiIcon(TEXTURE, SIZE_X, 0, 16, 60);
    private static final buildcraft.lib.gui.pos.GuiRectangle RECT_UPGRADE_HELP = new buildcraft.lib.gui.pos.GuiRectangle(62, 44, 70, 16);
    private static final buildcraft.lib.gui.pos.GuiRectangle RECT_UPGRADE_TOOLTIP = new buildcraft.lib.gui.pos.GuiRectangle(60, 20, 74, 20);
    private static final buildcraft.lib.gui.pos.GuiRectangle RECT_RF_BATTERY = new buildcraft.lib.gui.pos.GuiRectangle(30, 17, 8, 62);

    public GuiEngineRF(ContainerEngineRF menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, SIZE_X, SIZE_Y);
    }

    @Override
    protected void initGuiElements() {
        if (menu.engine != null) {
            mainGui.shownElements.add(new LedgerOwnership(mainGui,
                () -> menu.engine != null ? menu.engine.getOwner() : null,
                true
            ));

            mainGui.shownElements.add(new LedgerEngine(mainGui,
                menu::getSyncedCurrentOutput,
                menu::getSyncedPower,
                menu::getSyncedHeat,
                menu::getSyncedPowerStage,
                menu::isSyncedBurningEngine,
                true
            ));

            mainGui.shownElements.add(new DummyHelpElement(
                RECT_UPGRADE_HELP.offset(mainGui.rootElement),
                new ElementHelpInfo("buildcraft.help.rf_engine.upgrades.title", 0xFF_66_99_FF,
                    "buildcraft.help.rf_engine.upgrades")
            ));

            mainGui.shownElements.add(new buildcraft.lib.gui.GuiElementSimple(mainGui, RECT_UPGRADE_TOOLTIP.offset(mainGui.rootElement)) {
                @Override
                public void addToolTips(java.util.List<buildcraft.lib.gui.elem.ToolTip> tooltips) {
                    if (contains(mainGui.mouse)) {
                        java.util.List<String> lines = new java.util.ArrayList<>();
                        lines.add(LocaleUtil.localize("buildcraft.gui.rf_engine.upgrade_types"));
                        buildcraft.energy.tile.TileEngineRF.initUpgrades();
                        String unitLabel = LocaleUtil.localize(
                            buildcraft.energy.BCEnergyConfig.rfFeKey("buildcraft.gui.rf_engine.upgrade_rate_unit"));
                        for (java.util.Map.Entry<net.minecraft.world.item.Item, Long> entry : buildcraft.energy.tile.TileEngineRF.UPGRADE_VALUES.entrySet()) {
                            String itemName = new net.minecraft.world.item.ItemStack(entry.getKey()).getHoverName().getString();
                            long mj = entry.getValue();
                            int rfPerTick = (int) (mj / 100000L);
                            lines.add(itemName + " = +" + (rfPerTick * 20) + " " + unitLabel);
                        }
                        tooltips.add(new buildcraft.lib.gui.elem.ToolTip(lines));
                    }
                }
            });

            mainGui.shownElements.add(new buildcraft.lib.gui.GuiElementSimple(mainGui, RECT_RF_BATTERY.offset(mainGui.rootElement)) {
                @Override
                public void addHelpElements(java.util.List<ElementHelpInfo.HelpPosition> elements) {

                    int rfPerTick = menu.engine.getFeConsumptionRate();
                    long mjPerTick = menu.engine.getMjPerTick();
                    String rf = LocaleUtil.localizeRfFlow(rfPerTick);
                    String mj = LocaleUtil.localizeMjFlow(mjPerTick);
                    String conversion = LocaleUtil.localize(
                        buildcraft.energy.BCEnergyConfig.rfFeKey("buildcraft.help.rf_engine.battery"), rf, mj);
                    ElementHelpInfo help = ElementHelpInfo
                        .preTranslated(
                            buildcraft.energy.BCEnergyConfig.rfFeKey("buildcraft.help.rf_engine.battery.title"),
                            0xFF_33_AA_33, conversion);
                    elements.add(help.target(this));
                }

                @Override
                public void addToolTips(java.util.List<buildcraft.lib.gui.elem.ToolTip> tooltips) {
                    if (contains(mainGui.mouse)) {
                        int current = menu.getSyncedFeStored();
                        int max = buildcraft.energy.tile.TileEngineRF.MAX_FE;
                        tooltips.add(new buildcraft.lib.gui.elem.ToolTip(current + " / " + max + " FE"));
                    }
                }
            });
        }
    }

    @Override
    protected void drawBackgroundTexture(BCGraphics graphics) {
        ICON_GUI.drawAt(mainGui.rootElement);

        int x = (int) mainGui.rootElement.getX();
        int y = (int) mainGui.rootElement.getY();

        net.minecraft.world.item.ItemStack gearIron = new net.minecraft.world.item.ItemStack(buildcraft.core.BCCoreItems.GEAR_IRON);
        net.minecraft.world.item.ItemStack gearGold = new net.minecraft.world.item.ItemStack(buildcraft.core.BCCoreItems.GEAR_GOLD);

        graphics.item(gearIron, x + 78, y + 21);
        graphics.item(gearGold, x + 101, y + 21);

        graphics.blit(
            net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE,
            x + 57, y + 18,
            57.0f, 18.0f,
            80, 23,
            80, 23,
            256, 256,
            0xA5FFFFFF
        );

        double rfHeight = 60.0 * menu.getSyncedFeStored() / buildcraft.energy.tile.TileEngineRF.MAX_FE;
        double scale = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScale();
        rfHeight = (Math.round(rfHeight * scale)) / scale;
        ICON_RF.drawCutInside(new buildcraft.lib.gui.pos.GuiRectangle(31, 18 + 60 - rfHeight, 6, rfHeight).offset(mainGui.rootElement));
    }

    @Override
    protected void drawForegroundLayer() {
        BCGraphics graphics = GuiIcon.getGuiGraphics();
        String str = title.getString();
        int strWidth = font.width(str);
        int titleX = (imageWidth - strWidth) / 2;
        graphics.text(font, str, titleX, 6, 0xFF404040, false);
        graphics.text(font, playerInventoryTitle, 8, imageHeight - 96 + 2, 0xFF404040, false);
    }
}
