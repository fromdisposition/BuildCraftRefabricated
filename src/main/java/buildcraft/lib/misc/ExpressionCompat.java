/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.tiles.IControllable;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.api.NodeTypes;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.DyeColor;

public class ExpressionCompat {
   public static final FunctionContext RENDERING = DefaultContexts.RENDERING;
   public static final NodeType<Axis> ENUM_AXIS = new NodeType<>("Axis", Axis.X);
   public static final NodeType<Direction> ENUM_FACING;
   public static final NodeType<DyeColor> ENUM_DYE_COLOUR;
   public static final NodeType<EnumPowerStage> ENUM_POWER_STAGE;
   public static final NodeType<IControllable.Mode> ENUM_CONTROL_MODE;

   static {
      NodeTypes.addType("Axis", ENUM_AXIS);

      for (Axis a : Axis.values()) {
         ENUM_AXIS.putConstant(a + "", a);
      }

      ENUM_FACING = new NodeType<>("Facing", Direction.UP);
      NodeTypes.addType("Facing", ENUM_FACING);
      ENUM_FACING.put_t_t("getOpposite", Direction::getOpposite);
      ENUM_FACING.put_t_o("getAxis", Axis.class, Direction::getAxis);
      ENUM_FACING.put_t_o("(string)", String.class, Direction::getName);

      for (Direction f : Direction.values()) {
         ENUM_FACING.putConstant(f + "", f);
      }

      ENUM_DYE_COLOUR = new NodeType<>("Dye Colour", DyeColor.WHITE);
      NodeTypes.addType("DyeColor", ENUM_DYE_COLOUR);
      NodeTypes.addType("DyeColour", ENUM_DYE_COLOUR);
      ENUM_DYE_COLOUR.put_t_o("(string)", String.class, c -> c.getName());

      for (DyeColor c : DyeColor.values()) {
         ENUM_DYE_COLOUR.putConstant(c + "", c);
      }

      ENUM_POWER_STAGE = new NodeType<>("Engine Power Stage", EnumPowerStage.BLUE);
      NodeTypes.addType("EnginePowerStage", ENUM_POWER_STAGE);
      ENUM_POWER_STAGE.put_t_o("(string)", String.class, s -> s.name().toLowerCase());

      for (EnumPowerStage stage : EnumPowerStage.VALUES) {
         ENUM_POWER_STAGE.putConstant(stage + "", stage);
      }

      ENUM_CONTROL_MODE = new NodeType<>("Controllable Mode", IControllable.Mode.class, IControllable.Mode.ON);
      NodeTypes.addType("ControlMode", ENUM_CONTROL_MODE);
      ENUM_CONTROL_MODE.put_t_o("(string)", String.class, e -> e.lowerCaseName);

      for (IControllable.Mode mode : IControllable.Mode.VALUES) {
         ENUM_CONTROL_MODE.putConstant(mode + "", mode);
      }
   }
}
