package buildcraft.silicon.item;

import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;

import buildcraft.lib.misc.NBTUtilBC;

@SuppressWarnings("deprecation")
public class ItemGateCopier extends Item {
    private static final String NBT_DATA = "gate_data";

    public ItemGateCopier(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (getCopiedGateData(stack) != null) {

            tooltip.accept(Component.translatable("buildcraft.item.nonclean.usage",
                Component.keybind("key.sneak"), Component.keybind("key.use")));
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            return clearData(player, stack);
        }
        return InteractionResult.PASS;
    }

    private InteractionResult clearData(Player player, ItemStack stack) {
        if (getCopiedGateData(stack) == null) {
            return InteractionResult.PASS;
        }
        CompoundTag data = NBTUtilBC.getItemData(stack);
        data.remove(NBT_DATA);
        if (data.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            NBTUtilBC.setItemData(stack, data);
        }
        updateModelData(stack);
        buildcraft.lib.misc.MessageUtil.sendOverlayMessage(player,Component.translatable("chat.gateCopier.dataCleared"));
        return InteractionResult.SUCCESS;
    }

    private static void updateModelData(ItemStack stack) {
        if (getCopiedGateData(stack) != null) {
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
                List.of(1.0f), List.of(), List.of(), List.of()));
        } else {
            stack.remove(DataComponents.CUSTOM_MODEL_DATA);
        }
    }

    public static CompoundTag getCopiedGateData(ItemStack stack) {
        CompoundTag data = NBTUtilBC.getItemData(stack);
        if (data.contains(NBT_DATA)) {
            return data.getCompound(NBT_DATA).orElse(new CompoundTag());
        }
        return null;
    }

    public static void setCopiedGateData(ItemStack stack, CompoundTag nbt) {
        CompoundTag data = NBTUtilBC.getItemData(stack);
        data.put(NBT_DATA, nbt);
        NBTUtilBC.setItemData(stack, data);
        updateModelData(stack);
    }
}
