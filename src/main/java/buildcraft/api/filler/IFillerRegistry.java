/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.filler;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

public interface IFillerRegistry {
   void addPattern(IFillerPattern var1);

   @Nullable
   IFillerPattern getPattern(String var1);

   Collection<IFillerPattern> getPatterns();

   IFilledTemplate createFilledTemplate(BlockPos var1, BlockPos var2);
}
