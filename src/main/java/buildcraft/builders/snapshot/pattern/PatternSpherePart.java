/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot.pattern;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPatternShape;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.builders.BCBuildersSprites;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterFacing;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterHollow;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterRotation;
import buildcraft.lib.misc.VecUtil;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public final class PatternSpherePart extends Pattern implements IFillerPatternShape {
   private final PatternSpherePart.SpherePartType type;

   public PatternSpherePart(PatternSpherePart.SpherePartType type) {
      super("sphere_" + type.lowerCaseName);
      this.type = type;
   }

   @Override
   public int minParameters() {
      return this.type.openFaces == 1 ? 2 : 3;
   }

   @Override
   public int maxParameters() {
      return this.minParameters();
   }

   @Override
   public IStatementParameter createParameter(int index) {
      if (index >= this.minParameters()) {
         return null;
      }

      switch (index) {
         case 0:
            return PatternParameterHollow.FILLED_INNER;
         case 1:
            return PatternParameterFacing.DOWN;
         case 2:
            return PatternParameterRotation.NONE;
         default:
            return null;
      }
   }

   @Override
   public ISprite getSprite() {
      return BCBuildersSprites.FILLER_SPHERE_PART.get(this.type);
   }

   @Override
   public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
      PatternParameterFacing facing = getParam(1, params, PatternParameterFacing.DOWN);
      PatternParameterRotation rotation = getParam(2, params, PatternParameterRotation.NONE);
      PatternParameterHollow hollow = getParam(0, params, PatternParameterHollow.FILLED_INNER);
      Set<Direction> innerSides = EnumSet.noneOf(Direction.class);
      Vec3 max = new Vec3(filledTemplate.getMax().getX(), filledTemplate.getMax().getY(), filledTemplate.getMax().getZ());
      Vec3 center = VecUtil.scale(max, 0.5);
      Vec3 radius = center.add(0.5, 0.5, 0.5);
      innerSides.add(facing.face);
      Axis axis = facing.face.getAxis();
      Vec3 offset = VecUtil.offset(Vec3.ZERO, facing.face, VecUtil.getValue(radius, axis));
      center = center.add(offset);
      radius = VecUtil.replaceValue(radius, axis, VecUtil.getValue(radius, axis) * 2.0);
      if (this.type.openFaces > 1) {
         Axis secondaryAxis;
         if (rotation.rotationCount % 2 == 1) {
            secondaryAxis = axis == Axis.X ? Axis.Y : (axis == Axis.Y ? Axis.Z : Axis.X);
         } else {
            secondaryAxis = axis == Axis.X ? Axis.Z : (axis == Axis.Y ? Axis.X : Axis.Y);
         }

         Direction secondaryFace = VecUtil.getFacing(secondaryAxis, rotation.rotationCount >= 2);
         innerSides.add(secondaryFace);
         offset = VecUtil.offset(Vec3.ZERO, secondaryFace, VecUtil.getValue(radius, secondaryAxis));
         center = center.add(offset);
         radius = VecUtil.replaceValue(radius, secondaryAxis, VecUtil.getValue(radius, secondaryAxis) * 2.0);
         if (this.type.openFaces > 2) {
            int rotationCount = rotation.rotationCount + 1 & 3;
            Axis tertiaryAxis;
            if (rotationCount % 2 == 1) {
               tertiaryAxis = axis == Axis.X ? Axis.Y : (axis == Axis.Y ? Axis.Z : Axis.X);
            } else {
               tertiaryAxis = axis == Axis.X ? Axis.Z : (axis == Axis.Y ? Axis.X : Axis.Y);
            }

            Direction tertiaryFace = VecUtil.getFacing(tertiaryAxis, rotationCount >= 2);
            innerSides.add(tertiaryFace);
            offset = VecUtil.offset(Vec3.ZERO, tertiaryFace, VecUtil.getValue(radius, tertiaryAxis));
            center = center.add(offset);
            radius = VecUtil.replaceValue(radius, tertiaryAxis, VecUtil.getValue(radius, tertiaryAxis) * 2.0);
         }
      }

      fillEllipsoid(filledTemplate, center, radius, hollow, innerSides);
      return true;
   }

   public enum SpherePartType {
      EIGHTH(3),
      QUARTER(2),
      HALF(1);

      public final String lowerCaseName = this.name().toLowerCase(Locale.ROOT);
      final int openFaces;

      SpherePartType(int numOpenFaces) {
         this.openFaces = numOpenFaces;
      }
   }
}
