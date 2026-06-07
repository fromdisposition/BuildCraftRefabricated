package buildcraft.robotics;

import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.robotics.boards.BCBoardNBT;
import buildcraft.robotics.boards.RedstoneBoardRobotEmptyNBT;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;

public class ItemRedstoneBoard extends Item {
   private static final Map<String, Integer> BOARD_MODEL_INDEX = Map.of(
      "clean", 0,
      "green", 1,
      "blue", 2,
      "red", 3,
      "yellow", 4
   );

   public ItemRedstoneBoard(Properties properties) {
      super(properties);
   }

   public Component getName(ItemStack stack) {
      RedstoneBoardNBT<?> board = getBoardNBT(stack);
      return Component.translatable(this.getDescriptionId()).append(" (").append(board.getDisplayName()).append(")");
   }

   public static void appendTooltipLines(ItemRedstoneBoard item, ItemStack stack, TooltipFlag flag, List<Component> tooltip) {
      RedstoneBoardNBT<?> board = getBoardNBT(stack);
      List<String> lines = new java.util.ArrayList<>();
      board.addInformation(stack, null, lines, flag.isAdvanced());
      lines.forEach(line -> tooltip.add(Component.literal(line)));
   }

   public static ItemStack createStack(RedstoneBoardNBT<?> boardNBT) {
      ItemStack stack = new ItemStack(BCRoboticsItems.REDSTONE_BOARD);
      CompoundTag data = NBTUtilBC.getItemData(stack);
      boardNBT.createBoard(data);
      NBTUtilBC.setItemData(stack, data);
      applyModelData(stack, boardNBT);
      if (!(boardNBT instanceof RedstoneBoardRobotEmptyNBT)) {
         stack.set(DataComponents.MAX_STACK_SIZE, 1);
      }

      return stack;
   }

   public static RedstoneBoardNBT<?> getBoardNBT(ItemStack stack) {
      return getBoardNBT(getNBT(stack));
   }

   private static CompoundTag getNBT(ItemStack stack) {
      CompoundTag data = NBTUtilBC.getItemData(stack);
      if (!data.contains("id")) {
         RedstoneBoardRegistry.instance.getEmptyRobotBoard().createBoard(data);
         NBTUtilBC.setItemData(stack, data);
      }

      return data;
   }

   private static RedstoneBoardNBT<?> getBoardNBT(CompoundTag data) {
      return RedstoneBoardRegistry.instance.getRedstoneBoard(data);
   }

   private static void applyModelData(ItemStack stack, RedstoneBoardNBT<?> boardNBT) {
      String boardType = "clean";
      if (boardNBT instanceof BCBoardNBT bcBoard) {
         boardType = bcBoard.getBoardType();
      } else if (boardNBT instanceof RedstoneBoardRobotEmptyNBT) {
         boardType = "clean";
      }

      int index = BOARD_MODEL_INDEX.getOrDefault(boardType, 0);
      stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of((float)index), List.of(), List.of(), List.of()));
   }
}
