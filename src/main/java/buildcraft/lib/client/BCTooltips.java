package buildcraft.lib.client;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

public class BCTooltips {

    private static final Map<net.minecraft.world.item.Item, String> TOOLTIPS = new IdentityHashMap<>();

    private static final Set<net.minecraft.world.item.Item> DEV_ONLY =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private BCTooltips() {}

    public static void init() {
        ItemTooltipCallback.EVENT.register((stack, ctx, type, lines) -> {
            String key = TOOLTIPS.get(stack.getItem());
            if (key != null) {
                String resolved = key;
                if (net.minecraft.client.resources.language.I18n.exists(resolved)) {
                    String translated = net.minecraft.client.resources.language.I18n.get(resolved);
                    for (String line : translated.split("\n")) {
                        lines.add(Component.literal(line).withStyle(ChatFormatting.GRAY));
                    }
                } else {
                    lines.add(Component.translatable(resolved).withStyle(ChatFormatting.GRAY));
                }
            }
            if (DEV_ONLY.contains(stack.getItem())) {
                lines.add(Component.translatable("tip.dev_only").withStyle(ChatFormatting.RED));
            }
        });
    }

    public static void addTooltip(ItemLike item, String translationKey) {
        TOOLTIPS.put(item.asItem(), translationKey);
    }

    public static void markDevOnly(ItemLike item) {
        DEV_ONLY.add(item.asItem());
    }
}
