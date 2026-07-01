/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.client.render;

import buildcraft.robotics.entity.EntityRobot;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;

public class RobotRenderState extends EntityRenderState {
   public Identifier texture = EntityRobot.DEFAULT_TEXTURE;
   public float energy;
   public float aimYaw;
   public final ItemStackRenderState heldItemState = new ItemStackRenderState();
}
