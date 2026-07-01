/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.script;

import com.google.gson.JsonSyntaxException;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

public interface IScriptFileLog {
   int END_OF_LINE = Integer.MAX_VALUE;

   void error(int var1, int var2, int var3, String var4);

   default void error(int line, String message) {
      this.error(line, 0, Integer.MAX_VALUE, message);
   }

   void populateFile(@Nullable SourceFile var1, List<String> var2);

   void errorMissingArgument(int var1, int var2, String var3);

   void infoSkippingIfBlock(int var1);

   void infoEndSkipping(int var1);

   void infoConditionalResult(int var1, int var2, int var3, boolean var4);

   void errorFunctionUnknown(int var1, int var2, int var3, Collection<String> var4);

   default void errorStdMissingName(int line) {
      this.error(line, "Missing name: ");
   }

   void errorStdInvalidJson(int var1, JsonSyntaxException var2);

   void errorStdUnknownFile(int var1, String var2);

   default void errorImportMissingFile(int line) {
      this.error(line, "Cannot find the file ");
   }

   void errorImportNotFound(int var1, String var2);

   void errorImportMissingStarter(int var1, String var2);

   void errorImportRecursiveReplace(int var1, String var2);

   void errorAliasInvalidArgCount(int var1, int var2, int var3, @Nullable Integer var4);

   default void errorAliasMissingName(int tokenStart) {
      this.errorMissingArgument(tokenStart, 0, "The custom name for the function");
   }

   default void errorAliasMissingArgCount(int tokenStart) {
      this.errorMissingArgument(tokenStart, 1, "The number of arguments for the function");
   }

   default void errorAliasMissingReplacement(int line) {
      this.errorMissingArgument(line, 2, "The replacement for the alias. This can include ${1} and ${2} etc for the aliased arguments.");
   }

   void replace(int var1, int var2, @Nullable SourceFile var3, int var4, List<String> var5);
}
