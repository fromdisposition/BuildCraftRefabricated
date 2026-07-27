/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.elem;

import buildcraft.lib.misc.LocaleUtil;
import com.google.common.collect.ForwardingList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

public class ToolTip extends ForwardingList<String> implements RandomAccess {
   private final List<String> delegate = new ArrayList<>();
   private final long delay;
   private long mouseOverStart;

   public static ToolTip createLocalized(String... localeKeys) {
      List<String> allLines = new ArrayList<>();

      for (String key : localeKeys) {
         String localized = LocaleUtil.localize(key);
         Collections.addAll(allLines, localized.split("\n"));
      }

      return new ToolTip(allLines);
   }

   public ToolTip(String... lines) {
      this.delay = 0L;
      Collections.addAll(this.delegate, lines);
   }

   public ToolTip(int delay, String... lines) {
      this.delay = delay;
      Collections.addAll(this.delegate, lines);
   }

   public ToolTip(List<String> lines) {
      this.delay = 0L;
      this.delegate.addAll(lines);
   }

   protected final List<String> delegate() {
      return this.delegate;
   }

   public void onTick(boolean mouseOver) {
      if (this.delay != 0L) {
         if (mouseOver) {
            if (this.mouseOverStart == 0L) {
               this.mouseOverStart = System.currentTimeMillis();
            }
         } else {
            this.mouseOverStart = 0L;
         }
      }
   }

   public boolean isReady() {
      if (this.delay == 0L) {
         return true;
      } else {
         return this.mouseOverStart == 0L ? false : System.currentTimeMillis() - this.mouseOverStart >= this.delay;
      }
   }

   public void refresh() {
   }
}
