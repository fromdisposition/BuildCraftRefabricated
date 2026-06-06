package buildcraft.energy.blocks;

import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pipe.PipeFlowType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

final class EnginePipeInteraction {
   private EnginePipeInteraction() {
   }

   @Nullable
   static InteractionResult tryPlacePipe(
      IItemPipe pipe,
      ItemStack stack,
      Level level,
      Player player,
      InteractionHand hand,
      BlockHitResult hitResult,
      PipeFlowType fullFamily,
      PipeFlowType extractionOnlyFamily
   ) {
      if (!accepts(pipe.getDefinition(), fullFamily, extractionOnlyFamily)) {
         return null;
      } else if (stack.getItem() instanceof BlockItem blockItem) {
         InteractionResult result = blockItem.place(new BlockPlaceContext(level, player, hand, stack, hitResult));
         return result.consumesAction() ? result : null;
      } else {
         return null;
      }
   }

   static boolean accepts(PipeDefinition def, PipeFlowType fullFamily, PipeFlowType extractionOnlyFamily) {
      return def.flowType == fullFamily ? true : def.flowType == extractionOnlyFamily && isExtractionPipe(def);
   }

   static boolean isExtractionPipe(PipeDefinition def) {
      String id = def.identifier;
      if (id == null) {
         return false;
      }

      int colon = id.indexOf(58);
      String path = colon >= 0 ? id.substring(colon + 1) : id;
      return path.startsWith("wood_") || path.startsWith("diamond_wood_");
   }
}
