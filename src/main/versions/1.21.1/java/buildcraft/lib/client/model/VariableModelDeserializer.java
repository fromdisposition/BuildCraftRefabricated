/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model;

import net.minecraft.resources.Identifier;

/**
 * 1.21.1 (versions/1.21.1). The shared class implements the 1.21.5 Fabric {@code UnbakedModelDeserializer}
 * (for the "buildcraftlib:variable" engine models), an interface that does not exist on 1.21.1 — hence this
 * cut-down version. On 1.21.1 those expression models are kept out of vanilla's bulk model parse by
 * {@code BlockModelVariableSkipMixin} (they are only ever read by the engine BER via ModelHolderVariable), so
 * no deserializer/resolver is registered here. TYPE_ID/INSTANCE mirror the shared public surface; they are
 * referenced only by the &gt;=1.21.10 registration, which is gated out on 1.21.1.
 */
public final class VariableModelDeserializer {
   public static final Identifier TYPE_ID = Identifier.fromNamespaceAndPath("buildcraftlib", "variable");
   public static final VariableModelDeserializer INSTANCE = new VariableModelDeserializer();

   private VariableModelDeserializer() {
   }
}
