/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.tile;

//? if >= 1.21.10 {
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
//?}
import net.minecraft.world.level.block.entity.BlockEntity;

//? if >= 1.21.10 {
public class BcBerState<T extends BlockEntity> extends BlockEntityRenderState {
//?} else {
/*public class BcBerState<T extends BlockEntity> {
*///?}
   public T tile;
   public float partialTick;
   public int light;
}
