/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.properties;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumLaserTableType;
import buildcraft.api.enums.EnumMachineState;
import buildcraft.api.enums.EnumOptionalSnapshotType;
import buildcraft.api.enums.EnumPowerStage;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public final class BuildCraftProperties {
   public static final Property<Direction> BLOCK_FACING = EnumProperty.create("facing", Direction.class, d -> d.getAxis().isHorizontal());
   public static final Property<Direction> BLOCK_FACING_6 = EnumProperty.create("facing", Direction.class);
   public static final Property<DyeColor> BLOCK_COLOR = EnumProperty.create("color", DyeColor.class);
   public static final Property<EnumEngineType> ENGINE_TYPE = EnumProperty.create("type", EnumEngineType.class);
   public static final Property<EnumLaserTableType> LASER_TABLE_TYPE = EnumProperty.create("type", EnumLaserTableType.class);
   public static final Property<EnumMachineState> MACHINE_STATE = EnumProperty.create("state", EnumMachineState.class);
   public static final Property<EnumPowerStage> ENERGY_STAGE = EnumProperty.create("stage", EnumPowerStage.class);
   public static final Property<EnumOptionalSnapshotType> SNAPSHOT_TYPE = EnumProperty.create("snapshot_type", EnumOptionalSnapshotType.class);
   public static final Property<Integer> GENERIC_PIPE_DATA = IntegerProperty.create("pipe_data", 0, 15);
   public static final Property<Integer> LED_POWER = IntegerProperty.create("led_power", 0, 3);
   public static final Property<Boolean> JOINED_BELOW = BooleanProperty.create("joined_below");
   public static final Property<Boolean> MOVING = BooleanProperty.create("moving");
   public static final Property<Boolean> LED_DONE = BooleanProperty.create("led_done");
   public static final Property<Boolean> ACTIVE = BooleanProperty.create("active");
   public static final Property<Boolean> VALID = BooleanProperty.create("valid");
   public static final Property<Boolean> CONNECTED_UP = BooleanProperty.create("connected_up");
   public static final Property<Boolean> CONNECTED_DOWN = BooleanProperty.create("connected_down");
   public static final Property<Boolean> CONNECTED_EAST = BooleanProperty.create("connected_east");
   public static final Property<Boolean> CONNECTED_WEST = BooleanProperty.create("connected_west");
   public static final Property<Boolean> CONNECTED_NORTH = BooleanProperty.create("connected_north");
   public static final Property<Boolean> CONNECTED_SOUTH = BooleanProperty.create("connected_south");
   public static final Map<Direction, Property<Boolean>> CONNECTED_MAP;
   public static final int UPDATE_NONE = 0;
   public static final int UPDATE_NEIGHBOURS = 1;
   public static final int MARK_BLOCK_FOR_UPDATE = 2;
   public static final int UPDATE_EVEN_CLIENT = 6;
   public static final int MARK_THIS_AND_NEIGHBOURS = 3;
   public static final int UPDATE_ALL = 9;

   private BuildCraftProperties() {
   }

   static {
      Map<Direction, Property<Boolean>> map = Maps.newEnumMap(Direction.class);
      map.put(Direction.DOWN, CONNECTED_DOWN);
      map.put(Direction.UP, CONNECTED_UP);
      map.put(Direction.EAST, CONNECTED_EAST);
      map.put(Direction.WEST, CONNECTED_WEST);
      map.put(Direction.NORTH, CONNECTED_NORTH);
      map.put(Direction.SOUTH, CONNECTED_SOUTH);
      CONNECTED_MAP = Maps.immutableEnumMap(map);
   }
}
