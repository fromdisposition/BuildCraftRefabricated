package buildcraft.silicon.client.render;

import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.fabric.client.event.RenderLevelStageEvent;
import buildcraft.lib.client.render.laser.BcLaserRenderer;
import buildcraft.lib.client.render.laser.LaserBatch;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.silicon.block.BlockLaser;
import buildcraft.silicon.tile.TileLaser;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class RenderLaser {
   private static final int MAX_POWER = BuildCraftLaserManager.POWERS.length - 1;
   private static final Set<TileLaser> ACTIVE_LASERS = Collections.newSetFromMap(new WeakHashMap<>());

   public static void addLaser(TileLaser laser) {
      ACTIVE_LASERS.add(laser);
   }

   public static void removeLaser(TileLaser laser) {
      ACTIVE_LASERS.remove(laser);
   }

   public static int getActiveCount() {
      return ACTIVE_LASERS.size();
   }

   public static void onRenderLevel(RenderLevelStageEvent.AfterTranslucentBlocks event) {
      if (!ACTIVE_LASERS.isEmpty()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && mc.level != null) {
            ACTIVE_LASERS.removeIf(laserx -> laserx.isRemoved() || laserx.getLevel() != mc.level);
            PoseStack poseStack = event.getPoseStack();
            Vec3 cameraPos = event.getLevelRenderState().cameraRenderState.pos;
            LaserBatch.begin();

            try {
               for (TileLaser laser : ACTIVE_LASERS) {
                  Vec3 target = laser.laserPos;
                  if (target != null) {
                     long avg = laser.getAverageClient();
                     if (avg > 200000L) {
                        avg += 200000L;
                        Direction side = (Direction)laser.getBlockState().getValue(BlockLaser.FACING);
                        Vec3 offset = new Vec3(0.5, 0.5, 0.5).add(Vec3.atLowerCornerOf(side.getUnitVec3i()).scale(0.25));
                        Vec3 start = Vec3.atLowerCornerOf(laser.getBlockPos()).add(offset);
                        int index = (int)(avg * MAX_POWER / laser.getMaxPowerPerTick());
                        if (index > MAX_POWER) {
                           index = MAX_POWER;
                        }

                        LaserData_BC8 data = new LaserData_BC8(BuildCraftLaserManager.POWERS[index], start, target, 0.0625, false, false, 15);
                        BcLaserRenderer.renderLaserStatic(poseStack, data, cameraPos);
                     }
                  }
               }
            } finally {
               LaserBatch.end();
            }
         }
      }
   }
}
