package buildcraft.lib.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.AfterTranslucentFeatures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public final class AdvDebugRenderer {
   private AdvDebugRenderer() {
   }

   public static void register() {
      LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register((AfterTranslucentFeatures)context -> {
         BlockPos target = BCAdvDebugging.INSTANCE.getClientTarget();
         if (target != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null) {
               BlockEntity be = mc.level.getBlockEntity(target);
               if (!(be instanceof IAdvDebugTarget)) {
                  BCAdvDebugging.INSTANCE.clear();
               } else {
                  renderOptionalOverlay(be, context.poseStack(), context.levelState().cameraRenderState.pos);
               }
            }
         }
      });
   }

   private static void renderOptionalOverlay(BlockEntity be, PoseStack poseStack, Vec3 cameraPos) {
      try {
         Class<?> quarryClass = Class.forName("buildcraft.builders.tile.TileQuarry");
         Class<?> laserClass = Class.forName("buildcraft.silicon.tile.TileLaser");
         if (quarryClass.isInstance(be)) {
            Class<?> renderer = Class.forName("buildcraft.builders.client.render.AdvDebuggerQuarry");
            renderer.getMethod("render", quarryClass, poseStack.getClass(), BufferSource.class, Vec3.class)
               .invoke(null, be, poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), cameraPos);
         } else if (laserClass.isInstance(be)) {
            Class<?> renderer = Class.forName("buildcraft.silicon.client.render.AdvDebuggerLaser");
            renderer.getMethod("render", laserClass, poseStack.getClass(), BufferSource.class, Vec3.class)
               .invoke(null, be, poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), cameraPos);
         }
      } catch (ReflectiveOperationException var6) {
      }
   }
}
