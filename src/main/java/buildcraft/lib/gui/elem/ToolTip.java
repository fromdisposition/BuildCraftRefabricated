/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.elem;

import com.google.common.collect.ForwardingList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

public class ToolTip extends ForwardingList<String> implements RandomAccess {
   private final List<String> delegate = new ArrayList<>();
   private final long delay;
   private long mouseOverStart;

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

   @Override
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

   public void refresh() {
   }
}
