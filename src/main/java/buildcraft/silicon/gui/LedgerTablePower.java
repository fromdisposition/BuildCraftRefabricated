/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gui;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.ledger.Ledger_Neptune;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.silicon.tile.TileLaserTableBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class LedgerTablePower extends Ledger_Neptune {
   private static final int OVERLAY_COLOUR = -2855905;
   private static final int SUB_HEADER_COLOUR = -5591112;
   private static final int TEXT_COLOUR = -16777216;
   private static final SpriteHolderRegistry.SpriteHolder ICON_ACTIVE = SpriteHolderRegistry.getHolder("buildcraftlib:icons/engine_active");
   private static final SpriteHolderRegistry.SpriteHolder ICON_INACTIVE = SpriteHolderRegistry.getHolder("buildcraftlib:icons/engine_inactive");
   public final TileLaserTableBase tile;

   public LedgerTablePower(BuildCraftGui gui, TileLaserTableBase tile, boolean expandPositive) {
      super(gui, OVERLAY_COLOUR, expandPositive);
      this.tile = tile;
      this.title = "gui.power";
      this.appendText(LocaleUtil.localize("gui.assemblyCurrentRequired") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
      this.appendText(() -> LocaleUtil.localizeMj(tile.getRemainingRequiredPower()), TEXT_COLOUR);
      this.appendText(LocaleUtil.localize("gui.stored") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
      this.appendText(() -> LocaleUtil.localizeMj(tile.power), TEXT_COLOUR);
      this.appendText(LocaleUtil.localize("gui.assemblyRate") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
      this.appendText(() -> LocaleUtil.localizeMjFlow(tile.avgPowerClient), TEXT_COLOUR);
      this.calculateMaxSize();
   }

   @Override
   protected void drawIcon(double x, double y, BCGraphics graphics) {
      SpriteHolderRegistry.SpriteHolder holder = this.tile.avgPowerClient > 0L ? ICON_ACTIVE : ICON_INACTIVE;
      TextureAtlasSprite sprite = holder.getSprite();
      if (sprite != null) {
         graphics.blitSprite(sprite, (int)x, (int)y, 16, 16);
      }
   }
}
