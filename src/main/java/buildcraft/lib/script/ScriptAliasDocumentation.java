/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.script;

import java.util.ArrayList;
import java.util.List;

public class ScriptAliasDocumentation {
   public final String description;
   public final String[] paramDescriptions;

   public ScriptAliasDocumentation(String description, String[] paramDescriptions) {
      this.description = description;
      this.paramDescriptions = paramDescriptions;
   }

   public static ScriptAliasDocumentation parse(String[] sections) {
      String desc = "";
      List<String> params = new ArrayList<>();

      for (String s : sections) {
         s = s.trim();
         if (s.startsWith("%:")) {
            params.add("");
            s = s.substring(2).trim();
         }

         if (params.isEmpty()) {
            desc = desc + (desc.isEmpty() ? s : "\n" + s);
         } else {
            int i = params.size() - 1;
            String p = params.get(i);
            params.set(i, p.isEmpty() ? s : p + "\n" + s);
         }
      }

      return new ScriptAliasDocumentation(desc, params.toArray(new String[0]));
   }
}
