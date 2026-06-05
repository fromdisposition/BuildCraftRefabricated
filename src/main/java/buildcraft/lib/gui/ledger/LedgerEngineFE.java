/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.ledger;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.misc.LocaleUtil;

@SuppressWarnings("this-escape")
public class LedgerEngineFE extends Ledger_Neptune {

    private static final int OVERLAY_COLOUR = 0xD46C1F;
    private static final int HEADER_COLOUR = 0xE1C92F;
    private static final int SUB_HEADER_COLOUR = 0xAAAFB8;
    private static final int TEXT_COLOUR = 0x000000;

    private static final SpriteHolder ICON_ACTIVE = SpriteHolderRegistry.getHolder("buildcraftlib:icons/engine_active");
    private static final SpriteHolder ICON_INACTIVE = SpriteHolderRegistry.getHolder("buildcraftlib:icons/engine_inactive");
    private static final SpriteHolder ICON_WARM = SpriteHolderRegistry.getHolder("buildcraftlib:icons/engine_warm");
    private static final SpriteHolder ICON_OVERHEAT = SpriteHolderRegistry.getHolder("buildcraftlib:icons/engine_overheat");

    private final Supplier<EnumPowerStage> powerStageSupplier;
    private final Supplier<Boolean> engineOnSupplier;

    public LedgerEngineFE(BuildCraftGui gui, IntSupplier currentOutputFe, LongSupplier storedMj,
                           Supplier<Float> heatLevel,
                           Supplier<EnumPowerStage> powerStage,
                           Supplier<Boolean> engineOn, boolean expandPositive) {
        super(gui, OVERLAY_COLOUR, expandPositive);
        this.title = "gui.power";
        this.powerStageSupplier = powerStage;
        this.engineOnSupplier = engineOn;

        appendText(LocaleUtil.localize("gui.currentOutput") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeRfFlow(currentOutputFe.getAsInt()), TEXT_COLOUR);
        appendText(LocaleUtil.localize("gui.stored") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeMj(storedMj.getAsLong()), TEXT_COLOUR);
        appendText(LocaleUtil.localize("gui.heat") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeHeat(heatLevel.get()), TEXT_COLOUR);

        calculateMaxSize();
    }

    @Override
    public int getTitleColour() {
        return HEADER_COLOUR;
    }

    @Override
    protected void drawIcon(double x, double y, BCGraphics graphics) {
        SpriteHolder holder;
        EnumPowerStage stage = powerStageSupplier.get();
        switch (stage) {
            case OVERHEAT:
                holder = ICON_OVERHEAT;
                break;
            case RED:
            case YELLOW:
                holder = ICON_WARM;
                break;
            default:
                holder = engineOnSupplier.get() ? ICON_ACTIVE : ICON_INACTIVE;
        }
        TextureAtlasSprite sprite = holder.getSprite();
        if (sprite != null) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, (int) x, (int) y, 16, 16);
        }
    }
}
