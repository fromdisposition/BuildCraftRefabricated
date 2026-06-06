package buildcraft.core.client;

import buildcraft.core.BCCore;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.fluids.SimpleFluidContent;
import buildcraft.lib.misc.FluidUtilBC;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public final class FluidShardTintSource implements ItemTintSource {
   public static final FluidShardTintSource INSTANCE = new FluidShardTintSource();
   public static final MapCodec<FluidShardTintSource> MAP_CODEC = MapCodec.unit(INSTANCE);

   private FluidShardTintSource() {
   }

   public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
      SimpleFluidContent content = (SimpleFluidContent)stack.getOrDefault(BCCore.FLUID_CONTENT, SimpleFluidContent.EMPTY);
      FluidStack fluid = content.copy();
      if (fluid.isEmpty()) {
         return -1;
      } else {
         return fluid.getFluid().isSame(Fluids.WATER) ? -12618012 : FluidUtilBC.getFluidColor(fluid);
      }
   }

   public MapCodec<? extends ItemTintSource> type() {
      return MAP_CODEC;
   }
}
