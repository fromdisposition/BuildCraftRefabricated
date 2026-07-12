/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.registry;

import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.filler.IFillerRegistry;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Template;
import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

public enum FillerRegistry implements IFillerRegistry {
   INSTANCE;

   private final Map<String, IFillerPattern> patterns = new LinkedHashMap<>();

   @Override
   public void addPattern(IFillerPattern pattern) {
      this.patterns.put(pattern.getUniqueTag(), pattern);
   }

   @Nullable
   @Override
   public IFillerPattern getPattern(String name) {
      return this.patterns.get(name);
   }

   @Override
   public Collection<IFillerPattern> getPatterns() {
      return this.patterns.values();
   }

   @Override
   public IFilledTemplate createFilledTemplate(BlockPos pos, BlockPos size) {
      Template template = new Template();
      template.size = size;
      template.offset = pos;
      template.data = new BitSet(Snapshot.getDataSize(size));
      return template.getFilledTemplate();
   }
}
