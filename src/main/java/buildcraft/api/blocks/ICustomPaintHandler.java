package buildcraft.api.blocks;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public interface ICustomPaintHandler {
   InteractionResult attemptPaint(Level var1, BlockPos var2, BlockState var3, Vec3 var4, @Nullable Direction var5, @Nullable DyeColor var6);
}
