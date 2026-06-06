package buildcraft.builders.item;

import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.lib.misc.HashUtil;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;

public class ItemSnapshot extends Item {
   private final EnumSnapshotType snapshotType;
   private final boolean used;

   public ItemSnapshot(Properties properties, EnumSnapshotType snapshotType, boolean used) {
      super(properties);
      this.snapshotType = snapshotType;
      this.used = used;
   }

   public EnumSnapshotType getSnapshotType() {
      return this.snapshotType;
   }

   public boolean isUsed() {
      return this.used;
   }

   public ItemStack createUsedStack(Snapshot.Header header) {
      ItemStack stack = new ItemStack(this);
      CompoundTag tag = ((CustomData)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).copyTag();
      tag.put("header", header.serializeNBT());
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
      return stack;
   }

   public static Snapshot.Header getHeader(ItemStack stack) {
      if (stack.getItem() instanceof ItemSnapshot snapshotItem && snapshotItem.used) {
         CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
         if (customData != null) {
            CompoundTag nbt = customData.copyTag();
            if (nbt.contains("header")) {
               return new Snapshot.Header(nbt.getCompoundOrEmpty("header"));
            }
         }
      }

      return null;
   }

   @Override
   @SuppressWarnings("deprecation")
   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
      Snapshot.Header header = getHeader(stack);
      if (header == null) {
         tooltip.accept(Component.translatable("item.blueprint.blank").withStyle(ChatFormatting.GRAY));
      } else {
         tooltip.accept(Component.literal(header.name).withStyle(ChatFormatting.GRAY));
         if (flag.isAdvanced()) {
            tooltip.accept(Component.literal("Hash: " + HashUtil.convertHashToString(header.key.hash)).withStyle(ChatFormatting.GRAY));
            tooltip.accept(Component.literal("Date: " + header.created).withStyle(ChatFormatting.GRAY));
            tooltip.accept(Component.literal("Owner UUID: " + header.owner).withStyle(ChatFormatting.GRAY));
         }
      }
   }
}
