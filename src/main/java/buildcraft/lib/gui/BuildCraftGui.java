package buildcraft.lib.gui;

import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.gui.pos.IGuiPosition;
import buildcraft.lib.gui.pos.MousePosition;
import buildcraft.lib.misc.GuiUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class BuildCraftGui {
   public final Minecraft mc = Minecraft.getInstance();
   public final Screen gui;
   public final MousePosition mouse = new MousePosition();
   public final IGuiArea screenElement;
   public final IGuiArea rootElement;
   public final List<IGuiElement> shownElements = new ArrayList<>();
   public IMenuElement currentMenu;
   public IGuiPosition lowerLeftLedgerPos;
   public IGuiPosition lowerRightLedgerPos;
   private float lastPartialTicks;

   public BuildCraftGui(Screen gui, IGuiArea rootElement) {
      this.gui = gui;
      this.screenElement = GuiUtil.AREA_WHOLE_SCREEN;
      this.rootElement = rootElement;
      this.lowerLeftLedgerPos = rootElement.offset(0.0, 5.0);
      this.lowerRightLedgerPos = rootElement.getPosition(1, -1).offset(0.0, 5.0);
   }

   public BuildCraftGui(Screen gui) {
      this.gui = gui;
      this.screenElement = GuiUtil.AREA_WHOLE_SCREEN;
      this.rootElement = this.screenElement;
      this.lowerLeftLedgerPos = this.screenElement.getPosition(1, -1).offset(-5.0, 5.0);
      this.lowerRightLedgerPos = this.screenElement.offset(5.0, 5.0);
   }

   public static IGuiArea createWindowedArea(BcScreen<?> gui) {
      return IGuiArea.create(() -> gui.getGuiLeftPos(), () -> gui.getGuiTopPos(), () -> gui.getGuiImageWidth(), () -> gui.getGuiImageHeight());
   }

   public final float getLastPartialTicks() {
      return this.lastPartialTicks;
   }

   public void tick() {
      if (this.currentMenu != null) {
         this.currentMenu.tick();
      }

      for (IGuiElement element : this.shownElements) {
         element.tick();
      }
   }

   public List<IGuiElement> getElementsAt(double x, double y) {
      List<IGuiElement> elements = new ArrayList<>();
      IMenuElement m = this.currentMenu;
      if (m != null) {
         elements.addAll(m.getThisAndChildrenAt(x, y));
         if (m.shouldFullyOverride()) {
            return elements;
         }
      }

      for (IGuiElement elem : this.shownElements) {
         elements.addAll(elem.getThisAndChildrenAt(x, y));
      }

      return elements;
   }

   public void drawBackgroundLayer(float partialTicks, int mouseX, int mouseY, Runnable menuBackgroundRenderer) {
      partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
      this.lastPartialTicks = partialTicks;
      this.mouse.setMousePosition(mouseX, mouseY);
      menuBackgroundRenderer.run();
   }

   public void drawElementBackgrounds() {
      for (IGuiElement element : this.shownElements) {
         if (element != this.currentMenu) {
            element.drawBackground(this.lastPartialTicks);
         }
      }
   }

   public void drawDragLayer(BCGraphics graphics) {
      IMenuElement m = this.currentMenu;
      if (m != null && !m.shouldFullyOverride()) {
         m.drawBackground(this.lastPartialTicks);
         m.drawForeground(this.lastPartialTicks);
      }
   }

   public void drawMenuOverlayLayer(BCGraphics graphics) {
      IMenuElement m = this.currentMenu;
      if (m != null && m.shouldFullyOverride() && graphics != null) {
         int sx = (int)this.screenElement.getX();
         int sy = (int)this.screenElement.getY();
         int sw = (int)this.screenElement.getWidth();
         int sh = (int)this.screenElement.getHeight();
         graphics.fill(sx, sy, sx + sw, sy + sh, -1072689136);
         m.drawBackground(this.lastPartialTicks);
         m.drawForeground(this.lastPartialTicks);
      }
   }

   public void preDrawForeground() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      if (graphics != null) {
         graphics.pose().pushMatrix();
         graphics.pose().translate((float)(-this.rootElement.getX()), (float)(-this.rootElement.getY()));
      }
   }

   public void postDrawForeground() {
      BCGraphics graphics = GuiIcon.getGuiGraphics();
      if (graphics != null) {
         graphics.pose().popMatrix();
      }
   }

   public void drawElementForegrounds(Runnable menuBackgroundRenderer) {
      for (IGuiElement element : this.shownElements) {
         if (element != this.currentMenu) {
            element.drawForeground(this.lastPartialTicks);
         }
      }

      IMenuElement m = this.currentMenu;
      List<ToolTip> tooltips = new ArrayList<>();
      if (m != null && m.shouldFullyOverride()) {
         if (m instanceof ITooltipElement) {
            m.addToolTips(tooltips);
         }
      } else {
         if (m instanceof ITooltipElement) {
            m.addToolTips(tooltips);
         }

         for (IGuiElement element : this.shownElements) {
            if (element instanceof ITooltipElement) {
               element.addToolTips(tooltips);
            }
         }
      }

      if (!tooltips.isEmpty()) {
         BCGraphics graphics = GuiIcon.getGuiGraphics();
         if (graphics != null) {
            List<FormattedCharSequence> comps = new ArrayList<>();

            for (ToolTip tip : tooltips) {
               for (String str : tip) {
                  comps.add(Component.literal(str).getVisualOrderText());
               }
            }

            if (!comps.isEmpty()) {
               graphics.setTooltipForNextFrame(Minecraft.getInstance().font, comps, (int)this.mouse.getX(), (int)this.mouse.getY());
            }
         }
      }
   }

   public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
      this.mouse.setMousePosition(mouseX, mouseY);
      IMenuElement m = this.currentMenu;
      if (m != null) {
         m.onMouseClicked(mouseButton);
         if (m.shouldFullyOverride()) {
            return true;
         }
      }

      for (IGuiElement element : this.shownElements) {
         if (element instanceof IInteractionElement) {
            ((IInteractionElement)element).onMouseClicked(mouseButton);
         }
      }

      return false;
   }

   public void onMouseDragged(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
      this.mouse.setMousePosition(mouseX, mouseY);
      IMenuElement m = this.currentMenu;
      if (m != null) {
         m.onMouseDragged(clickedMouseButton, timeSinceLastClick);
         if (m.shouldFullyOverride()) {
            return;
         }
      }

      for (IGuiElement element : this.shownElements) {
         if (element instanceof IInteractionElement) {
            ((IInteractionElement)element).onMouseDragged(clickedMouseButton, timeSinceLastClick);
         }
      }
   }

   public void onMouseReleased(int mouseX, int mouseY, int state) {
      this.mouse.setMousePosition(mouseX, mouseY);
      IMenuElement m = this.currentMenu;
      if (m != null) {
         m.onMouseReleased(state);
         if (m.shouldFullyOverride()) {
            return;
         }
      }

      for (IGuiElement element : this.shownElements) {
         if (element instanceof IInteractionElement) {
            ((IInteractionElement)element).onMouseReleased(state);
         }
      }
   }

   public boolean onKeyTyped(char typedChar, int keyCode) {
      boolean action = false;
      IMenuElement m = this.currentMenu;
      if (m != null) {
         action = m.onKeyPress(typedChar, keyCode);
         if (action && m.shouldFullyOverride()) {
            return true;
         }
      }

      for (IGuiElement element : this.shownElements) {
         if (element instanceof IInteractionElement) {
            action |= ((IInteractionElement)element).onKeyPress(typedChar, keyCode);
         }
      }

      return action;
   }
}
