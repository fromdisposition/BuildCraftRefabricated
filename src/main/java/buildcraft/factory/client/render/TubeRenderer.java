package buildcraft.factory.client.render;

import buildcraft.factory.tile.TileMiner;
import buildcraft.factory.tile.TilePump;
import buildcraft.lib.client.render.laser.BcLaserRenderer;
import buildcraft.lib.client.render.laser.LaserBatch;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class TubeRenderer {
   private static final Set<TileMiner> ACTIVE_MINERS = Collections.newSetFromMap(new WeakHashMap<>());
   private static final LaserData_BC8.LaserType PUMP_TUBE;
   private static final LaserData_BC8.LaserType MINING_WELL_TUBE;

   public static void addMiner(TileMiner miner) {
      ACTIVE_MINERS.add(miner);
   }

   public static void removeMiner(TileMiner miner) {
      ACTIVE_MINERS.remove(miner);
   }

   public static void onRenderLevel(PoseStack poseStack, Vec3 cameraPos, float partialTicks) {
      if (!ACTIVE_MINERS.isEmpty()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && mc.level != null) {
            ACTIVE_MINERS.removeIf(minerx -> minerx.isRemoved() || minerx.getLevel() != mc.level);
            LaserBatch.begin();

            try {
               for (TileMiner miner : ACTIVE_MINERS) {
                  double length = miner.getLength(partialTicks);
                  if (!(length <= 0.0)) {
                     BlockPos pos = miner.getBlockPos();
                     Vec3 start = new Vec3(pos.getX() + 0.5, pos.getY() - 0.001, pos.getZ() + 0.5);
                     Vec3 end = new Vec3(pos.getX() + 0.5, pos.getY() - length, pos.getZ() + 0.5);
                     LaserData_BC8.LaserType type = miner instanceof TilePump ? PUMP_TUBE : MINING_WELL_TUBE;
                     LaserData_BC8 data = new LaserData_BC8(type, start, end, 0.0625, true, false, 0);
                     BcLaserRenderer.renderLaserStatic(poseStack, data, cameraPos);
                  }
               }
            } finally {
               LaserBatch.end();
            }
         }
      }
   }

   static {
      SpriteHolderRegistry.SpriteHolder sprite = SpriteHolderRegistry.getHolder("buildcraftfactory:block/pump/tube");
      LaserData_BC8.LaserRow cap = new LaserData_BC8.LaserRow(sprite, 0, 8, 8, 16);
      LaserData_BC8.LaserRow middle = new LaserData_BC8.LaserRow(sprite, 0, 0, 16, 8);
      PUMP_TUBE = new LaserData_BC8.LaserType(cap, middle, new LaserData_BC8.LaserRow[]{middle}, null, cap);
      sprite = SpriteHolderRegistry.getHolder("buildcraftfactory:block/mining_well/tube");
      cap = new LaserData_BC8.LaserRow(sprite, 0, 8, 8, 16);
      middle = new LaserData_BC8.LaserRow(sprite, 0, 0, 16, 8);
      MINING_WELL_TUBE = new LaserData_BC8.LaserType(cap, middle, new LaserData_BC8.LaserRow[]{middle}, null, cap);
   }
}
