/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide;

import buildcraft.lib.gui.ISimpleDrawable;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class PageLine implements Comparable<PageLine> {
   public ISimpleDrawable startIcon;
   public ISimpleDrawable startIconHovered;
   public final int indent;
   public final String text;
   public final boolean link;
   @Nullable
   public final Supplier<List<String>> tooltipSupplier;

   public PageLine(int indent, String text, boolean isLink) {
      this(null, null, indent, text, isLink);
   }

   public PageLine(ISimpleDrawable startIcon, ISimpleDrawable startIconHovered, int indent, String text, boolean isLink) {
      this(startIcon, startIconHovered, indent, text, isLink, null);
   }

   public PageLine(
      ISimpleDrawable startIcon, ISimpleDrawable startIconHovered, int indent, String text, boolean link, @Nullable Supplier<List<String>> tooltipSupplier
   ) {
      if (text == null) {
         throw new NullPointerException("text");
      }

      this.startIcon = startIcon;
      this.startIconHovered = startIconHovered;
      this.indent = indent;
      this.text = text;
      this.link = link;
      this.tooltipSupplier = tooltipSupplier;
   }

   @Override
   public String toString() {
      return "PageLine [indent = " + this.indent + ", text=" + this.text + "]";
   }

   public int compareTo(PageLine o) {
      return this.text.toLowerCase().compareTo(o.text.toLowerCase());
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      result = 31 * result + this.indent;
      result = 31 * result + (this.link ? 1231 : 1237);
      result = 31 * result + (this.startIcon == null ? 0 : this.startIcon.hashCode());
      return 31 * result + (this.text == null ? 0 : this.text.hashCode());
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (this.getClass() != obj.getClass()) {
         return false;
      }

      PageLine other = (PageLine)obj;
      if (this.indent != other.indent) {
         return false;
      }

      if (this.link != other.link) {
         return false;
      }

      if (this.startIcon == null) {
         if (other.startIcon != null) {
            return false;
         }
      } else if (!this.startIcon.equals(other.startIcon)) {
         return false;
      }

      if (this.text == null) {
         if (other.text != null) {
            return false;
         }
      } else if (!this.text.equals(other.text)) {
         return false;
      }

      return true;
   }

   @Nullable
   public List<String> getTooltip() {
      return this.tooltipSupplier == null ? null : this.tooltipSupplier.get();
   }
}
