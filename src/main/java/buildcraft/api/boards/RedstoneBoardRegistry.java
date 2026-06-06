package buildcraft.api.boards;

import java.util.Collection;
import net.minecraft.nbt.CompoundTag;

public abstract class RedstoneBoardRegistry {
   public static RedstoneBoardRegistry instance;

   public abstract void registerBoardType(RedstoneBoardNBT<?> var1, long var2);

   public abstract void setEmptyRobotBoard(RedstoneBoardRobotNBT var1);

   public abstract RedstoneBoardRobotNBT getEmptyRobotBoard();

   public abstract RedstoneBoardNBT<?> getRedstoneBoard(CompoundTag var1);

   public abstract RedstoneBoardNBT<?> getRedstoneBoard(String var1);

   public abstract Collection<RedstoneBoardNBT<?>> getAllBoardNBTs();

   public abstract long getPowerCost(RedstoneBoardNBT<?> var1);
}
