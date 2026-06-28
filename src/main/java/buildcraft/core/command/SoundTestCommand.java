/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.command;

import buildcraft.lib.BCLib;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
//? if >= 1.21.10 {
import net.minecraft.network.chat.ClickEvent.RunCommand;
import net.minecraft.network.chat.HoverEvent.ShowText;
//?}
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class SoundTestCommand {
   private static final String[] SOUNDS = new String[]{
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
      "entity.item.pickup"
   };
   private static final float[] PITCHES = new float[]{0.5F, 0.7F, 1.0F, 1.3F, 1.5F, 1.8F, 2.0F};

   public static void init() {
      CommandRegistrationCallback.EVENT.register((CommandRegistrationCallback)(dispatcher, registryAccess, environment) -> {
         if (BCLib.DEV) {
            register(dispatcher);
         }
      });
   }

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         Commands.literal("bcsoundtest")
            .executes(SoundTestCommand::printMenu)
            .then(
               Commands.argument("sound", IdentifierArgument.id())
                  .executes(ctx -> play(ctx, 1.0F, 1.0F))
                  .then(
                     Commands.argument("pitch", FloatArgumentType.floatArg(0.0F, 2.0F))
                        .executes(ctx -> play(ctx, FloatArgumentType.getFloat(ctx, "pitch"), 1.0F))
                        .then(
                           Commands.argument("volume", FloatArgumentType.floatArg(0.0F, 4.0F))
                              .executes(ctx -> play(ctx, FloatArgumentType.getFloat(ctx, "pitch"), FloatArgumentType.getFloat(ctx, "volume")))
                        )
                  )
            )
      );
   }

   private static int play(CommandContext<CommandSourceStack> ctx, float pitch, float volume) {
      CommandSourceStack source = (CommandSourceStack)ctx.getSource();
      Identifier soundId = IdentifierArgument.getId(ctx, "sound");
      //? if >= 1.21.10 {
      SoundEvent event = (SoundEvent)BuiltInRegistries.SOUND_EVENT.getValue(soundId);
      //?} else {
      /*SoundEvent event = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(soundId);
      *///?}
      if (event == null) {
         source.sendFailure(Component.literal("Unknown sound: " + soundId).withStyle(ChatFormatting.RED));
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
      source.sendSuccess(() -> Component.literal(String.format("▶ %s @ pitch %.2f, volume %.2f", soundId, pitch, volume)).withStyle(ChatFormatting.GRAY), false);
      return 1;
   }

   private static int printMenu(CommandContext<CommandSourceStack> ctx) {
      CommandSourceStack source = (CommandSourceStack)ctx.getSource();
      source.sendSystemMessage(Component.literal("=== BuildCraft Sound Test ===").withStyle(ChatFormatting.GOLD));
      source.sendSystemMessage(Component.literal("Click a pitch button below to play that sound. Each row is one sound ID.").withStyle(ChatFormatting.GRAY));
      source.sendSystemMessage(Component.literal("Custom: /bcsoundtest <sound_id> [pitch 0-2] [volume 0-4]").withStyle(ChatFormatting.DARK_GRAY));

      for (String sound : SOUNDS) {
         MutableComponent line = Component.literal("").append(Component.literal(padRight(sound, 32)).withStyle(ChatFormatting.AQUA));

         for (float pitch : PITCHES) {
            String cmd = "/bcsoundtest " + sound + " " + pitch;
            //? if >= 1.21.10 {
            MutableComponent button = Component.literal("[" + formatPitch(pitch) + "]")
               .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withClickEvent(new RunCommand(cmd)).withHoverEvent(new ShowText(Component.literal(cmd))));
            //?} else {
            /*MutableComponent button = Component.literal("[" + formatPitch(pitch) + "]")
               .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)
                  .withClickEvent(new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND, cmd))
                  .withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal(cmd))));
            *///?}
            line.append(Component.literal(" ")).append(button);
         }

         source.sendSystemMessage(line);
      }

      source.sendSystemMessage(
         Component.literal("Tip: pair the chosen sound with the wrench rotation paths in the engine blocks.").withStyle(ChatFormatting.DARK_GRAY)
      );
      return 1;
   }

   private static String padRight(String s, int width) {
      if (s.length() >= width) {
         return s;
      }

      StringBuilder sb = new StringBuilder(s);

      while (sb.length() < width) {
         sb.append(' ');
      }

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
