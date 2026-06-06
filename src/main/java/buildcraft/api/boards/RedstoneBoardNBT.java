package buildcraft.api.boards;

import java.util.List;
import java.util.Random;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class RedstoneBoardNBT<T> {
   private static Random rand = new Random();

   public abstract String getID();

   public abstract void addInformation(ItemStack var1, Player var2, List<String> var3, boolean var4);

   public abstract String getDisplayName();

   public abstract IRedstoneBoard<T> create(CompoundTag var1, T var2);

   public abstract String getItemModelLocation();

   public void createBoard(CompoundTag nbt) {
      nbt.putString("id", this.getID());
   }

   public int getParameterNumber(CompoundTag nbt) {
      return !nbt.contains("parameters") ? 0 : 0;
   }

   public float nextFloat(int difficulty) {
      return 1.0F - (float)Math.pow(rand.nextFloat(), 1.0F / difficulty);
   }
}
