package buildcraft.lib.gui.statement;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.ISimpleDrawable;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.world.item.ItemStack;

public class ParameterRenderer {
   private static final ISimpleDrawable BACKGROUND_DRAWABLE = (x, y) -> GuiElementStatement.SLOT_COLOUR.drawAt(x, y);
   private static final Map<IStatementParameter.DrawType, Function<IStatementParameter, ISimpleDrawable>> drawTypes = new EnumMap<>(
      IStatementParameter.DrawType.class
   );

   public static ISimpleDrawable getSpriteDrawable(IStatementParameter param) {
      return (x, y) -> {
         ISprite sprite = param.getSprite();
         if (sprite != null) {
            GuiIcon.drawAt(sprite, x + 1.0, y + 1.0, 16.0);
         }
      };
   }

   public static ISimpleDrawable getStackDrawable(IStatementParameter param, boolean orQuestionMark) {
      return (x, y) -> {
         ItemStack stack = param.getItemStack();
         if (!stack.isEmpty()) {
            BCGraphics graphics = GuiIcon.getGuiGraphics();
            if (graphics != null) {
               graphics.fakeItem(stack, (int)x + 1, (int)y + 1);
            }
         } else if (orQuestionMark) {
            GuiElementStatement.ICON_SLOT_NOT_SET.drawAt(x + 1.0, y + 1.0);
         }
      };
   }

   public static ISimpleDrawable getDrawable(IStatementParameter param) {
      if (param instanceof IDrawingParameter) {
         return BACKGROUND_DRAWABLE.andThen(((IDrawingParameter)param).getDrawable());
      }

      IStatementParameter.DrawType type = param.getDrawType();
      return BACKGROUND_DRAWABLE.andThen(drawTypes.get(type).apply(param));
   }

   public static void draw(IStatementParameter param, double x, double y) {
      getDrawable(param).drawAt(x, y);
   }

   static {
      drawTypes.put(IStatementParameter.DrawType.SPRITE_ONLY, ParameterRenderer::getSpriteDrawable);
      drawTypes.put(IStatementParameter.DrawType.STACK_ONLY, p -> getStackDrawable(p, false));
      drawTypes.put(IStatementParameter.DrawType.STACK_ONLY_OR_QUESTION_MARK, p -> getStackDrawable(p, true));
      drawTypes.put(IStatementParameter.DrawType.SPRITE_STACK, p -> getSpriteDrawable(p).andThen(getStackDrawable(p, false)));
      drawTypes.put(IStatementParameter.DrawType.STACK_SPRITE, p -> getStackDrawable(p, false).andThen(getSpriteDrawable(p)));
      drawTypes.put(IStatementParameter.DrawType.SPRITE_STACK_OR_QUESTION_MARK, p -> getSpriteDrawable(p).andThen(getStackDrawable(p, true)));
      drawTypes.put(IStatementParameter.DrawType.STACK_OR_QUESTION_MARK_THEN_SPRITE, p -> getStackDrawable(p, true).andThen(getSpriteDrawable(p)));
   }
}
