/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.ledger;

import java.util.function.Supplier;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import buildcraft.lib.gui.BCGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import buildcraft.lib.gui.BuildCraftGui;

@SuppressWarnings("this-escape")
public class LedgerOwnership extends Ledger_Neptune {
    private static final int COLOUR = 0xE0F0FF;

    private static final Identifier STEVE_SKIN = Identifier.parse("textures/entity/player/wide/steve.png");

    private final Supplier<GameProfile> ownerSupplier;

    public LedgerOwnership(BuildCraftGui gui, Supplier<GameProfile> ownerSupplier, boolean expandPositive) {
        super(gui, COLOUR, expandPositive);
        this.ownerSupplier = ownerSupplier;
        this.title = "gui.owner";

        appendText(() -> {
            GameProfile profile = ownerSupplier.get();
            return profile != null ? profile.name() : "Unknown";
        }, 0x000000);

        calculateMaxSize();
    }

    @Override
    protected void drawIcon(double x, double y, BCGraphics graphics) {
        Identifier skinTexture = getSkinTexture(ownerSupplier.get());

        graphics.blit(RenderPipelines.GUI_TEXTURED, skinTexture,
            (int) x, (int) y, 8f, 8f, 16, 16, 8, 8, 64, 64);

        graphics.blit(RenderPipelines.GUI_TEXTURED, skinTexture,
            (int) x, (int) y, 40f, 8f, 16, 16, 8, 8, 64, 64);
    }

    private static Identifier getSkinTexture(GameProfile profile) {
        if (profile == null || profile.id() == null) {
            return STEVE_SKIN;
        }
        try {

            var connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                PlayerInfo info = connection.getPlayerInfo(profile.id());
                if (info != null) {
                    var skin = info.getSkin();
                    var bodyTex = skin.body();
                    if (bodyTex != null) {
                        return bodyTex.id();
                    }
                }
            }
        } catch (Exception e) {

        }
        return STEVE_SKIN;
    }
}
