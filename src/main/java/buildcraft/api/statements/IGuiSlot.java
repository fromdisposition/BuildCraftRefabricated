package buildcraft.api.statements;

import buildcraft.api.core.IConvertable;
import buildcraft.api.core.render.ISprite;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;

public interface IGuiSlot extends IConvertable {
   String getUniqueTag();

   String getDescription();

   default List<String> getTooltip() {
      String desc = this.getDescription();
      return desc == null ? ImmutableList.of() : ImmutableList.of(desc);
   }

   @Nullable
   ISprite getSprite();
}
