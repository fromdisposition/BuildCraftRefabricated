/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.loader;

import buildcraft.lib.client.guide.entry.PageEntry;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.ProfilerFiller;

public interface IPageLoaderText extends IPageLoader {
   @Override
   default GuidePageFactory loadPage(InputStream in, Identifier name, PageEntry<?> entry, ProfilerFiller prof) throws IOException {
      Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
      return this.loadPage(new BufferedReader(reader), name, entry, prof);
   }

   GuidePageFactory loadPage(BufferedReader var1, Identifier var2, PageEntry<?> var3, ProfilerFiller var4) throws IOException;
}
