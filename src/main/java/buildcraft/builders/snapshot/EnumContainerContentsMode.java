/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

public enum EnumContainerContentsMode {
   INCLUDE("gui.buildcraft.builder.contentsmode.include"),
   IGNORE("gui.buildcraft.builder.contentsmode.ignore");

   private final String tooltipKey;

   EnumContainerContentsMode(String tooltipKey) {
      this.tooltipKey = tooltipKey;
   }

   public EnumContainerContentsMode next() {
      return values()[(this.ordinal() + 1) % values().length];
   }

   public String tooltipKey() {
      return this.tooltipKey;
   }

   public static EnumContainerContentsMode fromOrdinal(int ord) {
      return ord >= 0 && ord < values().length ? values()[ord] : INCLUDE;
   }
}
