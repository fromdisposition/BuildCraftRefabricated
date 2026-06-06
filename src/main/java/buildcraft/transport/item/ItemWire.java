package buildcraft.transport.item;

import buildcraft.lib.misc.ColourUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class ItemWire extends Item {
   private final DyeColor color;

   public ItemWire(Properties properties, DyeColor color) {
      super(properties);
      this.color = color;
   }

   public DyeColor getColor() {
      return this.color;
   }

   public Component getName(ItemStack stack) {
      Component colorName = Component.literal(ColourUtil.getTextFullTooltip(this.color));
      return Component.translatable("item.buildcrafttransport.wire", new Object[]{colorName});
   }
}
