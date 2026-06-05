/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.core;

import java.util.EnumMap;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.tiles.IControllable.Mode;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import buildcraft.core.statements.TriggerFluidContainer;
import buildcraft.core.statements.TriggerFluidContainerLevel;
import buildcraft.core.statements.TriggerInventory;
import buildcraft.core.statements.TriggerInventoryLevel;

public class BCCoreSprites {

    public static final SpriteHolder MARKER_VOLUME_CONNECTED = h("lasers/marker_volume_connected");
    public static final SpriteHolder MARKER_VOLUME_POSSIBLE = h("lasers/marker_volume_possible");
    public static final SpriteHolder MARKER_VOLUME_SIGNAL = h("lasers/marker_volume_signal");
    public static final SpriteHolder MARKER_PATH_CONNECTED = h("lasers/marker_path_connected");
    public static final SpriteHolder MARKER_PATH_POSSIBLE = h("lasers/marker_path_possible");
    public static final SpriteHolder MARKER_DEFAULT_POSSIBLE = h("lasers/marker_default_possible");

    public static final SpriteHolder STRIPES_READ = h("lasers/stripes_read");
    public static final SpriteHolder STRIPES_WRITE = h("lasers/stripes_write");
    public static final SpriteHolder STRIPES_WRITE_DIRECTION = h("lasers/stripes_write_direction");

    public static final SpriteHolder LASER_POWER_LOW = h("lasers/power_low");
    public static final SpriteHolder LASER_POWER_MED = h("lasers/power_med");
    public static final SpriteHolder LASER_POWER_HIGH = h("lasers/power_high");
    public static final SpriteHolder LASER_POWER_FULL = h("lasers/power_full");

    public static final SpriteHolder TRIGGER_MACHINE_ACTIVE = h("triggers/trigger_machine_active");
    public static final SpriteHolder TRIGGER_MACHINE_INACTIVE = h("triggers/trigger_machine_inactive");
    public static final SpriteHolder TRIGGER_REDSTONE_ACTIVE = new SpriteHolder("minecraft:block/redstone_torch");
    public static final SpriteHolder TRIGGER_REDSTONE_INACTIVE = new SpriteHolder("minecraft:block/redstone_torch_off");
    public static final SpriteHolder TRIGGER_TRUE = h("triggers/trigger_true");
    public static final SpriteHolder TRIGGER_POWER_HIGH = h("triggers/trigger_energy_storage_high");
    public static final SpriteHolder TRIGGER_POWER_LOW = h("triggers/trigger_energy_storage_low");

    public static final EnumMap<EnumPowerStage, SpriteHolder> TRIGGER_POWER_STAGE = new EnumMap<>(EnumPowerStage.class);

    public static final EnumMap<TriggerFluidContainer.State, SpriteHolder> TRIGGER_FLUID = new EnumMap<>(TriggerFluidContainer.State.class);
    public static final EnumMap<TriggerFluidContainerLevel.TriggerType, SpriteHolder> TRIGGER_FLUID_LEVEL = new EnumMap<>(TriggerFluidContainerLevel.TriggerType.class);

    public static final EnumMap<TriggerInventory.State, SpriteHolder> TRIGGER_INVENTORY = new EnumMap<>(TriggerInventory.State.class);
    public static final EnumMap<TriggerInventoryLevel.TriggerType, SpriteHolder> TRIGGER_INVENTORY_LEVEL = new EnumMap<>(TriggerInventoryLevel.TriggerType.class);

    public static final SpriteHolder ACTION_REDSTONE = new SpriteHolder("minecraft:block/redstone_torch");
    public static final EnumMap<Mode, SpriteHolder> ACTION_MACHINE_CONTROL = new EnumMap<>(Mode.class);

    public static final SpriteHolder PARAM_GATE_SIDE_ONLY = h("triggers/redstone_gate_side_only");
    public static final SpriteHolder[] PARAM_REDSTONE_LEVEL = new SpriteHolder[16];

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
        for (Mode mode : Mode.VALUES) {
            ACTION_MACHINE_CONTROL.put(mode, h("triggers/action_machinecontrol_" + mode.lowerCaseName));
        }
        for (int i = 0; i < 16; i++) {
            PARAM_REDSTONE_LEVEL[i] = h("triggers/parameter_redstone_" + i);
        }
    }

    private static SpriteHolder h(String path) {
        return SpriteHolderRegistry.getHolder("buildcraftcore:" + path);
    }
}
