package buildcraft.lib.client.render.tile;

import buildcraft.lib.client.render.LightUtil;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class BcBlockEntityRenderer<T extends BlockEntity, S extends BcBerState<T>> implements BlockEntityRenderer<T, S> {
   public void extractRenderState(T blockEntity, S state, float partialTick, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
      BlockPos pos = blockEntity.getBlockPos();
      state.tile = blockEntity;
      state.blockPos = pos;
      state.partialTick = partialTick;
      Level level = blockEntity.getLevel();
      state.light = level == null ? 15728880 : LightUtil.getLightCoords(level, pos);
      this.extract(blockEntity, state, partialTick);
   }

   protected void extract(T tile, S state, float partialTick) {
   }
}
