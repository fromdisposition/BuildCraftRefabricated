/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("ClassCanBeRecord")
public final class SoundAction {
    private static final Map<String, SoundAction> ACTIONS = new ConcurrentHashMap<>();

    public static SoundAction get(String name) {
        return ACTIONS.computeIfAbsent(name, SoundAction::new);
    }

    private final String name;

    private SoundAction(final String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    @Override
    public String toString() {
        return "SoundAction[" + this.name + "]";
    }
}
