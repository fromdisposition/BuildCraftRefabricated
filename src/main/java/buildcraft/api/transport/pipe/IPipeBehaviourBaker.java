/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pipe;

import java.util.List;
import net.minecraft.client.resources.model.geometry.BakedQuad;

public interface IPipeBehaviourBaker<B extends PipeBehaviour> {
   List<BakedQuad> bake(B var1);
}
