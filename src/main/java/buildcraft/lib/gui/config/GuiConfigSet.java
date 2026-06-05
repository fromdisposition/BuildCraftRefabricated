/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.gui.config;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import buildcraft.api.core.BCLog;

class GuiConfigSet {
    final Map<String, GuiPropertyBoolean> properties = new TreeMap<>();

    GuiPropertyBoolean getOrAddBoolean(String name, boolean defaultValue) {
        return properties.computeIfAbsent(name, n -> new GuiPropertyBoolean(n, defaultValue));
    }

    JsonObject writeToJson() {
        JsonObject json = new JsonObject();
        for (Entry<String, GuiPropertyBoolean> entry : properties.entrySet()) {
            json.add(entry.getKey(), entry.getValue().writeToJson());
        }
        return json;
    }

    void readFromJson(JsonObject json) {
        for (Entry<String, JsonElement> entry : json.entrySet()) {
            String name = entry.getKey();
            GuiPropertyBoolean prop = properties.get(name);
            if (prop == null) {
                prop = new GuiPropertyBoolean(name, false);
                properties.put(name, prop);
            }
            JsonElement elem = entry.getValue();
            if (!elem.isJsonPrimitive() || !elem.getAsJsonPrimitive().isBoolean()) {
                BCLog.logger.warn("[lib.gui.config] Non-boolean entry for '" + name + "', skipping");
                continue;
            }
            prop.readFromJson(elem);
        }
    }
}
