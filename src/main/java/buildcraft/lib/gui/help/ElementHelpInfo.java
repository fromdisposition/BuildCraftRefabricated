/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.help;

import buildcraft.lib.gui.pos.IGuiArea;

public class ElementHelpInfo {
   public final String title;
   public final int colour;
   public final String[] localeKeys;
   public final boolean isPreTranslated;

   public ElementHelpInfo(String title, int colour, String... localeKeys) {
      this.title = title;
      this.colour = colour;
      this.localeKeys = localeKeys;
      this.isPreTranslated = false;
   }

   public ElementHelpInfo(String title, int colour, boolean isPreTranslated, String... localeKeys) {
      this.title = title;
      this.colour = colour;
      this.localeKeys = localeKeys;
      this.isPreTranslated = isPreTranslated;
   }

   public static ElementHelpInfo preTranslated(String title, int colour, String... lines) {
      return new ElementHelpInfo(title, colour, true, lines);
   }

   public final ElementHelpInfo.HelpPosition target(IGuiArea target) {
      return new ElementHelpInfo.HelpPosition(this, target);
   }

   public static final class HelpPosition {
      public final ElementHelpInfo info;
      public final IGuiArea target;

      private HelpPosition(ElementHelpInfo info, IGuiArea target) {
         this.info = info;
         this.target = target;
      }
   }
}
