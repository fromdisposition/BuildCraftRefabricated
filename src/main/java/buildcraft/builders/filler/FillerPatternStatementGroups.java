/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.filler;

import buildcraft.api.filler.IFillerPattern;
import buildcraft.builders.BCBuildersStatements;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.statement.StatementContext;
import java.util.Arrays;
import java.util.List;

public final class FillerPatternStatementGroups {
   private FillerPatternStatementGroups() {
   }

   public static final StatementContext<IFillerPattern> CONTEXT = () -> List.of(
      group(
         BCBuildersStatements.PATTERN_NONE,
         BCBuildersStatements.PATTERN_BOX,
         BCBuildersStatements.PATTERN_CLEAR,
         BCBuildersStatements.PATTERN_FILL
      ),
      group(
         BCBuildersStatements.PATTERN_FRAME,
         BCBuildersStatements.PATTERN_PYRAMID,
         BCBuildersStatements.PATTERN_SPHERE,
         BCBuildersStatements.PATTERN_EIGHTH_SPHERE
      ),
      group(
         BCBuildersStatements.PATTERN_HEMI_SPHERE,
         BCBuildersStatements.PATTERN_QUARTER_SPHERE,
         BCBuildersStatements.PATTERN_STAIRS
      ),
      group(
         BCBuildersStatements.PATTERN_ARC,
         BCBuildersStatements.PATTERN_CIRCLE,
         BCBuildersStatements.PATTERN_HEXAGON,
         BCBuildersStatements.PATTERN_OCTAGON
      ),
      group(
         BCBuildersStatements.PATTERN_PENTAGON,
         BCBuildersStatements.PATTERN_SEMI_CIRCLE,
         BCBuildersStatements.PATTERN_SQUARE,
         BCBuildersStatements.PATTERN_TRIANGLE
      )
   );

   private static StatementContext.StatementGroup<IFillerPattern> group(IFillerPattern... patterns) {
      return new StatementContext.StatementGroup<IFillerPattern>() {
         @Override
         public List<IFillerPattern> getValues() {
            return Arrays.asList(patterns);
         }

         @Override
         public ISimpleDrawable getSourceIcon() {
            return null;
         }
      };
   }
}
