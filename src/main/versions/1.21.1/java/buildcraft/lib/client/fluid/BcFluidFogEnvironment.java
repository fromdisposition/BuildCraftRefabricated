/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.fluid;

/**
 * 1.21.1 stub (versions/1.21.1). The shared class extends the 1.21.5 vanilla FogEnvironment
 * (net.minecraft.client.renderer.fog.environment.FogEnvironment) to tint underwater fog for BC fluids; that fog
 * environment system does not exist on 1.21.1. Custom BC fluid fog is disabled there (vanilla fog still applies);
 * referenced only by the (no-op on 1.21.1) FogRendererBcFluidMixin.
 */
public final class BcFluidFogEnvironment {
   private BcFluidFogEnvironment() {
   }
}
