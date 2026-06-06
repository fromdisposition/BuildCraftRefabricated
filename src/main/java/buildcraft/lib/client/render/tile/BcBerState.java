package buildcraft.lib.client.render.tile;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BcBerState<T extends BlockEntity> extends BlockEntityRenderState {
   public T tile;
   public float partialTick;
   public int light;
}
