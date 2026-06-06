package buildcraft.builders.snapshot;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.template.TemplateApi;
import buildcraft.lib.misc.BlockUtil;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TemplateBuilder extends SnapshotBuilder<ITileForTemplateBuilder> {
   public TemplateBuilder(ITileForTemplateBuilder tile) {
      super(tile);
   }

   protected Template.BuildingInfo getBuildingInfo() {
      return this.tile.getTemplateBuildingInfo();
   }

   @Override
   protected boolean isAir(BlockPos blockPos) {
      return !this.getBuildingInfo().box.contains(blockPos)
         || !this.getBuildingInfo().getSnapshot().data.get(this.getBuildingInfo().getSnapshot().posToIndex(this.getBuildingInfo().fromWorld(blockPos)));
   }

   @Override
   protected boolean canPlace(BlockPos blockPos) {
      return isFillableSlot(this.tile.getWorldBC(), blockPos);
   }

   static boolean isFillableSlot(Level level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      return state.isAir() ? true : state.canBeReplaced() && state.getFluidState().isEmpty();
   }

   @Override
   protected boolean isReadyToPlace(BlockPos blockPos) {
      return true;
   }

   @Override
   protected boolean hasEnoughToPlaceItems(BlockPos blockPos) {
      return !this.tile.getInvResources().extract(null, 1, 1, true).isEmpty();
   }

   @Override
   protected List<ItemStack> getToPlaceItems(BlockPos blockPos) {
      return Collections.singletonList(this.tile.getInvResources().extract(null, 1, 1, false));
   }

   @Override
   protected boolean doPlaceTask(SnapshotBuilder<ITileForTemplateBuilder>.PlaceTask placeTask) {
      if (placeTask.items != null && !placeTask.items.isEmpty()) {
         Player fakePlayer = BuildCraftAPI.fakePlayerProvider
            .getFakePlayer((ServerLevel)this.tile.getWorldBC(), this.tile.getOwner(), this.tile.getBuilderPos());
         fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, placeTask.items.get(0));
         return !BlockUtil.canMachinePlace((ServerLevel)this.tile.getWorldBC(), placeTask.pos, this.tile.getOwner(), this.tile.getBuilderPos())
            ? false
            : TemplateApi.templateRegistry.handle(this.tile.getWorldBC(), placeTask.pos, fakePlayer, placeTask.items.get(0));
      } else {
         return false;
      }
   }

   @Override
   protected void cancelPlaceTask(SnapshotBuilder<ITileForTemplateBuilder>.PlaceTask placeTask) {
      super.cancelPlaceTask(placeTask);
      if (this.tile.getWorldBC() != null && !this.tile.getWorldBC().isClientSide() && placeTask.items != null && !placeTask.items.isEmpty()) {
         this.tile.getInvResources().insert(placeTask.items.get(0), false, false);
      }
   }

   @Override
   protected boolean isBlockCorrect(BlockPos blockPos) {
      return !this.isAir(blockPos) && !isFillableSlot(this.tile.getWorldBC(), blockPos);
   }
}
