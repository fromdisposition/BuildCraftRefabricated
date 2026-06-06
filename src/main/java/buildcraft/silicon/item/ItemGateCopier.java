package buildcraft.silicon.item;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class ItemGateCopier extends Item {
   private static final String NBT_DATA = "gate_data";

   public ItemGateCopier(Properties properties) {
      super(properties.stacksTo(1));
   }

   @Override
   @SuppressWarnings("deprecation")
   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
      super.appendHoverText(stack, context, display, tooltip, flag);
      if (getCopiedGateData(stack) != null) {
         tooltip.accept(Component.translatable("buildcraft.item.nonclean.usage", new Object[]{Component.keybind("key.sneak"), Component.keybind("key.use")}));
      }
   }

   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (level.isClientSide()) {
         return InteractionResult.PASS;
      } else {
         return (InteractionResult)(player.isShiftKeyDown() ? this.clearData(player, stack) : InteractionResult.PASS);
      }
   }

   private InteractionResult clearData(Player player, ItemStack stack) {
      if (getCopiedGateData(stack) == null) {
         return InteractionResult.PASS;
      }

      CompoundTag data = NBTUtilBC.getItemData(stack);
      data.remove("gate_data");
      if (data.isEmpty()) {
         stack.remove(DataComponents.CUSTOM_DATA);
      } else {
         NBTUtilBC.setItemData(stack, data);
      }

      updateModelData(stack);
      MessageUtil.sendOverlayMessage(player, Component.translatable("chat.gateCopier.dataCleared"));
      return InteractionResult.SUCCESS;
   }

   private static void updateModelData(ItemStack stack) {
      if (getCopiedGateData(stack) != null) {
         stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(1.0F), List.of(), List.of(), List.of()));
      } else {
         stack.remove(DataComponents.CUSTOM_MODEL_DATA);
      }
   }

   public static CompoundTag getCopiedGateData(ItemStack stack) {
      CompoundTag data = NBTUtilBC.getItemData(stack);
      return data.contains("gate_data") ? data.getCompound("gate_data").orElse(new CompoundTag()) : null;
   }

   public static void setCopiedGateData(ItemStack stack, CompoundTag nbt) {
      CompoundTag data = NBTUtilBC.getItemData(stack);
      data.put("gate_data", nbt);
      NBTUtilBC.setItemData(stack, data);
      updateModelData(stack);
   }
}
