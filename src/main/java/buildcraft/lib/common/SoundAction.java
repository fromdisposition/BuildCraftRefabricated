/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SoundAction {
   private static final Map<String, SoundAction> ACTIONS = new ConcurrentHashMap<>();
   private final String name;

   public static SoundAction get(String name) {
      return ACTIONS.computeIfAbsent(name, SoundAction::new);
   }

   private SoundAction(String name) {
      this.name = name;
   }

   public String name() {
      return this.name;
   }

   @Override
   public String toString() {
      return "SoundAction[" + this.name + "]";
   }
}
