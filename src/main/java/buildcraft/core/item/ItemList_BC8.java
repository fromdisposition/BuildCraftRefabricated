package buildcraft.core.item;

import buildcraft.api.items.IList;
import buildcraft.core.list.ContainerList;
import buildcraft.core.list.ListOpenContext;
import buildcraft.lib.list.ListHandler;
import buildcraft.lib.misc.AdvancementUtil;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class ItemList_BC8 extends Item implements IList {
   private static final Identifier ADVANCEMENT = Identifier.parse("buildcraftcore:list");

   public ItemList_BC8(Properties properties) {
      super(properties);
   }

   private static CompoundTag getCustomTag(@Nonnull ItemStack stack) {
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      return customData == null ? new CompoundTag() : customData.copyTag();
   }

   private static void setCustomTag(@Nonnull ItemStack stack, CompoundTag tag) {
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
   }

   public static void updateModelData(@Nonnull ItemStack stack) {
      boolean hasItems = ListHandler.hasItems(stack);
      if (hasItems) {
         stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(1.0F), List.of(), List.of(), List.of()));
      } else {
         stack.remove(DataComponents.CUSTOM_MODEL_DATA);
      }
   }

   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      ListOpenContext.remember(player, hand);
      if (!level.isClientSide()) {
         AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
         if (player instanceof ServerPlayer serverPlayer) {
            final InteractionHand openHand = hand;
            serverPlayer.openMenu(new MenuProvider() {
               public Component getDisplayName() {
                  return Component.translatable("gui.buildcraft.list");
               }

               public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player p) {
                  return new ContainerList(containerId, playerInv, openHand);
               }
            });
         }
      }

      return InteractionResult.SUCCESS;
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
      super.appendHoverText(stack, context, display, tooltip, flag);
      String label = this.getLocationName(stack);
      if (label != null && !label.isEmpty()) {
         tooltip.accept(Component.literal(label).withStyle(ChatFormatting.ITALIC));
      }
   }

   @Override
   public boolean matches(@Nonnull ItemStack stackList, @Nonnull ItemStack item) {
      return ListHandler.matches(stackList, item);
   }

   @Override
   public String getLocationName(@Nonnull ItemStack stack) {
      CompoundTag tag = getCustomTag(stack);
      return tag.getString("label").orElse("");
   }

   @Override
   public boolean setLocationName(@Nonnull ItemStack stack, String name) {
      CompoundTag tag = getCustomTag(stack);
      tag.putString("label", name);
      setCustomTag(stack, tag);
      return true;
   }
}
