/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot.pattern;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;
import buildcraft.builders.BCBuildersStatements;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.misc.LocaleUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class Pattern extends BCStatement implements IFillerPattern, IActionExternal {
   private final String desc;

   public Pattern(String tag) {
      super("buildcraft:" + tag);
      this.desc = "fillerpattern." + tag;
      FillerManager.registry.addPattern(this);
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize(this.desc);
   }

   @Override
   public void actionActivate(BlockEntity target, Direction side, IStatementContainer source, IStatementParameter[] parameters) {
      if (source instanceof IFillerStatementContainer) {
         ((IFillerStatementContainer)source).setPattern(this, parameters);
      } else if (target instanceof IFillerStatementContainer) {
         ((IFillerStatementContainer)target).setPattern(this, parameters);
      }
   }

   @Override
   public IFillerPattern[] getPossible() {
      return BCBuildersStatements.PATTERNS;
   }
}
