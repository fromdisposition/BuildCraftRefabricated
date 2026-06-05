package buildcraft.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import buildcraft.lib.BCLib;

public class SoundTestCommand {

    private static final String[] SOUNDS = {
        "block.tripwire.attach",
        "block.tripwire.detach",
        "block.tripwire.click_on",
        "block.tripwire.click_off",
        "block.lever.click",
        "block.comparator.click",
        "block.dispenser.fail",
        "block.copper_bulb.turn_on",
        "block.copper_bulb.turn_off",
        "block.bamboo_wood_button.click_on",
        "block.note_block.hat",
        "block.note_block.pling",
        "ui.button.click",
        "ui.toast.in",
        "ui.toast.out",
        "entity.experience_orb.pickup",
        "entity.item.pickup",
    };

    private static final float[] PITCHES = { 0.5f, 0.7f, 1.0f, 1.3f, 1.5f, 1.8f, 2.0f };

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            if (!BCLib.DEV) return;
            register(dispatcher);
        });
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bcsoundtest")
            .executes(SoundTestCommand::printMenu)
            .then(Commands.argument("sound", IdentifierArgument.id())
                .executes(ctx -> play(ctx, 1.0f, 1.0f))
                .then(Commands.argument("pitch", FloatArgumentType.floatArg(0.0f, 2.0f))
                    .executes(ctx -> play(ctx, FloatArgumentType.getFloat(ctx, "pitch"), 1.0f))
                    .then(Commands.argument("volume", FloatArgumentType.floatArg(0.0f, 4.0f))
                        .executes(ctx -> play(ctx,
                            FloatArgumentType.getFloat(ctx, "pitch"),
                            FloatArgumentType.getFloat(ctx, "volume")))
                    )
                )
            )
        );
    }

    private static int play(CommandContext<CommandSourceStack> ctx, float pitch, float volume) {
        CommandSourceStack source = ctx.getSource();
        Identifier soundId = IdentifierArgument.getId(ctx, "sound");
        SoundEvent event = BuiltInRegistries.SOUND_EVENT.getValue(soundId);
        if (event == null) {
            source.sendFailure(Component.literal("Unknown sound: " + soundId)
                .withStyle(ChatFormatting.RED));
            return 0;
        }
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            source.sendFailure(Component.literal("/bcsoundtest must be run by a player"));
            return 0;
        }
        BlockPos pos = player.blockPosition();

        player.level().playSound(null, pos, event, SoundSource.BLOCKS, volume, pitch);
        source.sendSuccess(() -> Component.literal(
                String.format("▶ %s @ pitch %.2f, volume %.2f", soundId, pitch, volume))
            .withStyle(ChatFormatting.GRAY), false);
        return 1;
    }

    private static int printMenu(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        source.sendSystemMessage(Component.literal("=== BuildCraft Sound Test ===")
            .withStyle(ChatFormatting.GOLD));
        source.sendSystemMessage(Component.literal(
                "Click a pitch button below to play that sound. Each row is one sound ID.")
            .withStyle(ChatFormatting.GRAY));
        source.sendSystemMessage(Component.literal(
                "Custom: /bcsoundtest <sound_id> [pitch 0-2] [volume 0-4]")
            .withStyle(ChatFormatting.DARK_GRAY));

        for (String sound : SOUNDS) {
            MutableComponent line = Component.literal("")
                .append(Component.literal(padRight(sound, 32)).withStyle(ChatFormatting.AQUA));
            for (float pitch : PITCHES) {
                String cmd = "/bcsoundtest " + sound + " " + pitch;
                MutableComponent button = Component.literal("[" + formatPitch(pitch) + "]")
                    .withStyle(Style.EMPTY
                        .withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent.RunCommand(cmd))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal(cmd))));
                line.append(Component.literal(" ")).append(button);
            }
            source.sendSystemMessage(line);
        }

        source.sendSystemMessage(Component.literal(
                "Tip: pair the chosen sound with the wrench rotation paths in the engine blocks.")
            .withStyle(ChatFormatting.DARK_GRAY));
        return 1;
    }

    private static String padRight(String s, int width) {
        if (s.length() >= width) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < width) sb.append(' ');
        return sb.toString();
    }

    private static String formatPitch(float pitch) {

        String s = String.format("%.2f", pitch);
        if (s.endsWith("0") && s.contains(".")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }
}
