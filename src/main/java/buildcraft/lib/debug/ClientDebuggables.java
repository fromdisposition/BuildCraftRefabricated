/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.debug;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import buildcraft.api.tiles.IDebuggable;

public class ClientDebuggables {
    public static final List<String> SERVER_LEFT = new ArrayList<>();
    public static final List<String> SERVER_RIGHT = new ArrayList<>();

    @Nullable
    public static IDebuggable getDebuggableObject(@Nullable HitResult mouseOver) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.reducedDebugInfo().get() ||
            mc.player == null ||
            mc.player.isReducedDebugInfo() ||
            !mc.gui.getDebugOverlay().showDebugScreen()) {
            return null;
        }
        if (mouseOver == null) {
            return null;
        }
        ClientLevel world = mc.level;
        if (world == null) {
            return null;
        }
        if (mouseOver instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = blockHit.getBlockPos();
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof IDebuggable) {
                return (IDebuggable) tile;
            }
        } else if (mouseOver instanceof EntityHitResult entityHit && entityHit.getType() == HitResult.Type.ENTITY) {
            Entity entity = entityHit.getEntity();
            if (entity instanceof IDebuggable) {
                return (IDebuggable) entity;
            }
        }
        return null;
    }

    @Nullable
    public static Direction getHitSide(@Nullable HitResult mouseOver) {
        if (mouseOver instanceof BlockHitResult blockHit) {
            return blockHit.getDirection();
        }
        return null;
    }
}
