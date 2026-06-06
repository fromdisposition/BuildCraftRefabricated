package buildcraft.api.boards;

public interface IRedstoneBoard<T> {
   void updateBoard(T var1);

   RedstoneBoardNBT<?> getNBTHandler();
}
