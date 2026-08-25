/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.client.render;

import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;

/** Display transforms for the robot item, taken from vanilla's block values. */
public final class RobotDisplay {
   private RobotDisplay() {
   }

   public static Vector3f rotation(ItemDisplayContext context) {
      return switch (context) {
         case GUI -> new Vector3f(30.0F, 225.0F, 0.0F);
         case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> new Vector3f(75.0F, 45.0F, 0.0F);
         case FIRST_PERSON_RIGHT_HAND -> new Vector3f(0.0F, 45.0F, 0.0F);
         case FIRST_PERSON_LEFT_HAND -> new Vector3f(0.0F, 225.0F, 0.0F);
         case HEAD -> new Vector3f(0.0F, 180.0F, 0.0F);
         default -> new Vector3f();
      };
   }

   public static float scale(ItemDisplayContext context) {
      return switch (context) {
         case GUI -> 0.8F;
         case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> 0.75F;
         case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> 0.8F;
         case GROUND -> 0.5F;
         default -> 1.0F;
      };
   }
}
