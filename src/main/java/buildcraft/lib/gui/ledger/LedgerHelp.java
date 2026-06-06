package buildcraft.lib.gui.ledger;

import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.LocaleUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class LedgerHelp extends Ledger_Neptune {
   private static final Identifier ICON_HELP = Identifier.parse("buildcraftlib:textures/icons/help.png");
   private static final int BORDER = 2;
   private IGuiElement selected = null;
   private boolean foundAny = false;
   private boolean init = false;
   private ElementHelpInfo currentHelpInfo = null;

   public LedgerHelp(BuildCraftGui gui, boolean expandPositive) {
      super(gui, -3368449, expandPositive);
      this.title = "gui.ledger.help";
      this.calculateMaxSize();
   }

   @Override
   public void tick() {
      super.tick();
      if (this.currentWidth == 22.0 && this.currentHeight == 24.0) {
         this.selected = null;
         this.currentHelpInfo = null;
      }
   }

   @Override
   protected void drawIcon(double x, double y, BCGraphics graphics) {
      if (!this.init) {
         this.init = true;
         List<ElementHelpInfo.HelpPosition> elements = new ArrayList<>();

         for (IGuiElement element : this.gui.shownElements) {
            element.addHelpElements(elements);
         }

         this.foundAny = !elements.isEmpty();
      }

      graphics.blit(RenderPipelines.GUI_TEXTURED, ICON_HELP, (int)x, (int)y, 0.0F, 0.0F, 16, 16, 16, 16);
   }

   @Override
   public void drawBackground(float partialTicks) {
      super.drawBackground(partialTicks);
      if (this.shouldDrawOpen()) {
         BCGraphics graphics = GuiIcon.getGuiGraphics();
         if (graphics != null) {
            boolean set = false;
            List<ElementHelpInfo.HelpPosition> elements = new ArrayList<>();

            for (IGuiElement element : this.gui.shownElements) {
               element.addHelpElements(elements);
               this.foundAny = this.foundAny | !elements.isEmpty();

               for (ElementHelpInfo.HelpPosition info : elements) {
                  IGuiArea rect = info.target;
                  boolean isHovered = rect.contains(this.gui.mouse);
                  if (isHovered && !set) {
                     if (this.selected != element) {
                        this.selected = element;
                        this.updateHelpText(info.info);
                     }

                     set = true;
                  }

                  boolean isSelected = this.selected == element;
                  this.drawHighlightBorder(graphics, rect, info.info.colour, isHovered, isSelected);
               }

               elements.clear();
            }
         }
      }
   }

   private void drawHighlightBorder(BCGraphics graphics, IGuiArea rect, int colour, boolean isHovered, boolean isSelected) {
      int x = (int)rect.getX();
      int y = (int)rect.getY();
      int w = (int)rect.getWidth();
      int h = (int)rect.getHeight();
      int alpha;
      if (isHovered && isSelected) {
         alpha = 221;
      } else if (!isHovered && !isSelected) {
         alpha = 136;
      } else {
         alpha = 187;
      }

      int borderColour = alpha << 24 | colour & 16777215;
      int bx = x - 2;
      int by = y - 2;
      int bw = w + 4;
      int bh = h + 4;
      graphics.fill(bx, by, bx + bw, by + 2, borderColour);
      graphics.fill(bx, y + h, bx + bw, y + h + 2, borderColour);
      graphics.fill(bx, by + 2, bx + 2, y + h, borderColour);
      graphics.fill(x + w, by + 2, x + w + 2, y + h, borderColour);
      if (isHovered || isSelected) {
         int fillAlpha = isHovered ? 51 : 34;
         int fillColour = fillAlpha << 24 | colour & 16777215;
         graphics.fill(x, y, x + w, y + h, fillColour);
      }
   }

   private void updateHelpText(ElementHelpInfo info) {
      if (info != this.currentHelpInfo) {
         this.currentHelpInfo = info;
         this.clearTextEntries();
         String localizedTitle = LocaleUtil.localize(info.title);
         this.appendText(localizedTitle, info.colour & 16777215).setDropShadow(true);

         for (String key : info.localeKeys) {
            if (key != null) {
               String text;
               if (info.isPreTranslated) {
                  text = key;
               } else {
                  text = LocaleUtil.localize(key);
               }

               if (!text.isEmpty()) {
                  this.appendText(text, 16777215);
               }
            }
         }

         this.calculateMaxSize();
      }
   }
}
