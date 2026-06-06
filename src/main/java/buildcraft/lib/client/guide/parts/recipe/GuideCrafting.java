package buildcraft.lib.client.guide.parts.recipe;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuidePartItem;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.recipe.ChangingItemStack;
import java.util.Arrays;

public class GuideCrafting extends GuidePartItem {
   public static final GuiIcon CRAFTING_GRID = new GuiIcon(GuiGuide.ICONS_2, 119.0, 0.0, 116.0, 54.0);
   public static final GuiRectangle[][] ITEM_POSITION = new GuiRectangle[3][3];
   public static final GuiRectangle OUT_POSITION = new GuiRectangle(95.0, 19.0, 16.0, 16.0);
   public static final GuiRectangle OFFSET = new GuiRectangle(
      (GuiGuide.PAGE_LEFT_TEXT.width - CRAFTING_GRID.width) / 2.0, 0.0, CRAFTING_GRID.width, CRAFTING_GRID.height
   );
   public static final int PIXEL_HEIGHT = 60;
   private final ChangingItemStack[][] input;
   private final ChangingItemStack output;
   private final int hash;

   GuideCrafting(GuiGuide gui, ChangingItemStack[][] input, ChangingItemStack output) {
      super(gui);
      this.input = input;
      this.output = output;
      this.hash = Arrays.deepHashCode(new Object[]{input, output});
   }

   @Override
   public int hashCode() {
      return this.hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (this.getClass() != obj.getClass()) {
         return false;
      }

      GuideCrafting other = (GuideCrafting)obj;
      return Arrays.deepEquals(this.input, other.input) && this.output.equals(other.output);
   }

   @Override
   public GuidePart.PagePosition renderIntoArea(int x, int y, int width, int height, GuidePart.PagePosition current, int index) {
      if (current.pixel + 60 > height) {
         current = current.newPage();
      }

      x += (int)OFFSET.x;
      y += (int)OFFSET.y + current.pixel;
      if (current.page == index) {
         CRAFTING_GRID.drawAt(x, y);

         for (int itemX = 0; itemX < this.input.length; itemX++) {
            for (int itemY = 0; itemY < this.input[itemX].length; itemY++) {
               GuiRectangle rect = ITEM_POSITION[itemX][itemY];
               this.drawItemStack(this.input[itemX][itemY].get(), x + (int)rect.x, y + (int)rect.y);
            }
         }

         this.drawItemStack(this.output.get(), x + (int)OUT_POSITION.x, y + (int)OUT_POSITION.y);
      }

      return current.nextLine(60, height);
   }

   @Override
   public GuidePart.PagePosition handleMouseClick(int x, int y, int width, int height, GuidePart.PagePosition current, int index, int mouseX, int mouseY) {
      if (current.pixel + 60 > height) {
         current = current.newPage();
      }

      x += (int)OFFSET.x;
      y += (int)OFFSET.y + current.pixel;
      if (current.page == index) {
         for (int itemX = 0; itemX < this.input.length; itemX++) {
            for (int itemY = 0; itemY < this.input[itemX].length; itemY++) {
               GuiRectangle rect = ITEM_POSITION[itemX][itemY];
               this.testClickItemStack(this.input[itemX][itemY].get(), x + (int)rect.x, y + (int)rect.y);
            }
         }

         this.testClickItemStack(this.output.get(), x + (int)OUT_POSITION.x, y + (int)OUT_POSITION.y);
      }

      return current.nextLine(60, height);
   }

   static {
      for (int x = 0; x < 3; x++) {
         for (int y = 0; y < 3; y++) {
            ITEM_POSITION[x][y] = new GuiRectangle(1 + x * 18, 1 + y * 18, 16.0, 16.0);
         }
      }
   }
}
