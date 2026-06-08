/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.filler;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IGuiSlot;
import buildcraft.builders.BCBuildersStatements;
import buildcraft.builders.snapshot.pattern.PatternShape2d;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.statement.StatementContext;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public enum FillerStatementContext implements StatementContext<IFillerPattern> {
   CONTEXT_ALL;

   private static final List<FillerStatementContext.Group> groups = ImmutableList.copyOf(FillerStatementContext.Group.values());

   public static void setupPossible() {
      for (FillerStatementContext.Group group : FillerStatementContext.Group.values()) {
         group.patterns.clear();
      }

      for (IFillerPattern pattern : FillerManager.registry.getPatterns()) {
         if (pattern instanceof PatternShape2d) {
            FillerStatementContext.Group.SHAPES_2D.patterns.add(pattern);
         } else {
            FillerStatementContext.Group.DEFAULT.patterns.add(pattern);
         }
      }

      for (FillerStatementContext.Group group : FillerStatementContext.Group.values()) {
         group.patterns.sort(Comparator.comparing(IGuiSlot::getUniqueTag));
      }

      if (FillerStatementContext.Group.DEFAULT.patterns.remove(BCBuildersStatements.PATTERN_NONE)) {
         FillerStatementContext.Group.DEFAULT.patterns.add(0, BCBuildersStatements.PATTERN_NONE);
      }
   }

   @Override
   public List<FillerStatementContext.Group> getAllPossible() {
      return groups;
   }

   static {
      setupPossible();
   }

   public enum Group implements StatementContext.StatementGroup<IFillerPattern> {
      DEFAULT,
      SHAPES_2D;

      final List<IFillerPattern> patterns = new ArrayList<>();

      @Override
      public ISimpleDrawable getSourceIcon() {
         return null;
      }

      @Override
      public List<IFillerPattern> getValues() {
         return this.patterns;
      }
   }
}
