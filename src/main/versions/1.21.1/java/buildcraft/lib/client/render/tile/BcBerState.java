/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.tile;

import net.minecraft.world.level.block.entity.BlockEntity;

/** 1.21.1 stub (versions/1.21.1): plain data holder; the modern base extends 1.21.5 BlockEntityRenderState. */
public class BcBerState<T extends BlockEntity> {
   public T tile;
   public float partialTick;
   public int light;
}
