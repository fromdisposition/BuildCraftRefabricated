/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.model.geometry.BakedQuad;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.config.DetailedConfigOption;

import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseCutoutKey;
import buildcraft.transport.client.model.PipeModelCacheBase.PipeBaseTranslucentKey;

public interface IPipeBaseModelGen {
    DetailedConfigOption OPTION_INSIDE_COLOUR_MULT = new DetailedConfigOption("render.pipe.misc.inside.shade", "0.725");

    List<BakedQuad> generateCutout(PipeBaseCutoutKey key);

    List<BakedQuad> generateTranslucent(PipeBaseTranslucentKey key);

    TextureAtlasSprite[] getItemSprites(PipeDefinition def);

    default List<MutableQuad> generateCutoutMutable(PipeBaseCutoutKey key) {
        return new ArrayList<>();
    }

    default List<MutableQuad> generateTranslucentMutable(PipeBaseTranslucentKey key) {
        return new ArrayList<>();
    }
}
