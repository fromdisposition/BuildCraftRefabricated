package buildcraft.lib.gui.ledger;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.misc.LocaleUtil;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class LedgerEngineFE extends Ledger_Neptune {
   private static final int OVERLAY_COLOUR = 13921311;
   private static final int HEADER_COLOUR = 14797103;
   private static final int SUB_HEADER_COLOUR = 11186104;
   private static final int TEXT_COLOUR = 0;
   private static final SpriteHolderRegistry.SpriteHolder ICON_ACTIVE = SpriteHolderRegistry.getHolder("buildcraftlib:icons/engine_active");
   private static final SpriteHolderRegistry.SpriteHolder ICON_INACTIVE = SpriteHolderRegistry.getHolder("buildcraftlib:icons/engine_inactive");
   private static final SpriteHolderRegistry.SpriteHolder ICON_WARM = SpriteHolderRegistry.getHolder("buildcraftlib:icons/engine_warm");
   private static final SpriteHolderRegistry.SpriteHolder ICON_OVERHEAT = SpriteHolderRegistry.getHolder("buildcraftlib:icons/engine_overheat");
   private final Supplier<EnumPowerStage> powerStageSupplier;
   private final Supplier<Boolean> engineOnSupplier;

   public LedgerEngineFE(
      BuildCraftGui gui,
      IntSupplier currentOutputFe,
      LongSupplier storedMj,
      Supplier<Float> heatLevel,
      Supplier<EnumPowerStage> powerStage,
      Supplier<Boolean> engineOn,
      boolean expandPositive
   ) {
      super(gui, 13921311, expandPositive);
      this.title = "gui.power";
      this.powerStageSupplier = powerStage;
      this.engineOnSupplier = engineOn;
      this.appendText(LocaleUtil.localize("gui.currentOutput") + ":", 11186104).setDropShadow(true);
      this.appendText(() -> LocaleUtil.localizeRfFlow(currentOutputFe.getAsInt()), 0);
      this.appendText(LocaleUtil.localize("gui.stored") + ":", 11186104).setDropShadow(true);
      this.appendText(() -> LocaleUtil.localizeMj(storedMj.getAsLong()), 0);
      this.appendText(LocaleUtil.localize("gui.heat") + ":", 11186104).setDropShadow(true);
      this.appendText(() -> LocaleUtil.localizeHeat(heatLevel.get()), 0);
      this.calculateMaxSize();
   }

   @Override
   public int getTitleColour() {
      return 14797103;
   }

   @Override
   protected void drawIcon(double x, double y, BCGraphics graphics) {
      EnumPowerStage stage = this.powerStageSupplier.get();

      TextureAtlasSprite sprite = (switch (stage) {
         case OVERHEAT -> ICON_OVERHEAT;
         case RED, YELLOW -> ICON_WARM;
         default -> this.engineOnSupplier.get() ? ICON_ACTIVE : ICON_INACTIVE;
      }).getSprite();
      if (sprite != null) {
         graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, (int)x, (int)y, 16, 16);
      }
   }
}
