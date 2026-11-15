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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
      .maximumSize(256L)
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

   public static GlobalSavedDataSnapshots get(GlobalSavedDataSnapshots.Side side) {
      if (!INSTANCES.containsKey(side)) {
         INSTANCES.put(side, new GlobalSavedDataSnapshots(side));
      }

      return INSTANCES.get(side);
   }

   public static GlobalSavedDataSnapshots get(Level world) {
      return get(world.isClientSide() ? GlobalSavedDataSnapshots.Side.CLIENT : GlobalSavedDataSnapshots.Side.SERVER);
   }

   @Nullable
   private Pair<Snapshot, File> readSnapshot(Snapshot.Key key) {
      File direct = this.snapshotFileFor(key);
      return direct.isFile() ? this.readSnapshotFile(direct, key) : null;
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
      return new File(this.snapshotsFile, key.toString() + SNAPSHOT_FILE_EXTENSION);
   }

   private List<Snapshot.Key> readList() {
      Builder<Snapshot.Key> listBuilder = ImmutableList.builder();
      File[] files = this.snapshotsFile.listFiles();
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
               } catch (IOException | RuntimeException io) {
                  BCLog.logger.error("Failed to read the snapshot " + snapshotFile, io);
               }
            }
         }
      }

      return listBuilder.build();
   }

   public void addSnapshot(Snapshot snapshot) {
      File snapshotFile = this.snapshotFileFor(snapshot.key);
      this.snapshotsCache.put(snapshot.key, Optional.of(snapshot));
      if (!snapshotFile.exists()) {
         CompoundTag nbt = Snapshot.writeToNBT(snapshot);
         this.listCache.clear();
         BuildersNetworkAsync.runDiskWrite(() -> {
            Path tempFile = null;
            try {
               tempFile = Files.createTempFile(this.snapshotsFile.toPath(), snapshot.key.toString(), ".tmp");
               try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile.toFile())) {
                  NbtSquisher.squishVanilla(nbt, fileOutputStream);
               }

               Files.move(tempFile, snapshotFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
               BCLog.logger.error("Failed to write the snapshot file: " + snapshotFile, e);
               if (tempFile != null && tempFile.toFile().exists() && !tempFile.toFile().delete()) {
                  BCLog.logger.warn("Failed to delete partial snapshot file: {}", tempFile);
               }
            }

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
