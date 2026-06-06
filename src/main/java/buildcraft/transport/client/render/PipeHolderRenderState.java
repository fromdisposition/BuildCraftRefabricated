package buildcraft.transport.client.render;

import buildcraft.transport.tile.TilePipeHolder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

public class PipeHolderRenderState extends BlockEntityRenderState {
   public TilePipeHolder pipe;
   public float partialTick;
   public final List<PipeHolderRenderState.ItemRenderEntry> itemEntries = new ArrayList<>();
   private final List<ItemStackRenderState> itemStatePool = new ArrayList<>();
   private int itemStatePoolUsed;

   public void beginItemExtraction() {
      this.itemEntries.clear();
      this.itemStatePoolUsed = 0;
   }

   public ItemStackRenderState acquireItemState() {
      if (this.itemStatePoolUsed >= this.itemStatePool.size()) {
         this.itemStatePool.add(new ItemStackRenderState());
      }

      return this.itemStatePool.get(this.itemStatePoolUsed++);
   }

   public static class ItemRenderEntry {
      public final ItemStackRenderState renderState;
      public final double posX;
      public final double posY;
      public final double posZ;
      public final Direction direction;
      public final DyeColor colour;
      public final int stackCount;

      public ItemRenderEntry(ItemStackRenderState renderState, double posX, double posY, double posZ, Direction direction, DyeColor colour, int stackCount) {
         this.renderState = renderState;
         this.posX = posX;
         this.posY = posY;
         this.posZ = posZ;
         this.direction = direction;
         this.colour = colour;
         this.stackCount = stackCount;
      }
   }
}
