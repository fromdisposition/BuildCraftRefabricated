/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders;

import buildcraft.builders.snapshot.pattern.PatternSpherePart;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterCenter;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

public class BCBuildersSprites {
   public static final SpriteHolderRegistry.SpriteHolder FILLER_PLANNER = getHolder("addons/filler_planner");
   public static final SpriteHolderRegistry.SpriteHolder ROBOT = getHolder("block/robot");
   public static final SpriteHolderRegistry.SpriteHolder FILLER_NONE = getHolder("filler/patterns/none");
   public static final SpriteHolderRegistry.SpriteHolder FILLER_CLEAR = getHolder("filler/patterns/clear");
   public static final SpriteHolderRegistry.SpriteHolder FILLER_FILL = getHolder("filler/patterns/fill");
   public static final SpriteHolderRegistry.SpriteHolder FILLER_BOX = getHolder("filler/patterns/box");
   public static final SpriteHolderRegistry.SpriteHolder FILLER_FRAME = getHolder("filler/patterns/frame");
   public static final SpriteHolderRegistry.SpriteHolder FILLER_FLATTEN = getHolder("filler/patterns/flatten");
   public static final SpriteHolderRegistry.SpriteHolder FILLER_HORIZON = getHolder("filler/patterns/horizon");
   public static final SpriteHolderRegistry.SpriteHolder FILLER_CYLINDER = getHolder("filler/patterns/cylinder");
   public static final SpriteHolderRegistry.SpriteHolder FILLER_PYRAMID = getHolder("filler/patterns/pyramid");
   public static final SpriteHolderRegistry.SpriteHolder FILLER_STAIRS = getHolder("filler/patterns/stairs");
   public static final SpriteHolderRegistry.SpriteHolder FILLER_SPHERE = getHolder("filler/patterns/sphere");
   public static final SpriteHolderRegistry.SpriteHolder FILLER_2D_TRIANGLE = getHolder("filler/patterns/2d_triangle");
   public static final SpriteHolderRegistry.SpriteHolder FILLER_2D_SQUARE = getHolder("filler/patterns/2d_square");
   public static final SpriteHolderRegistry.SpriteHolder FILLER_2D_PENTAGON = getHolder("filler/patterns/2d_pentagon");
   public static final SpriteHolderRegistry.SpriteHolder FILLER_2D_HEXAGON = getHolder("filler/patterns/2d_hexagon");
   public static final SpriteHolderRegistry.SpriteHolder FILLER_2D_OCTAGON = getHolder("filler/patterns/2d_octagon");
   public static final SpriteHolderRegistry.SpriteHolder FILLER_2D_CIRCLE = getHolder("filler/patterns/2d_circle");
   public static final SpriteHolderRegistry.SpriteHolder FILLER_2D_SEMI_CIRCLE = getHolder("filler/patterns/2d_semi_circle");
   public static final SpriteHolderRegistry.SpriteHolder FILLER_2D_ARC = getHolder("filler/patterns/2d_arc");
   public static final SpriteHolderRegistry.SpriteHolder PARAM_HOLLOW = getHolder("filler/parameters/hollow");
   public static final SpriteHolderRegistry.SpriteHolder PARAM_FILLED_INNER = getHolder("filler/parameters/filled_inner");
   public static final SpriteHolderRegistry.SpriteHolder PARAM_FILLED_OUTER = getHolder("filler/parameters/filled_outer");
   public static final SpriteHolderRegistry.SpriteHolder PARAM_STAIRS_DOWN = getHolder("filler/parameters/stairs_descend");
   public static final SpriteHolderRegistry.SpriteHolder PARAM_STAIRS_UP = getHolder("filler/parameters/stairs_ascend");
   public static final SpriteHolderRegistry.SpriteHolder[] PARAM_ROTATION = new SpriteHolderRegistry.SpriteHolder[4];
   public static final Map<Direction, SpriteHolderRegistry.SpriteHolder> PARAM_XZ_DIR;
   public static final Map<PatternParameterCenter, SpriteHolderRegistry.SpriteHolder> PARAM_CENTER;
   public static final Map<Axis, SpriteHolderRegistry.SpriteHolder> PARAM_AXIS;
   public static final Map<Direction, SpriteHolderRegistry.SpriteHolder> PARAM_FACE;
   public static final Map<PatternSpherePart.SpherePartType, SpriteHolderRegistry.SpriteHolder> FILLER_SPHERE_PART;

   private static SpriteHolderRegistry.SpriteHolder getHolder(String suffix) {
      return SpriteHolderRegistry.getHolder("buildcraftbuilders:" + suffix);
   }

   public static void fmlPreInit() {
   }

   static {
      for (int r = 0; r < 4; r++) {
         PARAM_ROTATION[r] = getHolder("filler/parameters/rotation_" + r);
      }

      PARAM_XZ_DIR = new EnumMap<>(Direction.class);
      PARAM_XZ_DIR.put(Direction.WEST, getHolder("filler/parameters/arrow_left"));
      PARAM_XZ_DIR.put(Direction.EAST, getHolder("filler/parameters/arrow_right"));
      PARAM_XZ_DIR.put(Direction.NORTH, getHolder("filler/parameters/arrow_up"));
      PARAM_XZ_DIR.put(Direction.SOUTH, getHolder("filler/parameters/arrow_down"));
      PARAM_CENTER = new EnumMap<>(PatternParameterCenter.class);

      for (PatternParameterCenter param : PatternParameterCenter.values()) {
         PARAM_CENTER.put(param, getHolder("filler/parameters/center_" + param.ordinal()));
      }

      PARAM_AXIS = new EnumMap<>(Axis.class);

      for (Axis axis : Axis.values()) {
         PARAM_AXIS.put(axis, getHolder("filler/parameters/axis_" + axis.getSerializedName()));
      }

      PARAM_FACE = new EnumMap<>(Direction.class);

      for (Direction face : Direction.values()) {
         PARAM_FACE.put(face, getHolder("filler/parameters/face_" + face.getSerializedName()));
      }

      FILLER_SPHERE_PART = new EnumMap<>(PatternSpherePart.SpherePartType.class);

      for (PatternSpherePart.SpherePartType type : PatternSpherePart.SpherePartType.values()) {
         FILLER_SPHERE_PART.put(type, getHolder("filler/patterns/sphere_" + type.lowerCaseName));
      }
   }
}
