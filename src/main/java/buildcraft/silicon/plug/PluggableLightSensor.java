/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.plug;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventStatement;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;

import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.BCSiliconStatements;

public class PluggableLightSensor extends PipePluggable {

    private static final AABB[] BOXES = new AABB[6];

    static {
        double ll = 2 / 16.0;
        double lu = 4 / 16.0;
        double ul = 12 / 16.0;
        double uu = 14 / 16.0;

        double min = 5 / 16.0;
        double max = 11 / 16.0;

        BOXES[Direction.DOWN.ordinal()] = new AABB(min, ll, min, max, lu, max);
        BOXES[Direction.UP.ordinal()] = new AABB(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.ordinal()] = new AABB(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.ordinal()] = new AABB(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.ordinal()] = new AABB(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.ordinal()] = new AABB(ul, min, min, uu, max, max);
    }

    public PluggableLightSensor(PluggableDefinition definition, IPipeHolder holder, Direction side) {
        super(definition, holder, side);
    }

    @Override
    public AABB getBoundingBox() {
        return BOXES[side.ordinal()];
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public ItemStack getPickStack() {
        return new ItemStack(BCSiliconItems.PLUG_LIGHT_SENSOR.get());
    }

    @Override
    public void onPlacedBy(Player player) {
        super.onPlacedBy(player);
        buildcraft.transport.BCTransportAttachments.recordPluggablePlacement(
            player, buildcraft.transport.BCTransportAttachments.PluggablesPlaced.Kind.LIGHT_SENSOR);
    }

    @PipeEventHandler
    public void addInternalTriggers(PipeEventStatement.AddTriggerInternalSided event) {
        if (event.side == this.side) {
            event.triggers.add(BCSiliconStatements.TRIGGER_LIGHT_LOW);
            event.triggers.add(BCSiliconStatements.TRIGGER_LIGHT_HIGH);
        }
    }

    @Override
    public buildcraft.silicon.client.model.key.KeyPlugSimple getModelRenderKey(Object layer) {
        if (layer == null) return null;
        String name = layer.toString().toLowerCase();
        if (name.contains("cutout")) {
            return new buildcraft.silicon.client.model.key.KeyPlugSimple("daylight_sensor", false, layer, this.side);
        }
        return null;
    }
}
