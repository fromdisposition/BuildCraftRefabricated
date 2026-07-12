/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fabric.mixin.client;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 1.21.1 stub (versions/1.21.1). The shared mixin injects BuildCraft's picture-in-picture renderers
 * (blueprint/zone-map GUI previews) into vanilla's PIP renderer list — a 1.21.5+ subsystem that does
 * not exist on 1.21.1. Here it is an empty no-op mixin so it stays valid in buildcraft.mixins.json
 * without injecting anything; the in-GUI 3D blueprint/zone-map previews are simply unavailable.
 */
@Mixin(GameRenderer.class)
public class GameRendererPipMixin {
}
