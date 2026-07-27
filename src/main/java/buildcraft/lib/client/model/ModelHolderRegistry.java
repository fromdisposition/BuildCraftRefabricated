/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.Identifier;

public class ModelHolderRegistry {
   public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.model.holder");
   static final List<ModelHolder> HOLDERS = new ArrayList<>();

   public static void onTextureStitchPre(Set<Identifier> toRegisterSprites) {
      for (ModelHolder holder : HOLDERS) {
         holder.onTextureStitchPre(toRegisterSprites);
      }
   }

   public static void onModelBake() {
      for (ModelHolder holder : HOLDERS) {
         holder.onModelBake();
      }

      if (DEBUG) {
         BCLog.logger.info("[lib.model.holder] List of registered Models:");
         List<ModelHolder> holders = new ArrayList<>(HOLDERS);
         holders.sort(Comparator.comparing(a -> a.modelLocation.toString()));

         for (ModelHolder holder : holders) {
            String status = "  ";
            if (holder.failReason != null) {
               status = status + "(" + holder.failReason + ")";
            } else if (!holder.hasBakedQuads()) {
               status = status + "(Model was registered too late)";
            }

            BCLog.logger.info("  - " + holder.modelLocation + status);
         }

         BCLog.logger.info("[lib.model.holder] Total of " + HOLDERS.size() + " models");
      }
   }
}
