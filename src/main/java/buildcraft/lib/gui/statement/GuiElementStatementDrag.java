package buildcraft.lib.gui.statement;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IGuiSlot;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BuildCraftGui;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.IMenuElement;
import buildcraft.lib.misc.data.IReference;
import buildcraft.lib.statement.StatementWrapper;
import javax.annotation.Nullable;

public class GuiElementStatementDrag implements IMenuElement {
   public final BuildCraftGui gui;
   private boolean isDragging;
   @Nullable
   private IGuiSlot dragging;

   public GuiElementStatementDrag(BuildCraftGui gui) {
      this.gui = gui;
   }

   public void startDragging(IGuiSlot slot) {
      this.isDragging = true;
      this.dragging = slot;
      this.gui.currentMenu = this;
   }

   @Override
   public double getX() {
      return 0.0;
   }

   @Override
   public double getY() {
      return 0.0;
   }

   @Override
   public double getWidth() {
      return 0.0;
   }

   @Override
   public double getHeight() {
      return 0.0;
   }

   @Override
   @SuppressWarnings("unchecked")
   public void drawForeground(float partialTicks) {
      if (this.isDragging) {
         boolean canPlace = false;

         for (IGuiElement element : this.gui.getElementsAt(this.gui.mouse.getX(), this.gui.mouse.getY())) {
            if (element instanceof IReference && checkCanSet((IReference)element, this.dragging)) {
               canPlace = true;
               break;
            }
         }

         double x = this.gui.mouse.getX() - 9.0;
         double y = this.gui.mouse.getY() - 9.0;
         if (this.dragging instanceof IStatementParameter) {
            ParameterRenderer.draw((IStatementParameter)this.dragging, x, y);
         } else {
            GuiIcon background = GuiElementStatement.SLOT_COLOUR;
            if (this.dragging instanceof StatementWrapper) {
               EnumPipePart part = ((StatementWrapper)this.dragging).sourcePart;
               if (part != EnumPipePart.CENTER) {
                  background = background.offset(0.0, (1 + part.getIndex()) * 18);
               }
            }

            background.drawAt(x, y);
            if (!canPlace) {
               BCGraphics graphics = GuiIcon.getGuiGraphics();
               if (graphics != null) {
                  graphics.fill((int)x, (int)y, (int)x + 18, (int)y + 18, -2130758347);
               }
            }

            if (this.dragging != null) {
               ISprite sprite = this.dragging.getSprite();
               if (sprite != null) {
                  GuiIcon.drawAt(sprite, x + 1.0, y + 1.0, 16.0);
               }
            }
         }
      }
   }

   private static <T> boolean checkCanSet(IReference<T> ref, Object value) {
      if (value == null) {
         return ref.canSet(null);
      }

      T obj = ref.convertToType(value);
      return obj != null && ref.canSet(obj);
   }

   @Override
   public void onMouseClicked(int button) {
      if (button == 1) {
         for (IGuiElement element : this.gui.getElementsAt(this.gui.mouse.getX(), this.gui.mouse.getY())) {
            if (element instanceof IReference<?> ref) {
               Object obj = ref.get();
               if (obj == null || obj instanceof IGuiSlot) {
                  this.startDragging((IGuiSlot)obj);
                  break;
               }
            }
         }
      }
   }

   @Override
   public void onMouseReleased(int button) {
      if (this.isDragging) {
         for (IGuiElement element : this.gui.getElementsAt(this.gui.mouse.getX(), this.gui.mouse.getY())) {
            if (element instanceof IReference<?> ref) {
               ref.setIfCan(this.dragging);
            }
         }

         this.isDragging = false;
         this.dragging = null;
         if (this.gui.currentMenu == this) {
            this.gui.currentMenu = null;
         }
      }
   }

   @Override
   public boolean shouldFullyOverride() {
      return false;
   }
}
