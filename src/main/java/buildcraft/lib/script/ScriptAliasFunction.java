/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.script;

import javax.annotation.Nullable;

public class ScriptAliasFunction {
   public final String name;
   public final LineData[] rawOutput;
   public final int startLine;
   public final int argCount;
   @Nullable
   public final ScriptAliasDocumentation docs;

   public ScriptAliasFunction(ScriptAliasFunction.AliasBuilder builder) {
      this.name = builder.name;
      this.rawOutput = builder.rawOutputs;
      this.startLine = builder.startLine;
      this.argCount = builder.argCount;
      this.docs = builder.docs;
   }

   public static class AliasBuilder {
      public String name;
      public LineData[] rawOutputs;
      public int startLine;
      public int argCount;
      public ScriptAliasDocumentation docs;
   }
}
