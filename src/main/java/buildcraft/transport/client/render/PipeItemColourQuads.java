/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import org.joml.Vector3f;

import net.minecraft.core.Direction;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.ModelUtil.UvFaceData;
import buildcraft.lib.client.model.MutableQuad;

import buildcraft.transport.BCTransportSprites;

public final class PipeItemColourQuads {
    private static final MutableQuad[] COLOURED_QUADS = new MutableQuad[6];
    private static boolean initialized;

    private PipeItemColourQuads() {}

    public static MutableQuad[] get() {
        ensureInitialized();
        return COLOURED_QUADS;
    }

    private static void ensureInitialized() {
        if (initialized) {
            return;
        }
        initialized = true;

        Vector3f center = new Vector3f();
        Vector3f radius = new Vector3f(0.2f, 0.2f, 0.2f);
        var sprite = BCTransportSprites.COLOUR_ITEM_BOX;
        UvFaceData uvs = new UvFaceData();
        uvs.minU = (float) sprite.getInterpU(0);
        uvs.maxU = (float) sprite.getInterpU(1);
        uvs.minV = (float) sprite.getInterpV(0);
        uvs.maxV = (float) sprite.getInterpV(1);

        for (Direction face : Direction.values()) {
            MutableQuad quad = ModelUtil.createFace(face, center, radius, uvs);
            quad.setCalculatedDiffuse();
            COLOURED_QUADS[face.ordinal()] = quad;
        }
    }
}
