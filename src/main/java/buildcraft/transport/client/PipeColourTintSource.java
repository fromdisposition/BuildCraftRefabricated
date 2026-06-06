package buildcraft.transport.client;

import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.BCTransportItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class PipeColourTintSource implements ItemTintSource {
   public static final PipeColourTintSource INSTANCE = new PipeColourTintSource();
   public static final MapCodec<PipeColourTintSource> MAP_CODEC = MapCodec.unit(INSTANCE);
   private static final int OVERLAY_ALPHA = 76;

   private PipeColourTintSource() {
   }

   public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
      DyeColor col = (DyeColor)stack.get(BCTransportItems.PIPE_COLOUR);
      return col != null ? 1275068416 | ColourUtil.getLightHex(col) : 1291845631;
   }

   public MapCodec<? extends ItemTintSource> type() {
      return MAP_CODEC;
   }
}
