/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

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
