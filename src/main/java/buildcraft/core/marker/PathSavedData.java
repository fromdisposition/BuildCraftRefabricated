/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.marker;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedDataType;

public class PathSavedData extends SavedData {
    public static final String ID = "buildcraft_marker_path";

    public List<BlockPos> markerPositions = new ArrayList<>();
    public List<List<BlockPos>> markerConnections = new ArrayList<>();

    private PathSubCache subCache;

    private PathSavedData() {
    }

    public void setSubCache(PathSubCache subCache) {
        this.subCache = subCache;
    }

    private void syncFromSubCache() {
        if (subCache == null) return;
        markerPositions = new ArrayList<>(subCache.getAllMarkers());
        markerConnections = new ArrayList<>();
        for (PathConnection connection : subCache.getConnections()) {
            markerConnections.add(new ArrayList<>(connection.getMarkerPositions()));
        }
    }

    private static final Codec<PathSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().optionalFieldOf("markers", List.of())
                    .forGetter(d -> {
                        d.syncFromSubCache();
                        return d.markerPositions;
                    }),
            BlockPos.CODEC.listOf().listOf().optionalFieldOf("connections", List.of())
                    .forGetter(d -> d.markerConnections)
    ).apply(instance, (positions, connections) -> {
        PathSavedData data = new PathSavedData();
        data.markerPositions = new ArrayList<>(positions);
        data.markerConnections = new ArrayList<>();
        for (List<BlockPos> conn : connections) {
            data.markerConnections.add(new ArrayList<>(conn));
        }
        return data;
    }));

    public static final SavedDataType<PathSavedData> TYPE = new SavedDataType<>(

            Identifier.withDefaultNamespace(ID),

            PathSavedData::new,
            CODEC,
            net.minecraft.util.datafix.DataFixTypes.LEVEL
    );

    public static PathSavedData getOrCreate(Level level) {
        if (level.isClientSide()) return new PathSavedData();
        return ((net.minecraft.server.level.ServerLevel) level)
                .getDataStorage().computeIfAbsent(TYPE);
    }
}
