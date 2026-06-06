package buildcraft.fabric.client.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;

public final class RenderTooltipEvent {
   public static final class Pre {
      private static final List<Consumer<RenderTooltipEvent.Pre>> LISTENERS = new ArrayList<>();
      private final GuiGraphicsExtractor graphics;
      private final ItemStack stack;
      private final Font font;
      private final List<ClientTooltipComponent> components;
      private final ClientTooltipPositioner tooltipPositioner;
      private final int screenWidth;
      private final int screenHeight;
      private final int x;
      private final int y;

      public static void register(Consumer<RenderTooltipEvent.Pre> listener) {
         LISTENERS.add(listener);
      }

      public static void fire(RenderTooltipEvent.Pre event) {
         for (Consumer<RenderTooltipEvent.Pre> listener : LISTENERS) {
            listener.accept(event);
         }
      }

      public Pre(
         GuiGraphicsExtractor graphics,
         ItemStack stack,
         Font font,
         List<ClientTooltipComponent> components,
         ClientTooltipPositioner tooltipPositioner,
         int screenWidth,
         int screenHeight,
         int x,
         int y
      ) {
         this.graphics = graphics;
         this.stack = stack;
         this.font = font;
         this.components = components;
         this.tooltipPositioner = tooltipPositioner;
         this.screenWidth = screenWidth;
         this.screenHeight = screenHeight;
         this.x = x;
         this.y = y;
      }

      public GuiGraphicsExtractor getGraphics() {
         return this.graphics;
      }

      public ItemStack getItemStack() {
         return this.stack;
      }

      public Font getFont() {
         return this.font;
      }

      public List<ClientTooltipComponent> getComponents() {
         return this.components;
      }

      public ClientTooltipPositioner getTooltipPositioner() {
         return this.tooltipPositioner;
      }

      public int getScreenWidth() {
         return this.screenWidth;
      }

      public int getScreenHeight() {
         return this.screenHeight;
      }

      public int getX() {
         return this.x;
      }

      public int getY() {
         return this.y;
      }
   }
}
