/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.Identifier;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableCreator;
import buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableNbtReader;
import buildcraft.api.transport.pluggable.PluggableDefinition.IPluggableNetLoader;

import buildcraft.transport.pipe.PipeEventBus;

import buildcraft.silicon.plug.FilterEventHandler;
import buildcraft.silicon.plug.PluggableFacade;
import buildcraft.silicon.plug.PluggableGate;
import buildcraft.silicon.plug.PluggableLens;
import buildcraft.silicon.plug.PluggableLightSensor;
import buildcraft.silicon.plug.PluggablePulsar;
import buildcraft.silicon.plug.PluggableTimer;

public class BCSiliconPlugs {
    private static final Logger LOGGER = LoggerFactory.getLogger("BuildCraft");

    public static PluggableDefinition facade;
    public static PluggableDefinition gate;
    public static PluggableDefinition pulsar;
    public static PluggableDefinition lens;
    public static PluggableDefinition lightSensor;
    public static PluggableDefinition timer;

    private static final List<PluggableDefinition> PENDING = new ArrayList<>();

    public static void preInit() {
        facade = create("facade",
            (IPluggableNbtReader) PluggableFacade::new,
            (IPluggableNetLoader) PluggableFacade::new);
        gate = create("gate",
            (IPluggableNbtReader) PluggableGate::new,
            (IPluggableNetLoader) PluggableGate::new);
        pulsar = create("pulsar",
            (IPluggableNbtReader) PluggablePulsar::new,
            (IPluggableNetLoader) PluggablePulsar::new);
        lens = create("lens",
            (IPluggableNbtReader) PluggableLens::new,
            (IPluggableNetLoader) PluggableLens::new);
        lightSensor = createSimple("light_sensor",
            PluggableLightSensor::new);
        timer = createSimple("timer",
            PluggableTimer::new);

        PipeEventBus.registerGlobalHandler(FilterEventHandler.class);
    }

    public static void registerAll() {
        if (PipeApi.pluggableRegistry == null) {
            LOGGER.error("[silicon.plugs] PipeApi.pluggableRegistry is null at registerAll! "
                + "Pluggables (facades, gates, etc.) will NOT be saved or loaded.");
            return;
        }
        for (PluggableDefinition def : PENDING) {
            PipeApi.pluggableRegistry.register(def);
        }
        LOGGER.info("[silicon.plugs] Registered {} pluggable definitions", PENDING.size());
        PENDING.clear();
    }

    private static PluggableDefinition create(String name, IPluggableNbtReader reader, IPluggableNetLoader loader) {
        PluggableDefinition def = new PluggableDefinition(idFor(name), reader, loader);
        PENDING.add(def);
        return def;
    }

    private static PluggableDefinition createSimple(String name, IPluggableCreator creator) {
        PluggableDefinition def = new PluggableDefinition(idFor(name), creator);
        PENDING.add(def);
        return def;
    }

    private static Identifier idFor(String name) {
        return Identifier.fromNamespaceAndPath(BCSilicon.MODID, name);
    }
}
