/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders;

import buildcraft.builders.client.render.BuilderRobotVisualRenderer;
import buildcraft.builders.snapshot.ClientArchitectScans;
import buildcraft.builders.snapshot.ITileForSnapshotBuilder;
import buildcraft.builders.snapshot.SnapshotBuilder;
import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.builders.tile.TileFiller;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.fabric.client.event.RenderLevelStageEvent;
import buildcraft.fabric.client.event.SubmitCustomGeometryEvent;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.render.ItemRenderUtil;
import buildcraft.lib.client.render.laser.BcLaserRenderer;
import buildcraft.lib.client.render.laser.LaserBatch;
import buildcraft.lib.client.render.laser.LaserBoxRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.VecUtil;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
//? if >= 26.1.3 {
//?} else {
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
//?}
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public enum BCBuildersEventDist {
   INSTANCE;

   public static final LaserData_BC8.LaserType FRAME;
   public static final LaserData_BC8.LaserType FRAME_BOTTOM;
   public static final LaserData_BC8.LaserType DRILL;
   private final Map<Level, Deque<WeakReference<TileQuarry>>> allQuarries = new WeakHashMap<>();
   private final Map<Level, Deque<WeakReference<TileFiller>>> allFillers = new WeakHashMap<>();
   private final Map<Level, Deque<WeakReference<TileArchitectTable>>> allArchitectTables = new WeakHashMap<>();
   private final Map<Level, Deque<WeakReference<TileBuilder>>> allBuilders = new WeakHashMap<>();
   public static final Identifier DESTROYING_THE_WORLD = Identifier.parse("buildcraftbuilders:destroying_the_world");
   public static final long FULL_SPEED_WINDOW_TICKS = 40L;
   static final int SCAN_INTERVAL_TICKS = 20;
   private static final double PATH_LASER_INSET = 0.1;
   private long serverTickCounter = 0L;
   private static final Identifier SCAN_TEXTURE = Identifier.parse("buildcraftbuilders:textures/block/scan.png");

   public synchronized void validateArchitectTable(TileArchitectTable table) {
      Deque<WeakReference<TileArchitectTable>> tables = this.allArchitectTables.computeIfAbsent(table.getLevel(), k -> new LinkedList<>());
      tables.add(new WeakReference<>(table));
   }

   public synchronized void invalidateArchitectTable(TileArchitectTable table) {
      Deque<WeakReference<TileArchitectTable>> tables = this.allArchitectTables.get(table.getLevel());
      if (tables != null) {
         Iterator<WeakReference<TileArchitectTable>> iter = tables.iterator();

         while (iter.hasNext()) {
            WeakReference<TileArchitectTable> ref = iter.next();
            TileArchitectTable t = ref.get();
            if (t == null || t == table) {
               iter.remove();
            }
         }
      }
   }

   public synchronized void validateBuilder(TileBuilder builder) {
      Deque<WeakReference<TileBuilder>> builders = this.allBuilders.computeIfAbsent(builder.getLevel(), k -> new LinkedList<>());
      builders.add(new WeakReference<>(builder));
   }

   public synchronized void invalidateBuilder(TileBuilder builder) {
      Deque<WeakReference<TileBuilder>> builders = this.allBuilders.get(builder.getLevel());
      if (builders != null) {
         Iterator<WeakReference<TileBuilder>> iter = builders.iterator();

         while (iter.hasNext()) {
            WeakReference<TileBuilder> ref = iter.next();
            TileBuilder b = ref.get();
            if (b == null || b == builder) {
               iter.remove();
            }
         }
      }
   }

   public synchronized void validateQuarry(TileQuarry quarry) {
      Deque<WeakReference<TileQuarry>> quarries = this.allQuarries.computeIfAbsent(quarry.getLevel(), k -> new LinkedList<>());
      quarries.add(new WeakReference<>(quarry));
   }

   public synchronized void invalidateQuarry(TileQuarry quarry) {
      Deque<WeakReference<TileQuarry>> quarries = this.allQuarries.get(quarry.getLevel());
      if (quarries != null) {
         Iterator<WeakReference<TileQuarry>> iter = quarries.iterator();

         while (iter.hasNext()) {
            WeakReference<TileQuarry> ref = iter.next();
            TileQuarry pos = ref.get();
            if (pos == null || pos == quarry) {
               iter.remove();
            }
         }
      }
   }

   public synchronized Set<UUID> findOwnersToAward(Level level, long currentTick) {
      Set<UUID> winners = new HashSet<>();
      if (level != null && !level.isClientSide()) {
         Deque<WeakReference<TileQuarry>> quarries = this.allQuarries.get(level);
         if (quarries != null && quarries.size() >= 2) {
            Map<UUID, Integer> countByOwner = new HashMap<>();
            Iterator<WeakReference<TileQuarry>> iter = quarries.iterator();

            while (iter.hasNext()) {
               TileQuarry q = iter.next().get();
               if (q == null || q.isRemoved()) {
                  iter.remove();
               } else if (q.frameBox.isInitialized()) {
                  int sizeX = q.frameBox.max().getX() - q.frameBox.min().getX() + 1;
                  int sizeZ = q.frameBox.max().getZ() - q.frameBox.min().getZ() + 1;
                  if (sizeX >= 64 && sizeZ >= 64) {
                     long lastFullSpeed = q.getLastFullSpeedTick();
                     if (lastFullSpeed != Long.MIN_VALUE && currentTick - lastFullSpeed <= 40L) {
                        GameProfile owner = q.getOwner();
                        if (owner != null && owner.id() != null) {
                           int next = countByOwner.getOrDefault(owner.id(), 0) + 1;
                           countByOwner.put(owner.id(), next);
                           if (next >= 2) {
                              winners.add(owner.id());
                           }
                        }
                     }
                  }
               }
            }

            return winners;
         } else {
            return winners;
         }
      } else {
         return winners;
      }
   }

   public synchronized void onServerTick() {
      this.serverTickCounter++;
      if (this.serverTickCounter % 20L == 0L) {
         for (Entry<Level, Deque<WeakReference<TileQuarry>>> entry : this.allQuarries.entrySet()) {
            Level level = entry.getKey();
            Deque<WeakReference<TileQuarry>> quarries = entry.getValue();
            if (quarries != null && quarries.size() >= 2) {
               long now = level.getGameTime();

               for (UUID winner : this.findOwnersToAward(level, now)) {
                  AdvancementUtil.unlockAdvancement(winner, level, DESTROYING_THE_WORLD);
               }
            }
         }
      }
   }

   public synchronized void validateFiller(TileFiller filler) {
      Deque<WeakReference<TileFiller>> fillers = this.allFillers.computeIfAbsent(filler.getLevel(), k -> new LinkedList<>());
      fillers.add(new WeakReference<>(filler));
   }

   public synchronized void invalidateFiller(TileFiller filler) {
      Deque<WeakReference<TileFiller>> fillers = this.allFillers.get(filler.getLevel());
      if (fillers != null) {
         Iterator<WeakReference<TileFiller>> iter = fillers.iterator();

         while (iter.hasNext()) {
            WeakReference<TileFiller> ref = iter.next();
            TileFiller f = ref.get();
            if (f == null || f == filler) {
               iter.remove();
            }
         }
      }
   }

   public void renderAllQuarries(RenderLevelStageEvent.AfterTranslucentBlocks event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         Deque<WeakReference<TileQuarry>> quarries = this.allQuarries.get(mc.level);
         if (quarries != null && !quarries.isEmpty()) {
            Vec3 cameraPos = event.getLevelRenderState().cameraRenderState.pos;
            PoseStack poseStack = event.getPoseStack();
            float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            LaserBatch.begin();

            try {
               Iterator<WeakReference<TileQuarry>> iter = quarries.iterator();

               while (iter.hasNext()) {
                  WeakReference<TileQuarry> ref = iter.next();
                  TileQuarry quarry = ref.get();
                  if (quarry != null && !quarry.isRemoved()) {
                     this.renderQuarry(quarry, poseStack, cameraPos, partialTicks);
                  } else {
                     iter.remove();
                  }
               }
            } finally {
               LaserBatch.end();
            }
         }
      }
   }

   private void renderQuarry(TileQuarry tile, PoseStack poseStack, Vec3 cameraPos, float partialTicks) {
      if (tile.frameBox.isInitialized()) {
         BlockPos min = tile.frameBox.min();
         BlockPos max = tile.frameBox.max();
         double yOffset = 1.25;
         if (tile.currentTask instanceof TileQuarry.TaskBreakBlock taskBreakBlock) {
            BlockPos pos = taskBreakBlock.breakPos;
            if (tile.drillPos == null) {
               if (taskBreakBlock.clientPower != 0L) {
                  Vec3 from = VecUtil.convertCenter(tile.getBlockPos());
                  Vec3 to = VecUtil.convertCenter(pos);
                  LaserData_BC8 laser = new LaserData_BC8(BuildCraftLaserManager.POWER_LOW, from, to, 0.0625);
                  BcLaserRenderer.renderLaserStatic(poseStack, laser, cameraPos);
               }
            } else {
               long power = (long)(taskBreakBlock.prevClientPower + (double)(taskBreakBlock.clientPower - taskBreakBlock.prevClientPower) * partialTicks);
               double value = (double)power / taskBreakBlock.getTarget();
               if (value < 0.9) {
                  value = 1.0 - value / 0.9;
               } else {
                  value = (value - 0.9) / 0.1;
               }

               double scaleMin = 0.5;
               double scaleMax = 1.25;
               yOffset = scaleMin + value * (scaleMax - scaleMin);
            }
         }

         if (tile.clientDrillPos != null && tile.prevClientDrillPos != null) {
            Vec3 interpolatedPos = tile.prevClientDrillPos.add(tile.clientDrillPos.subtract(tile.prevClientDrillPos).scale(partialTicks));
            double frameY = max.getY() + 0.5;
            //? if >= 26.1.3 {
            /*BcLaserRenderer.renderLasersBatched(
               poseStack,
               List.of(
                  new LaserData_BC8(
                     FRAME, new Vec3(interpolatedPos.x + 0.5, frameY, interpolatedPos.z), new Vec3(interpolatedPos.x + 0.5, frameY, max.getZ() + 0.75), 0.0625
                  ),
                  new LaserData_BC8(
                     FRAME, new Vec3(interpolatedPos.x + 0.5, frameY, interpolatedPos.z), new Vec3(interpolatedPos.x + 0.5, frameY, min.getZ() + 0.25), 0.0625
                  ),
                  new LaserData_BC8(
                     FRAME, new Vec3(interpolatedPos.x, frameY, interpolatedPos.z + 0.5), new Vec3(max.getX() + 0.75, frameY, interpolatedPos.z + 0.5), 0.0625
                  ),
                  new LaserData_BC8(
                     FRAME, new Vec3(interpolatedPos.x, frameY, interpolatedPos.z + 0.5), new Vec3(min.getX() + 0.25, frameY, interpolatedPos.z + 0.5), 0.0625
                  ),
                  new LaserData_BC8(
                     FRAME_BOTTOM,
                     new Vec3(interpolatedPos.x + 0.5, interpolatedPos.y + 1.0 + 0.25, interpolatedPos.z + 0.5),
                     new Vec3(interpolatedPos.x + 0.5, max.getY() + 0.5, interpolatedPos.z + 0.5),
                     0.0625
                  ),
                  new LaserData_BC8(
                     DRILL,
                     new Vec3(interpolatedPos.x + 0.5, interpolatedPos.y + 1.0 + yOffset, interpolatedPos.z + 0.5),
                     new Vec3(interpolatedPos.x + 0.5, interpolatedPos.y + yOffset, interpolatedPos.z + 0.5),
                     0.0625
                  )
               ),
               cameraPos
            );*/
            //?} else {
            BcLaserRenderer.renderLasersBatched(
               poseStack,
               List.of(
                  new LaserData_BC8(
                     FRAME, new Vec3(interpolatedPos.x + 0.5, frameY, interpolatedPos.z), new Vec3(interpolatedPos.x + 0.5, frameY, max.getZ() + 0.75), 0.0625
                  ),
                  new LaserData_BC8(
                     FRAME, new Vec3(interpolatedPos.x + 0.5, frameY, interpolatedPos.z), new Vec3(interpolatedPos.x + 0.5, frameY, min.getZ() + 0.25), 0.0625
                  ),
                  new LaserData_BC8(
                     FRAME, new Vec3(interpolatedPos.x, frameY, interpolatedPos.z + 0.5), new Vec3(max.getX() + 0.75, frameY, interpolatedPos.z + 0.5), 0.0625
                  ),
                  new LaserData_BC8(
                     FRAME, new Vec3(interpolatedPos.x, frameY, interpolatedPos.z + 0.5), new Vec3(min.getX() + 0.25, frameY, interpolatedPos.z + 0.5), 0.0625
                  ),
                  new LaserData_BC8(
                     FRAME_BOTTOM,
                     new Vec3(interpolatedPos.x + 0.5, interpolatedPos.y + 1.0 + 0.25, interpolatedPos.z + 0.5),
                     new Vec3(interpolatedPos.x + 0.5, max.getY() + 0.5, interpolatedPos.z + 0.5),
                     0.0625
                  ),
                  new LaserData_BC8(
                     DRILL,
                     new Vec3(interpolatedPos.x + 0.5, interpolatedPos.y + 1.0 + yOffset, interpolatedPos.z + 0.5),
                     new Vec3(interpolatedPos.x + 0.5, interpolatedPos.y + yOffset, interpolatedPos.z + 0.5),
                     0.0625
                  )
               ),
               cameraPos
            );
            //?}
         } else {
            LaserBoxRenderer.renderLaserBoxStatic(poseStack, tile.frameBox, BuildCraftLaserManager.STRIPES_WRITE, true, false, cameraPos);
         }
      }
   }

   public void renderAllArchitectTables(RenderLevelStageEvent.AfterTranslucentBlocks event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         Vec3 cameraPos = event.getLevelRenderState().cameraRenderState.pos;
         PoseStack poseStack = event.getPoseStack();
         Deque<WeakReference<TileArchitectTable>> tables = this.allArchitectTables.get(mc.level);
         if (tables != null && !tables.isEmpty()) {
            LaserBatch.begin();

            try {
               Iterator<WeakReference<TileArchitectTable>> iter = tables.iterator();

               while (iter.hasNext()) {
                  WeakReference<TileArchitectTable> ref = iter.next();
                  TileArchitectTable table = ref.get();
                  if (table == null || table.isRemoved()) {
                     iter.remove();
                  } else if (table.getIsValid() && table.markerBox && table.box.isInitialized()) {
                     LaserBoxRenderer.renderLaserBoxStatic(poseStack, table.box, BuildCraftLaserManager.STRIPES_READ, true, false, cameraPos);
                  }
               }
            } finally {
               LaserBatch.end();
            }
         }

         //? if >= 26.1.3 {
         /*this.renderDigitizingCubes(cameraPos, poseStack);*/
         //?} else {
         this.renderDigitizingCubes(cameraPos, poseStack, mc);
         //?}
      }
   }

   //? if >= 26.1.3 {
   /*private void renderDigitizingCubes(Vec3 cameraPos, PoseStack poseStack) {
      List<ClientArchitectScans.ScanRun> runs = ClientArchitectScans.INSTANCE.getRuns();
      if (!runs.isEmpty()) {
         RenderType renderType = RenderTypes.entityTranslucent(SCAN_TEXTURE);
         LaserBatch.submitGeometry(poseStack, renderType, (pose, vc) -> {
            for (ClientArchitectScans.ScanRun run : runs) {
               int alpha = Math.max(0, Math.min(50, run.remaining()));
               BlockPos min = run.min();
               BlockPos max = run.max();
               renderScanCuboid(
                  pose, vc,
                  min.getX() - cameraPos.x, min.getY() - cameraPos.y, min.getZ() - cameraPos.z,
                  max.getX() + 1 - cameraPos.x, max.getY() + 1 - cameraPos.y, max.getZ() + 1 - cameraPos.z,
                  alpha
               );
            }
         });
      }
   }*/
   //?} else {
   private void renderDigitizingCubes(Vec3 cameraPos, PoseStack poseStack, Minecraft mc) {
      List<ClientArchitectScans.ScanRun> runs = ClientArchitectScans.INSTANCE.getRuns();
      if (!runs.isEmpty()) {
         BufferSource bufferSource = mc.renderBuffers().bufferSource();
         RenderType renderType = RenderTypes.entityTranslucent(SCAN_TEXTURE);
         VertexConsumer buffer = bufferSource.getBuffer(renderType);
         Pose pose = poseStack.last();

         for (ClientArchitectScans.ScanRun run : runs) {
            int alpha = Math.max(0, Math.min(50, run.remaining()));
            BlockPos min = run.min();
            BlockPos max = run.max();
            renderScanCuboid(
               pose,
               buffer,
               min.getX() - cameraPos.x,
               min.getY() - cameraPos.y,
               min.getZ() - cameraPos.z,
               max.getX() + 1 - cameraPos.x,
               max.getY() + 1 - cameraPos.y,
               max.getZ() + 1 - cameraPos.z,
               alpha
            );
         }

         bufferSource.endBatch(renderType);
      }
   }
   //?}

   private static void renderScanCuboid(
      Pose pose, VertexConsumer buffer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int alpha
   ) {
      scanQuad(pose, buffer, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, minX, minY, minZ, 0.0F, -1.0F, 0.0F, alpha);
      scanQuad(pose, buffer, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, 0.0F, 1.0F, 0.0F, alpha);
      scanQuad(pose, buffer, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, minX, minY, minZ, 0.0F, 0.0F, -1.0F, alpha);
      scanQuad(pose, buffer, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, minY, maxZ, 0.0F, 0.0F, 1.0F, alpha);
      scanQuad(pose, buffer, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, minX, minY, maxZ, -1.0F, 0.0F, 0.0F, alpha);
      scanQuad(pose, buffer, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, maxX, minY, minZ, 1.0F, 0.0F, 0.0F, alpha);
   }

   private static void scanQuad(
      Pose pose,
      VertexConsumer buffer,
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      double x3,
      double y3,
      double z3,
      double x4,
      double y4,
      double z4,
      float nx,
      float ny,
      float nz,
      int alpha
   ) {
      scanVertex(pose, buffer, x1, y1, z1, 0.0F, 0.0F, nx, ny, nz, alpha);
      scanVertex(pose, buffer, x2, y2, z2, 0.0F, 1.0F, nx, ny, nz, alpha);
      scanVertex(pose, buffer, x3, y3, z3, 1.0F, 1.0F, nx, ny, nz, alpha);
      scanVertex(pose, buffer, x4, y4, z4, 1.0F, 0.0F, nx, ny, nz, alpha);
   }

   private static void scanVertex(Pose pose, VertexConsumer buffer, double x, double y, double z, float u, float v, float nx, float ny, float nz, int alpha) {
      buffer.addVertex(pose.pose(), (float)x, (float)y, (float)z)
         .setColor(255, 255, 255, alpha)
         .setUv(u, v)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(pose, nx, ny, nz);
   }

   public void renderAllBuilders(RenderLevelStageEvent.AfterTranslucentBlocks event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         Deque<WeakReference<TileBuilder>> builders = this.allBuilders.get(mc.level);
         if (builders != null && !builders.isEmpty()) {
            Vec3 cameraPos = event.getLevelRenderState().cameraRenderState.pos;
            PoseStack poseStack = event.getPoseStack();
            float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            LaserBatch.begin();

            try {
               Iterator<WeakReference<TileBuilder>> iter = builders.iterator();

               while (iter.hasNext()) {
                  WeakReference<TileBuilder> ref = iter.next();
                  TileBuilder builder = ref.get();
                  if (builder != null && !builder.isRemoved()) {
                     if (builder.getBox() != null && builder.getBox().isInitialized()) {
                        LaserBoxRenderer.renderLaserBoxStatic(poseStack, builder.getBox(), BuildCraftLaserManager.STRIPES_WRITE, true, false, cameraPos);
                     }

                     List<BlockPos> path = builder.path;
                     if (path != null && path.size() >= 2) {
                        for (int i = 1; i < path.size(); i++) {
                           Vec3 from = Vec3.atCenterOf((Vec3i)path.get(i - 1));
                           Vec3 to = Vec3.atCenterOf((Vec3i)path.get(i));
                           Vec3 dir = to.subtract(from).normalize().scale(0.1);
                           BcLaserRenderer.renderLaserStatic(
                              poseStack,
                              new LaserData_BC8(BuildCraftLaserManager.STRIPES_WRITE_DIRECTION, from.add(dir), to.subtract(dir), 0.06211180124223602),
                              cameraPos
                           );
                        }
                     }

                     SnapshotBuilder<?> active = builder.getBuilder();
                     if (active != null) {
                        Vec3 robotPos = active.visualRobotPos;
                        if (robotPos != null) {
                           if (active.visualPrevRobotPos != null) {
                              robotPos = active.visualPrevRobotPos.add(robotPos.subtract(active.visualPrevRobotPos).scale(partialTicks));
                           }

                           BuilderRobotVisualRenderer.renderRobotAndBreakTasks(mc, poseStack, cameraPos, robotPos, active);
                        }
                     }
                  } else {
                     iter.remove();
                  }
               }
            } finally {
               LaserBatch.end();
            }
         }
      }
   }

   public void renderAllBuildersCustomGeometry(SubmitCustomGeometryEvent event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         Deque<WeakReference<TileBuilder>> builders = this.allBuilders.get(mc.level);
         if (builders != null && !builders.isEmpty()) {
            Vec3 cameraPos = event.getLevelRenderState().cameraRenderState.pos;
            PoseStack poseStack = event.getPoseStack();
            float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            SubmitNodeCollector collector = event.getSubmitNodeCollector();

            for (WeakReference<TileBuilder> ref : builders) {
               TileBuilder builder = ref.get();
               if (builder != null && !builder.isRemoved()) {
                  SnapshotBuilder<?> active = builder.getBuilder();
                  if (active != null && !active.clientPlaceTasks.isEmpty()) {
                     renderPlaceTasks(active, cameraPos, poseStack, collector, partialTicks);
                  }
               }
            }
         }
      }
   }

   private static <T extends ITileForSnapshotBuilder> void renderPlaceTasks(
      SnapshotBuilder<T> active, Vec3 cameraPos, PoseStack poseStack, SubmitNodeCollector collector, float partialTicks
   ) {
      for (SnapshotBuilder<T>.PlaceTask placeTask : active.clientPlaceTasks) {
         Vec3 prevPos = active.prevClientPlaceTasks
            .stream()
            .filter(task -> task.pos.equals(placeTask.pos))
            .map(active::getPlaceTaskItemPos)
            .findFirst()
            .orElse(active.getPlaceTaskItemPos(placeTask));
         Vec3 pos = prevPos.add(active.getPlaceTaskItemPos(placeTask).subtract(prevPos).scale(partialTicks));
         int light = BcLaserRenderer.computeLightmap(pos.x, pos.y, pos.z, 0);
         ItemRenderUtil.beginItemBatch(poseStack, collector, light);

         for (Object itemObj : placeTask.items) {
            ItemStack item = (ItemStack)itemObj;
            ItemRenderUtil.renderItemStack(pos.x - cameraPos.x, pos.y - cameraPos.y, pos.z - cameraPos.z, item, 1, light, Direction.SOUTH, null);
         }
      }
   }

   public void renderAllFillers(RenderLevelStageEvent.AfterTranslucentBlocks event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         Deque<WeakReference<TileFiller>> fillers = this.allFillers.get(mc.level);
         if (fillers != null && !fillers.isEmpty()) {
            Vec3 cameraPos = event.getLevelRenderState().cameraRenderState.pos;
            PoseStack poseStack = event.getPoseStack();
            LaserBatch.begin();

            try {
               Iterator<WeakReference<TileFiller>> iter = fillers.iterator();

               while (iter.hasNext()) {
                  WeakReference<TileFiller> ref = iter.next();
                  TileFiller filler = ref.get();
                  if (filler != null && !filler.isRemoved()) {
                     if (filler.markerBox && filler.box.isInitialized()) {
                        LaserBoxRenderer.renderLaserBoxStatic(poseStack, filler.box, BuildCraftLaserManager.STRIPES_WRITE, true, false, cameraPos);
                     }

                     if (filler.builder != null) {
                        float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
                        Vec3 robotPos = filler.builder.visualRobotPos;
                        if (robotPos != null) {
                           if (filler.builder.visualPrevRobotPos != null) {
                              robotPos = filler.builder.visualPrevRobotPos.add(robotPos.subtract(filler.builder.visualPrevRobotPos).scale(partialTicks));
                           }

                           BuilderRobotVisualRenderer.renderRobotAndBreakTasks(mc, poseStack, cameraPos, robotPos, filler.builder);
                        }
                     }
                  } else {
                     iter.remove();
                  }
               }
            } finally {
               LaserBatch.end();
            }
         }
      }
   }

   public void renderAllFillersCustomGeometry(SubmitCustomGeometryEvent event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         Deque<WeakReference<TileFiller>> fillers = this.allFillers.get(mc.level);
         if (fillers != null && !fillers.isEmpty()) {
            Vec3 cameraPos = event.getLevelRenderState().cameraRenderState.pos;
            PoseStack poseStack = event.getPoseStack();
            float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            SubmitNodeCollector collector = event.getSubmitNodeCollector();

            for (WeakReference<TileFiller> ref : fillers) {
               TileFiller filler = ref.get();
               if (filler != null && !filler.isRemoved() && filler.builder != null && !filler.builder.clientPlaceTasks.isEmpty()) {
                  Map<BlockPos, Vec3> prevPlacePosByBlock = new HashMap<>();

                  for (var prevTask : filler.builder.prevClientPlaceTasks) {
                     prevPlacePosByBlock.put(prevTask.pos, filler.builder.getPlaceTaskItemPos(prevTask));
                  }

                  for (var placeTask : filler.builder.clientPlaceTasks) {
                     Vec3 targetPos = filler.builder.getPlaceTaskItemPos(placeTask);
                     Vec3 prevPos = prevPlacePosByBlock.getOrDefault(placeTask.pos, targetPos);
                     Vec3 pos = prevPos.add(targetPos.subtract(prevPos).scale(partialTicks));
                     int light = BcLaserRenderer.computeLightmap(pos.x, pos.y, pos.z, 0);
                     ItemRenderUtil.beginItemBatch(poseStack, collector, light);

                     for (Object itemObj : placeTask.items) {
                        ItemStack item = (ItemStack)itemObj;
                        ItemRenderUtil.renderItemStack(pos.x - cameraPos.x, pos.y - cameraPos.y, pos.z - cameraPos.z, item, 1, light, Direction.SOUTH, null);
                     }
                  }
               }
            }
         }
      }
   }

   static {
      SpriteHolderRegistry.SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:block/frame/default");
      LaserData_BC8.LaserRow capStart = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
      LaserData_BC8.LaserRow start = null;
      LaserData_BC8.LaserRow[] middle = new LaserData_BC8.LaserRow[]{new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12)};
      LaserData_BC8.LaserRow end = new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12);
      LaserData_BC8.LaserRow capEnd = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
      FRAME = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
      sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:block/frame/default");
      capStart = new LaserData_BC8.LaserRow(sprite, 0, 0, 0, 0);
      start = null;
      middle = new LaserData_BC8.LaserRow[]{new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12)};
      end = new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 12);
      capEnd = new LaserData_BC8.LaserRow(sprite, 4, 4, 12, 12);
      FRAME_BOTTOM = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
      sprite = SpriteHolderRegistry.getHolder("buildcraftbuilders:block/quarry/drill");
      capStart = new LaserData_BC8.LaserRow(sprite, 6, 0, 10, 4);
      start = null;
      middle = new LaserData_BC8.LaserRow[]{new LaserData_BC8.LaserRow(sprite, 0, 0, 16, 4)};
      end = null;
      capEnd = new LaserData_BC8.LaserRow(sprite, 6, 0, 10, 4);
      DRILL = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
   }
}
