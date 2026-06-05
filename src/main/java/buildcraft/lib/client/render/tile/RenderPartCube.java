/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.core.Direction;

import buildcraft.lib.client.model.MutableVertex;

public class RenderPartCube {

    public final MutableVertex center = new MutableVertex();
    public double sizeX = 1 / 16.0, sizeY = 1 / 16.0, sizeZ = 1 / 16.0;

    public RenderPartCube() {
        this(1 / 16.0, 1 / 16.0, 1 / 16.0);
    }

    public RenderPartCube(double x, double y, double z) {
        center.positiond(x, y, z);
    }

    public void render(PoseStack.Pose pose, VertexConsumer consumer) {
        render(pose, consumer, null);
    }

    public void render(PoseStack.Pose pose, VertexConsumer consumer, Direction skipFace) {
        float x = center.position_x;
        float y = center.position_y;
        float z = center.position_z;

        float rX = (float) (sizeX / 2);
        float rY = (float) (sizeY / 2);
        float rZ = (float) (sizeZ / 2);

        int r = center.colour_r;
        int g = center.colour_g;
        int b = center.colour_b;
        int a = center.colour_a;

        if (skipFace != Direction.UP) {
            emit(pose, consumer, x - rX, y + rY, z + rZ, r, g, b, a);
            emit(pose, consumer, x + rX, y + rY, z + rZ, r, g, b, a);
            emit(pose, consumer, x + rX, y + rY, z - rZ, r, g, b, a);
            emit(pose, consumer, x - rX, y + rY, z - rZ, r, g, b, a);
        }

        if (skipFace != Direction.DOWN) {
            emit(pose, consumer, x - rX, y - rY, z - rZ, r, g, b, a);
            emit(pose, consumer, x + rX, y - rY, z - rZ, r, g, b, a);
            emit(pose, consumer, x + rX, y - rY, z + rZ, r, g, b, a);
            emit(pose, consumer, x - rX, y - rY, z + rZ, r, g, b, a);
        }

        if (skipFace != Direction.WEST) {
            emit(pose, consumer, x - rX, y - rY, z + rZ, r, g, b, a);
            emit(pose, consumer, x - rX, y + rY, z + rZ, r, g, b, a);
            emit(pose, consumer, x - rX, y + rY, z - rZ, r, g, b, a);
            emit(pose, consumer, x - rX, y - rY, z - rZ, r, g, b, a);
        }

        if (skipFace != Direction.EAST) {
            emit(pose, consumer, x + rX, y - rY, z - rZ, r, g, b, a);
            emit(pose, consumer, x + rX, y + rY, z - rZ, r, g, b, a);
            emit(pose, consumer, x + rX, y + rY, z + rZ, r, g, b, a);
            emit(pose, consumer, x + rX, y - rY, z + rZ, r, g, b, a);
        }

        if (skipFace != Direction.NORTH) {
            emit(pose, consumer, x - rX, y - rY, z - rZ, r, g, b, a);
            emit(pose, consumer, x - rX, y + rY, z - rZ, r, g, b, a);
            emit(pose, consumer, x + rX, y + rY, z - rZ, r, g, b, a);
            emit(pose, consumer, x + rX, y - rY, z - rZ, r, g, b, a);
        }

        if (skipFace != Direction.SOUTH) {
            emit(pose, consumer, x + rX, y - rY, z + rZ, r, g, b, a);
            emit(pose, consumer, x + rX, y + rY, z + rZ, r, g, b, a);
            emit(pose, consumer, x - rX, y + rY, z + rZ, r, g, b, a);
            emit(pose, consumer, x - rX, y - rY, z + rZ, r, g, b, a);
        }
    }

    private static void emit(PoseStack.Pose pose, VertexConsumer consumer,
                             float x, float y, float z,
                             int r, int g, int b, int a) {
        consumer.addVertex(pose, x, y, z).setColor(r, g, b, a);
    }
}
