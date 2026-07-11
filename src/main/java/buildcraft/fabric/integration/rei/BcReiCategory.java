package buildcraft.fabric.integration.rei;

import java.util.ArrayList;
import java.util.List;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * One reusable category: the plugin supplies the id, tab icon, translated title (the JEI keys are
 * reused) and a slot-layout function; text {@link BcReiDisplay#lines} render under the layout.
 */
final class BcReiCategory<D extends BcReiDisplay> implements DisplayCategory<D> {
   /** Lays out slots/arrows for one display; text lines are appended below by the category itself. */
   interface SlotLayout<D> {
      void addWidgets(D display, Point origin, List<Widget> widgets);
   }

   private final CategoryIdentifier<D> id;
   private final Renderer icon;
   private final Component title;
   private final int displayHeight;
   private final SlotLayout<D> layout;

   BcReiCategory(CategoryIdentifier<D> id, ItemLike icon, String titleKey, int displayHeight, SlotLayout<D> layout) {
      this.id = id;
      this.icon = EntryStacks.of(new ItemStack(icon));
      this.title = Component.translatable(titleKey);
      this.displayHeight = displayHeight;
      this.layout = layout;
   }

   @Override
   public CategoryIdentifier<? extends D> getCategoryIdentifier() {
      return this.id;
   }

   @Override
   public Renderer getIcon() {
      return this.icon;
   }

   @Override
   public Component getTitle() {
      return this.title;
   }

   @Override
   public int getDisplayHeight() {
      return this.displayHeight;
   }

   @Override
   public List<Widget> setupDisplay(D display, Rectangle bounds) {
      List<Widget> widgets = new ArrayList<>();
      widgets.add(Widgets.createRecipeBase(bounds));
      Point origin = new Point(bounds.getX() + 5, bounds.getY() + 5);
      this.layout.addWidgets(display, origin, widgets);
      int textY = bounds.getMaxY() - 5 - display.lines.size() * 10;
      for (Component line : display.lines) {
         widgets.add(Widgets.createLabel(new Point(origin.x, textY), line).leftAligned().noShadow().color(0xFF404040, 0xFFBBBBBB));
         textY += 10;
      }
      return widgets;
   }

   static Widget slot(EntryIngredient entries, Point origin, int x, int y, boolean output) {
      var slot = Widgets.createSlot(new Point(origin.x + x, origin.y + y)).entries(entries);
      return output ? slot.markOutput() : slot.markInput();
   }

   static Widget arrow(Point origin, int x, int y) {
      return Widgets.createArrow(new Point(origin.x + x, origin.y + y));
   }
}
