/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.tiles.IControllable;
import buildcraft.core.statements.TriggerFluidContainer;
import buildcraft.core.statements.TriggerFluidContainerLevel;
import buildcraft.core.statements.TriggerInventory;
import buildcraft.core.statements.TriggerInventoryLevel;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import java.util.EnumMap;

public class BCCoreSprites {
   public static final SpriteHolderRegistry.SpriteHolder MARKER_VOLUME_CONNECTED = h("lasers/marker_volume_connected");
   public static final SpriteHolderRegistry.SpriteHolder MARKER_VOLUME_POSSIBLE = h("lasers/marker_volume_possible");
   public static final SpriteHolderRegistry.SpriteHolder MARKER_VOLUME_SIGNAL = h("lasers/marker_volume_signal");
   public static final SpriteHolderRegistry.SpriteHolder MARKER_PATH_CONNECTED = h("lasers/marker_path_connected");
   public static final SpriteHolderRegistry.SpriteHolder MARKER_PATH_POSSIBLE = h("lasers/marker_path_possible");
   public static final SpriteHolderRegistry.SpriteHolder MARKER_DEFAULT_POSSIBLE = h("lasers/marker_default_possible");
   public static final SpriteHolderRegistry.SpriteHolder STRIPES_READ = h("lasers/stripes_read");
   public static final SpriteHolderRegistry.SpriteHolder STRIPES_WRITE = h("lasers/stripes_write");
   public static final SpriteHolderRegistry.SpriteHolder STRIPES_WRITE_DIRECTION = h("lasers/stripes_write_direction");
   public static final SpriteHolderRegistry.SpriteHolder LASER_POWER_LOW = h("lasers/power_low");
   public static final SpriteHolderRegistry.SpriteHolder LASER_POWER_MED = h("lasers/power_med");
   public static final SpriteHolderRegistry.SpriteHolder LASER_POWER_HIGH = h("lasers/power_high");
   public static final SpriteHolderRegistry.SpriteHolder LASER_POWER_FULL = h("lasers/power_full");
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_MACHINE_ACTIVE = h("triggers/trigger_machine_active");
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_MACHINE_INACTIVE = h("triggers/trigger_machine_inactive");
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_REDSTONE_ACTIVE = new SpriteHolderRegistry.SpriteHolder("minecraft:block/redstone_torch");
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_REDSTONE_INACTIVE = new SpriteHolderRegistry.SpriteHolder("minecraft:block/redstone_torch_off");
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_TRUE = h("triggers/trigger_true");
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_POWER_HIGH = h("triggers/trigger_energy_storage_high");
   public static final SpriteHolderRegistry.SpriteHolder TRIGGER_POWER_LOW = h("triggers/trigger_energy_storage_low");
   public static final EnumMap<EnumPowerStage, SpriteHolderRegistry.SpriteHolder> TRIGGER_POWER_STAGE = new EnumMap<>(EnumPowerStage.class);
   public static final EnumMap<TriggerFluidContainer.State, SpriteHolderRegistry.SpriteHolder> TRIGGER_FLUID = new EnumMap<>(TriggerFluidContainer.State.class);
   public static final EnumMap<TriggerFluidContainerLevel.TriggerType, SpriteHolderRegistry.SpriteHolder> TRIGGER_FLUID_LEVEL = new EnumMap<>(
      TriggerFluidContainerLevel.TriggerType.class
   );
   public static final EnumMap<TriggerInventory.State, SpriteHolderRegistry.SpriteHolder> TRIGGER_INVENTORY = new EnumMap<>(TriggerInventory.State.class);
   public static final EnumMap<TriggerInventoryLevel.TriggerType, SpriteHolderRegistry.SpriteHolder> TRIGGER_INVENTORY_LEVEL = new EnumMap<>(
      TriggerInventoryLevel.TriggerType.class
   );
   public static final SpriteHolderRegistry.SpriteHolder ACTION_REDSTONE = new SpriteHolderRegistry.SpriteHolder("minecraft:block/redstone_torch");
   public static final EnumMap<IControllable.Mode, SpriteHolderRegistry.SpriteHolder> ACTION_MACHINE_CONTROL = new EnumMap<>(IControllable.Mode.class);
   public static final SpriteHolderRegistry.SpriteHolder PARAM_GATE_SIDE_ONLY = h("triggers/redstone_gate_side_only");
   public static final SpriteHolderRegistry.SpriteHolder[] PARAM_REDSTONE_LEVEL = new SpriteHolderRegistry.SpriteHolder[16];

   private static SpriteHolderRegistry.SpriteHolder h(String path) {
      return SpriteHolderRegistry.getHolder("buildcraftcore:" + path);
   }

   static {
      for (EnumPowerStage stage : EnumPowerStage.VALUES) {
         TRIGGER_POWER_STAGE.put(stage, h("triggers/trigger_engineheat_" + stage.getSerializedName()));
      }

      for (TriggerFluidContainer.State state : TriggerFluidContainer.State.VALUES) {
         TRIGGER_FLUID.put(state, h("triggers/trigger_liquidcontainer_" + state.name().toLowerCase()));
      }

      for (TriggerFluidContainerLevel.TriggerType type : TriggerFluidContainerLevel.TriggerType.VALUES) {
         TRIGGER_FLUID_LEVEL.put(type, h("triggers/trigger_liquidcontainer_" + type.name().toLowerCase()));
      }

      for (TriggerInventory.State state : TriggerInventory.State.VALUES) {
         TRIGGER_INVENTORY.put(state, h("triggers/trigger_inventory_" + state.name().toLowerCase()));
      }

      for (TriggerInventoryLevel.TriggerType type : TriggerInventoryLevel.TriggerType.VALUES) {
         TRIGGER_INVENTORY_LEVEL.put(type, h("triggers/trigger_inventory_" + type.name().toLowerCase()));
      }

      for (IControllable.Mode mode : IControllable.Mode.VALUES) {
         ACTION_MACHINE_CONTROL.put(mode, h("triggers/action_machinecontrol_" + mode.lowerCaseName));
      }

      for (int i = 0; i < 16; i++) {
         PARAM_REDSTONE_LEVEL[i] = h("triggers/parameter_redstone_" + i);
      }
   }
}
