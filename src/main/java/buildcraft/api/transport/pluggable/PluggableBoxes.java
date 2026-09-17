/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pluggable;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

/**
 * Shared face-mounted pluggable collision/selection boxes, built from a width span {@code [min, max]} (across the
 * mounting face) and a thickness range for the low faces ({@code [near1, near2]}, DOWN/NORTH/WEST) and the high
 * faces ({@code [far1, far2]}, UP/SOUTH/EAST).
 */
public final class PluggableBoxes {
   /**
    * Small chip mounted against the pipe body, shared by the gate, light sensor, pulsar and timer. Matched to the
    * rendered gate geometry ({@code GateQuadGeometry}): body spans 0.125..0.2506 off the face, overlays protrude
    * 0.0125 beyond it, so the box wraps 0.1125..0.2625 -- the exact visual envelope.
    */
   public static final AABB[] CHIP = faceBoxes(0.3125, 0.6875, 0.1125, 0.2625, 0.7375, 0.8875);

   private PluggableBoxes() {
   }

   public static AABB[] faceBoxes(double min, double max, double near1, double near2, double far1, double far2) {
      AABB[] boxes = new AABB[6];
      boxes[Direction.DOWN.get3DDataValue()] = new AABB(min, near1, min, max, near2, max);
      boxes[Direction.UP.get3DDataValue()] = new AABB(min, far1, min, max, far2, max);
      boxes[Direction.NORTH.get3DDataValue()] = new AABB(min, min, near1, max, max, near2);
      boxes[Direction.SOUTH.get3DDataValue()] = new AABB(min, min, far1, max, max, far2);
      boxes[Direction.WEST.get3DDataValue()] = new AABB(near1, min, min, near2, max, max);
      boxes[Direction.EAST.get3DDataValue()] = new AABB(far1, min, min, far2, max, max);
      return boxes;
   }
}
