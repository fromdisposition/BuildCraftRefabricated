package buildcraft.api.statements;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import buildcraft.api.core.IConvertable;
import buildcraft.api.core.render.ISprite;

public interface IGuiSlot extends IConvertable {

    String getUniqueTag();

    String getDescription();

    default List<String> getTooltip() {
        String desc = getDescription();
        if (desc == null) {
            return ImmutableList.of();
        }
        return ImmutableList.of(desc);
    }

    @Nullable
    ISprite getSprite();
}
