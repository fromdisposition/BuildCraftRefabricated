package buildcraft.silicon.gui;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.ledger.Ledger_Neptune;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.silicon.tile.TileLaserTableBase;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class LedgerTablePower extends Ledger_Neptune {
   private static final int OVERLAY_COLOUR = -2855905;
   private static final int SUB_HEADER_COLOUR = -5591112;
   private static final int TEXT_COLOUR = -16777216;
   private static final SpriteHolderRegistry.SpriteHolder ICON_ACTIVE = SpriteHolderRegistry.getHolder("buildcraftlib:icons/engine_active");
   private static final SpriteHolderRegistry.SpriteHolder ICON_INACTIVE = SpriteHolderRegistry.getHolder("buildcraftlib:icons/engine_inactive");
   public final TileLaserTableBase tile;

   public LedgerTablePower(BuildCraftGui gui, TileLaserTableBase tile, boolean expandPositive) {
      super(gui, -2855905, expandPositive);
      this.tile = tile;
      this.title = "gui.power";
      this.appendText(LocaleUtil.localize("gui.assemblyCurrentRequired") + ":", -5591112).setDropShadow(true);
      this.appendText(() -> LocaleUtil.localizeMj(tile.getTarget()), -16777216);
      this.appendText(LocaleUtil.localize("gui.stored") + ":", -5591112).setDropShadow(true);
      this.appendText(() -> LocaleUtil.localizeMj(tile.power), -16777216);
      this.appendText(LocaleUtil.localize("gui.assemblyRate") + ":", -5591112).setDropShadow(true);
      this.appendText(() -> LocaleUtil.localizeMjFlow(tile.avgPowerClient), -16777216);
      this.calculateMaxSize();
   }

   @Override
   protected void drawIcon(double x, double y, BCGraphics graphics) {
      SpriteHolderRegistry.SpriteHolder holder = this.tile.avgPowerClient > 0L ? ICON_ACTIVE : ICON_INACTIVE;
      TextureAtlasSprite sprite = holder.getSprite();
      if (sprite != null) {
         graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, (int)x, (int)y, 16, 16);
      }
   }
}
