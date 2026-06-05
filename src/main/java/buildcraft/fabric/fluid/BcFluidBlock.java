package buildcraft.fabric.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.Vec3;

public class BcFluidBlock extends LiquidBlock {
    private final boolean sticky;

    public BcFluidBlock(FlowingFluid fluid, Properties properties, boolean sticky) {
        super(fluid, properties);
        this.sticky = sticky;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
            InsideBlockEffectApplier effectApplier, boolean isSubmerged) {
        super.entityInside(state, level, pos, entity, effectApplier, isSubmerged);
        if (sticky) {
            entity.makeStuckInBlock(state, new Vec3(0.25D, 0.05D, 0.25D));
        }
    }
}
