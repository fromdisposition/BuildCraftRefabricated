package buildcraft.robotics;

import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;

public class ImplRedstoneBoardRegistry extends RedstoneBoardRegistry {
   private static final class BoardFactory {
      RedstoneBoardNBT<?> boardNBT;
      long energyCost;
   }

   private final Map<String, BoardFactory> boards = new HashMap<>();
   private RedstoneBoardRobotNBT emptyRobotBoardNBT;

   @Override
   public void registerBoardType(RedstoneBoardNBT<?> redstoneBoardNBT, long energyCost) {
      BoardFactory factory = new BoardFactory();
      factory.boardNBT = redstoneBoardNBT;
      factory.energyCost = energyCost;
      this.boards.put(redstoneBoardNBT.getID(), factory);
   }

   @Override
   public void setEmptyRobotBoard(RedstoneBoardRobotNBT redstoneBoardNBT) {
      this.emptyRobotBoardNBT = redstoneBoardNBT;
   }

   @Override
   public RedstoneBoardRobotNBT getEmptyRobotBoard() {
      return this.emptyRobotBoardNBT;
   }

   @Override
   public RedstoneBoardNBT<?> getRedstoneBoard(CompoundTag nbt) {
      return this.getRedstoneBoard(nbt.getString("id").orElse(""));
   }

   @Override
   public RedstoneBoardNBT<?> getRedstoneBoard(String id) {
      BoardFactory factory = this.boards.get(id);
      return factory != null ? factory.boardNBT : this.emptyRobotBoardNBT;
   }

   @Override
   public Collection<RedstoneBoardNBT<?>> getAllBoardNBTs() {
      ArrayList<RedstoneBoardNBT<?>> result = new ArrayList<>();

      for (BoardFactory factory : this.boards.values()) {
         result.add(factory.boardNBT);
      }

      return result;
   }

   @Override
   public long getPowerCost(RedstoneBoardNBT<?> board) {
      BoardFactory factory = this.boards.get(board.getID());
      return factory != null ? factory.energyCost : 0L;
   }
}
