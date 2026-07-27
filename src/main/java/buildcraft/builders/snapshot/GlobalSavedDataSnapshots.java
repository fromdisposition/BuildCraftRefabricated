/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.lib.fabric.loader.GamePaths;
import buildcraft.lib.misc.data.SingleCache;
import buildcraft.lib.nbt.NbtSquisher;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
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
import org.jspecify.annotations.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GlobalSavedDataSnapshots {
   private static final String SNAPSHOT_FILE_EXTENSION = ".bcnbt";
   private static final Map<GlobalSavedDataSnapshots.Side, GlobalSavedDataSnapshots> INSTANCES = new EnumMap<>(GlobalSavedDataSnapshots.Side.class);
   private final LoadingCache<Snapshot.Key, Optional<Snapshot>> snapshotsCache = CacheBuilder.newBuilder()
      //? if >= 26.2 {
      .expireAfterAccess(java.time.Duration.ofMinutes(10))
      //?} else {
      /*.expireAfterAccess(10L, TimeUnit.MINUTES)
      *///?}
      .build(CacheLoader.from(key -> Optional.ofNullable(this.readSnapshot(key)).map(Pair::getLeft)));
   private final SingleCache<List<Snapshot.Key>> listCache = new SingleCache<>(this::readList, 1L, TimeUnit.SECONDS);
   private final File snapshotsFile;
   private static final Logger LOGGER = LogManager.getLogger("BCSavedSnapshots");

   private GlobalSavedDataSnapshots(GlobalSavedDataSnapshots.Side side) {
      this.snapshotsFile = new File(GamePaths.GAMEDIR.toFile(), "snapshots-" + side.name().toLowerCase(Locale.ROOT));
      if (!this.snapshotsFile.exists()) {
         if (!this.snapshotsFile.mkdirs()) {
            throw new RuntimeException("Failed to make the directories required for snapshots: " + this.snapshotsFile);
         }
      } else if (!this.snapshotsFile.isDirectory()) {
         throw new IllegalStateException("The snapshots directory was not a directory: " + this.snapshotsFile);
      }
   }

   public static void reInit(GlobalSavedDataSnapshots.Side side) {
      INSTANCES.put(side, new GlobalSavedDataSnapshots(side));
   }

   public static GlobalSavedDataSnapshots get(GlobalSavedDataSnapshots.Side side) {
      if (!INSTANCES.containsKey(side)) {
         INSTANCES.put(side, new GlobalSavedDataSnapshots(side));
      }

      return INSTANCES.get(side);
   }

   public static GlobalSavedDataSnapshots get(Level world) {
      return get(world.isClientSide() ? GlobalSavedDataSnapshots.Side.CLIENT : GlobalSavedDataSnapshots.Side.SERVER);
   }

   private Pair<Snapshot, File> readSnapshot(Snapshot.Key key) {
      File direct = this.snapshotFileFor(key);
      if (direct.isFile()) {
         return this.readSnapshotFile(direct, key);
      }

      String targetPrefix = key.toString();
      LOGGER.debug("readSnapshot: looking for key-prefix={} in dir={}", targetPrefix, this.snapshotsFile);
      File[] files = this.snapshotsFile.listFiles();
      if (files == null) {
         LOGGER.warn("readSnapshot: listFiles() returned null for dir={}", this.snapshotsFile);
         return null;
      }

      int matchedPrefix = 0;

      for (File snapshotFile : files) {
         if (snapshotFile.getName().startsWith(targetPrefix) && snapshotFile.getName().endsWith(".bcnbt")) {
            matchedPrefix++;
            Pair<Snapshot, File> loaded = this.readSnapshotFile(snapshotFile, key);
            if (loaded != null) {
               return loaded;
            }
         }
      }

      LOGGER.debug("readSnapshot: no file matched prefix={} (files-with-prefix-match={})", targetPrefix, matchedPrefix);
      return null;
   }

   @Nullable
   private Pair<Snapshot, File> readSnapshotFile(File snapshotFile, Snapshot.Key key) {
      try (FileInputStream fileInputStream = new FileInputStream(snapshotFile)) {
         Snapshot snapshot = Snapshot.readFromNBT(NbtSquisher.expand(fileInputStream));
         if (Objects.equals(snapshot.key, key)) {
            return Pair.of(snapshot, snapshotFile);
         }

         LOGGER.debug("readSnapshot: file {} key mismatch", snapshotFile.getName());
      } catch (InvalidInputDataException e) {
         LOGGER.warn("readSnapshot: corrupted snapshot file {}: {}", snapshotFile, e.getMessage());
      } catch (IOException e) {
         LOGGER.warn("readSnapshot: IO error reading {}", snapshotFile, e);
      } catch (Throwable t) {
         LOGGER.error("readSnapshot: unexpected error reading {}", snapshotFile, t);
      }

      return null;
   }

   private File snapshotFileFor(Snapshot.Key key) {
      return new File(this.snapshotsFile, key.toString() + ".bcnbt");
   }

   private List<Snapshot.Key> readList() {
      Builder<Snapshot.Key> listBuilder = ImmutableList.builder();
      File[] files = this.snapshotsFile.listFiles();
      if (files != null) {
         for (File snapshotFile : files) {
            if (snapshotFile.getName().endsWith(".bcnbt")) {
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
      File snapshotFile = this.snapshotFileFor(snapshot.key);
      if (snapshotFile.exists()) {
         this.snapshotsCache.invalidate(snapshot.key);
      } else {
         CompoundTag nbt = Snapshot.writeToNBT(snapshot);
         this.snapshotsCache.invalidate(snapshot.key);
         this.listCache.clear();
         BuildersNetworkAsync.runDiskWrite(() -> {
            try (FileOutputStream fileOutputStream = new FileOutputStream(snapshotFile)) {
               NbtSquisher.squishVanilla(nbt, fileOutputStream);
            } catch (IOException e) {
               BCLog.logger.error("Failed to write the snapshot file: " + snapshotFile, e);
               if (snapshotFile.exists() && !snapshotFile.delete()) {
                  BCLog.logger.warn("Failed to delete partial snapshot file: {}", snapshotFile);
               }
            }

            this.snapshotsCache.invalidate(snapshot.key);
            this.listCache.clear();
         });
      }
   }

   public void removeSnapshot(Snapshot.Key key) {
      Optional.ofNullable(this.readSnapshot(key)).<File>map(Pair::getRight).ifPresent(snapshotFile -> {
         if (!snapshotFile.delete()) {
            BCLog.logger.error("Failed to delete the snapshot file: " + snapshotFile);
         }

         this.snapshotsCache.invalidate(key);
      });
      this.listCache.clear();
   }

   @Nullable
   public Snapshot getSnapshot(Snapshot.@Nullable Key key) {
      return key == null ? null : this.snapshotsCache.getUnchecked(key).orElse(null);
   }

   public List<Snapshot.Key> getList() {
      return this.listCache.get();
   }

   public enum Side {
      CLIENT,
      SERVER;
   }
}
