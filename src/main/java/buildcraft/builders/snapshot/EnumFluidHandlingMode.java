package buildcraft.builders.snapshot;

import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum EnumFluidHandlingMode {
   NO_REPLACE(() -> new ItemStack(Items.BARRIER), "gui.buildcraft.builder.fluidmode.no_replace"),
   REPLACE(() -> new ItemStack(Items.BRICKS), "gui.buildcraft.builder.fluidmode.replace"),
   CLEAR(() -> new ItemStack(Items.BUCKET), "gui.buildcraft.builder.fluidmode.clear");

   private final Supplier<ItemStack> iconSupplier;
   private final String tooltipKey;

   EnumFluidHandlingMode(Supplier<ItemStack> iconSupplier, String tooltipKey) {
      this.iconSupplier = iconSupplier;
      this.tooltipKey = tooltipKey;
   }

   public EnumFluidHandlingMode next() {
      return values()[(this.ordinal() + 1) % values().length];
   }

   public ItemStack icon() {
      return this.iconSupplier.get();
   }

   public String tooltipKey() {
      return this.tooltipKey;
   }

   public static EnumFluidHandlingMode fromOrdinal(int ord) {
      return ord >= 0 && ord < values().length ? values()[ord] : NO_REPLACE;
   }
}
