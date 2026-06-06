package buildcraft.lib.gui.slot;

import java.util.function.IntFunction;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotDisplay extends Slot {
   private static final Container EMPTY_INVENTORY = new SimpleContainer(0);
   private final IntFunction<ItemStack> getter;
   private final int displayIndex;

   public SlotDisplay(IntFunction<ItemStack> getter, int displayIndex, int x, int y) {
      super(EMPTY_INVENTORY, displayIndex, x, y);
      this.getter = getter;
      this.displayIndex = displayIndex;
   }

   public ItemStack getItem() {
      return this.getter.apply(this.displayIndex);
   }

   public boolean mayPlace(ItemStack stack) {
      return false;
   }

   public boolean mayPickup(Player player) {
      return false;
   }

   public void set(ItemStack stack) {
   }

   public ItemStack remove(int amount) {
      return ItemStack.EMPTY;
   }

   public boolean hasItem() {
      return !this.getItem().isEmpty();
   }

   public int getMaxStackSize() {
      return 0;
   }

   public boolean isFake() {
      return true;
   }
}
