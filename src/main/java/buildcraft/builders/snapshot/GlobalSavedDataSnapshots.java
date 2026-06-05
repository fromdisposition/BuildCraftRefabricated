/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.world.level.Level;

import buildcraft.lib.fabric.loader.GamePaths;
import buildcraft.fabric.env.Env;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.lib.misc.data.SingleCache;
import buildcraft.lib.nbt.NbtSquisher;

public class GlobalSavedDataSnapshots {
    private static final String SNAPSHOT_FILE_EXTENSION = ".bcnbt";

    public enum Side {
        CLIENT, SERVER;
    }

    private static final Map<Side, GlobalSavedDataSnapshots> INSTANCES = new EnumMap<>(Side.class);
    private final LoadingCache<Snapshot.Key, Optional<Snapshot>> snapshotsCache = CacheBuilder.newBuilder()
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .build(CacheLoader.from(key -> Optional.ofNullable(readSnapshot(key)).map(Pair::getLeft)));
    private final SingleCache<List<Snapshot.Key>> listCache = new SingleCache<>(
        this::readList,
        1,
        TimeUnit.SECONDS
    );
    private final File snapshotsFile;

    private GlobalSavedDataSnapshots(Side side) {
        snapshotsFile = new File(
            GamePaths.GAMEDIR.toFile(),
            "snapshots-" + side.name().toLowerCase(Locale.ROOT)
        );
        if (!snapshotsFile.exists()) {
            if (!snapshotsFile.mkdirs()) {
                throw new RuntimeException("Failed to make the directories required for snapshots: " + snapshotsFile);
            }
        } else if (!snapshotsFile.isDirectory()) {
            throw new IllegalStateException("The snapshots directory was not a directory: " + snapshotsFile);
        }
    }

    public static void reInit(Side side) {
        INSTANCES.put(side, new GlobalSavedDataSnapshots(side));
    }

    public static GlobalSavedDataSnapshots get(Side side) {
        if (!INSTANCES.containsKey(side)) {
            INSTANCES.put(side, new GlobalSavedDataSnapshots(side));
        }
        return INSTANCES.get(side);
    }

    public static GlobalSavedDataSnapshots get(Level world) {
        return get(world.isClientSide() ? Side.CLIENT : Side.SERVER);
    }

    private static final org.apache.logging.log4j.Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger("BCSavedSnapshots");

    private Pair<Snapshot, File> readSnapshot(Snapshot.Key key) {
        String targetPrefix = key.toString();
        LOGGER.info("readSnapshot: looking for key-prefix={} in dir={}", targetPrefix, snapshotsFile);
        File[] files = snapshotsFile.listFiles();
        if (files == null) {
            LOGGER.warn("readSnapshot: listFiles() returned null for dir={}", snapshotsFile);
            return null;
        }
        LOGGER.info("readSnapshot: directory has {} entries", files.length);
        int matchedPrefix = 0;
        for (File snapshotFile : files) {
            if (snapshotFile.getName().startsWith(targetPrefix) &&
                snapshotFile.getName().endsWith(SNAPSHOT_FILE_EXTENSION)) {
                matchedPrefix++;
                try (FileInputStream fileInputStream = new FileInputStream(snapshotFile)) {
                    Snapshot snapshot = Snapshot.readFromNBT(NbtSquisher.expand(fileInputStream));
                    String loadedHash = snapshot.key == null || snapshot.key.hash == null
                            ? "null"
                            : buildcraft.lib.misc.HashUtil.convertHashToString(snapshot.key.hash);
                    boolean matches = Objects.equals(snapshot.key, key);
                    LOGGER.info("readSnapshot: loaded {} class={} loadedHash={} loadedHeader={} "
                            + "requestedHeader={} equals={}",
                            snapshotFile.getName(),
                            snapshot.getClass().getSimpleName(),
                            loadedHash,
                            snapshot.key != null && snapshot.key.header != null,
                            key.header != null,
                            matches);
                    if (matches) {
                        return Pair.of(snapshot, snapshotFile);
                    }
                } catch (InvalidInputDataException e) {
                    LOGGER.warn("readSnapshot: corrupted snapshot file {}: {}", snapshotFile, e.getMessage());
                } catch (IOException e) {
                    LOGGER.warn("readSnapshot: IO error reading {}", snapshotFile, e);
                } catch (Throwable t) {
                    LOGGER.error("readSnapshot: unexpected error reading {}", snapshotFile, t);
                }
            }
        }
        LOGGER.warn("readSnapshot: no file matched prefix={} (files-with-prefix-match={})", targetPrefix, matchedPrefix);
        return null;
    }

    private List<Snapshot.Key> readList() {
        ImmutableList.Builder<Snapshot.Key> listBuilder = ImmutableList.builder();
        File[] files = snapshotsFile.listFiles();
        if (files != null) {
            for (File snapshotFile : files) {
                if (snapshotFile.getName().endsWith(SNAPSHOT_FILE_EXTENSION)) {
                    try (FileInputStream fileInputStream = new FileInputStream(snapshotFile)) {
                        Snapshot snapshot = Snapshot.readFromNBT(NbtSquisher.expand(fileInputStream));
                        if (snapshotFile.getName().startsWith(snapshot.key.toString())) {
                            listBuilder.add(snapshot.key);
                        }
                    } catch (InvalidInputDataException e) {
                        BCLog.logger.warn("Skipping corrupted snapshot file: " + snapshotFile + " - " + e.getMessage());
                    } catch (IOException io) {
                        BCLog.logger.error("Failed to read the snapshot " + snapshotFile, io);
                    }
                }
            }
        }
        return listBuilder.build();
    }

    public void addSnapshot(Snapshot snapshot) {
        File snapshotFile = new File(
            snapshotsFile,
            snapshot.key.toString() + SNAPSHOT_FILE_EXTENSION
        );
        if (!snapshotFile.exists()) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(snapshotFile)) {
                NbtSquisher.squishVanilla(Snapshot.writeToNBT(snapshot), fileOutputStream);
            } catch (IOException e) {
                BCLog.logger.error("Failed to write the snapshot file: " + snapshotFile, e);
            }
        }
        snapshotsCache.invalidate(snapshot.key);
        listCache.clear();
    }

    public void removeSnapshot(Snapshot.Key key) {
        Optional.ofNullable(readSnapshot(key)).map(Pair::getRight).ifPresent(snapshotFile -> {
            if (!snapshotFile.delete()) {
                BCLog.logger.error("Failed to delete the snapshot file: " + snapshotFile);
            }
            snapshotsCache.invalidate(key);
        });
        listCache.clear();
    }

    @Nullable
    public Snapshot getSnapshot(@Nullable Snapshot.Key key) {
        if (key == null) return null;
        return snapshotsCache.getUnchecked(key).orElse(null);
    }

    public List<Snapshot.Key> getList() {
        return listCache.get();
    }
}
