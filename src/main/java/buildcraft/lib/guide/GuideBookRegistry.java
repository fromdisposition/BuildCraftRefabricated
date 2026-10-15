/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.guide;

import buildcraft.api.registry.IReloadableRegistry;
import buildcraft.lib.script.ScriptableRegistry;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;

public class GuideBookRegistry extends ScriptableRegistry<GuideBook> {
   public static final GuideBookRegistry INSTANCE = new GuideBookRegistry();

   private GuideBookRegistry() {
      super(IReloadableRegistry.PackType.DATA_PACK, "buildcraft/book");
      this.addCustomType("", GuideBook.DESERIALISER);
   }

   @Nullable
   public GuideBook getBook(String bookName) {
      Identifier loc = Identifier.parse(bookName);
      GuideBook guideBook = this.getReloadableEntryMap().get(loc);
      if (guideBook != null) {
         return guideBook;
      }

      for (GuideBook book : this.getPermanent()) {
         if (book.name.toString().equals(bookName)) {
            return book;
         }
      }

      return null;
   }
}
