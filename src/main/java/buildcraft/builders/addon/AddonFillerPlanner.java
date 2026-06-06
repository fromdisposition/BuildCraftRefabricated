package buildcraft.builders.addon;

import buildcraft.api.core.IBox;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;
import buildcraft.builders.BCBuildersGuis;
import buildcraft.builders.BCBuildersSprites;
import buildcraft.builders.filler.FillerType;
import buildcraft.builders.filler.FillerUtil;
import buildcraft.builders.snapshot.Template;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.AddonDefaultRenderer;
import buildcraft.core.marker.volume.IFastAddonRenderer;
import buildcraft.core.marker.volume.ISingleAddon;
import buildcraft.lib.statement.FullStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AddonFillerPlanner extends Addon implements ISingleAddon, IFillerStatementContainer {
   public static final int MAX_PREVIEW_BLOCKS = 4096;
   public final FullStatement<IFillerPattern> patternStatement = new FullStatement<>(FillerType.INSTANCE, 4, null);
   public boolean inverted;
   @Nullable
   public Template.BuildingInfo buildingInfo;
   private List<BlockPos> cachedPreviewPositions = Collections.emptyList();

   public List<BlockPos> getCachedPreviewPositions() {
      return this.cachedPreviewPositions;
   }

   public void updateBuildingInfo() {
      this.buildingInfo = FillerUtil.createBuildingInfo(
         this,
         this.patternStatement,
         IntStream.range(0, this.patternStatement.maxParams).mapToObj(this.patternStatement::get).toArray(IStatementParameter[]::new),
         this.inverted
      );
      this.cachedPreviewPositions = this.rebuildPreviewCache();
   }

   private List<BlockPos> rebuildPreviewCache() {
      if (this.buildingInfo != null && this.volumeBox != null && this.volumeBox.world != null) {
         List<BlockPos> positions = new ArrayList<>();

         for (BlockPos blockPos : BlockPos.betweenClosed(this.buildingInfo.box.min(), this.buildingInfo.box.max())) {
            if (this.buildingInfo.getSnapshot().data.get(this.buildingInfo.getSnapshot().posToIndex(this.buildingInfo.fromWorld(blockPos)))
               && this.volumeBox.world.isEmptyBlock(blockPos)) {
               positions.add(blockPos.immutable());
               if (positions.size() >= 4096) {
                  break;
               }
            }
         }

         return Collections.unmodifiableList(positions);
      } else {
         return Collections.emptyList();
      }
   }

   @Override
   public void onVolumeBoxSizeChange() {
      this.updateBuildingInfo();
   }

   @Override
   public IFastAddonRenderer<AddonFillerPlanner> getRenderer() {
      return new AddonDefaultRenderer<AddonFillerPlanner>(BCBuildersSprites.FILLER_PLANNER).then(new AddonRendererFillerPlanner());
   }

   @Override
   public void onAdded() {
      super.onAdded();
      this.updateBuildingInfo();
   }

   @Override
   public void postReadFromNbt() {
      super.postReadFromNbt();
      this.updateBuildingInfo();
   }

   @Override
   public void onPlayerRightClick(Player player) {
      super.onPlayerRightClick(player);
      BCBuildersGuis.openFillerPlannerGUI(player, this);
   }

   @Override
   public CompoundTag writeToNBT(CompoundTag nbt) {
      nbt.put("patternStatement", this.patternStatement.writeToNbt());
      nbt.putBoolean("inverted", this.inverted);
      return nbt;
   }

   @Override
   public void readFromNBT(CompoundTag nbt) {
      this.patternStatement.readFromNbt(nbt.getCompound("patternStatement").orElse(new CompoundTag()));
      this.inverted = nbt.getBoolean("inverted").orElse(false);
   }

   @Override
   public BlockEntity getNeighbourTile(Direction side) {
      return null;
   }

   @Override
   public BlockEntity getTile() {
      return null;
   }

   @Override
   public Level getFillerWorld() {
      return this.volumeBox.world;
   }

   @Override
   public boolean hasBox() {
      return true;
   }

   @Override
   public IBox getBox() {
      return this.volumeBox.box;
   }

   @Override
   public void setPattern(IFillerPattern pattern, IStatementParameter[] params) {
      this.patternStatement.set(pattern, params);
      this.updateBuildingInfo();
   }
}
