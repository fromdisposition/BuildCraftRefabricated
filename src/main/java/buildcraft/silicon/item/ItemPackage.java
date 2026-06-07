package buildcraft.silicon.item;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.silicon.BCSiliconEntities;
import buildcraft.silicon.entity.EntityPackage;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemPackage extends Item {
   public static final int SLOTS = 9;
   private static final String KEY_PREFIX = "item";

   public ItemPackage(Properties properties) {
      super(properties);
   }

   @Override
   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!hasContents(stack)) {
         return InteractionResult.PASS;
      }

      if (!level.isClientSide()) {
         EntityPackage entity = new EntityPackage(BCSiliconEntities.PACKAGE, level);
         entity.setPackage(stack.copyWithCount(1));
         entity.shootFrom(player, 1.5F);
         level.addFreshEntity(entity);
         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }
      }

      return InteractionResult.SUCCESS;
   }

   public static boolean isPackage(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() instanceof ItemPackage;
   }

   public static ItemStack getStack(ItemStack pkg, int slot) {
      CompoundTag data = NBTUtilBC.getItemData(pkg);
      Tag payload = data.get(KEY_PREFIX + slot);
      if (payload == null) {
         return ItemStack.EMPTY;
      }

      return ItemStack.CODEC.parse(NBTUtilBC.registryAwareOps(), payload).resultOrPartial().orElse(ItemStack.EMPTY);
   }

   public static void setStack(ItemStack pkg, int slot, ItemStack content) {
      CompoundTag data = NBTUtilBC.getItemData(pkg);
      if (content.isEmpty()) {
         data.remove(KEY_PREFIX + slot);
      } else {
         ItemStack.CODEC
            .encodeStart(NBTUtilBC.registryAwareOps(), content)
            .resultOrPartial()
            .ifPresent(payload -> data.put(KEY_PREFIX + slot, payload));
      }

      NBTUtilBC.setItemData(pkg, data);
   }

   public static int getContentCount(ItemStack pkg) {
      CompoundTag data = NBTUtilBC.getItemData(pkg);
      int count = 0;

      for (int i = 0; i < SLOTS; i++) {
         if (data.contains(KEY_PREFIX + i)) {
            count++;
         }
      }

      return count;
   }

   public static boolean hasContents(ItemStack pkg) {
      return getContentCount(pkg) > 0;
   }

   public static void appendTooltipLines(ItemPackage item, ItemStack stack, TooltipFlag flag, List<Component> tooltip) {
      for (int i = 0; i < SLOTS; i++) {
         ItemStack content = getStack(stack, i);
         if (!content.isEmpty()) {
            tooltip.add(content.getHoverName().copy().append(" x" + content.getCount()));
         }
      }
   }
}
