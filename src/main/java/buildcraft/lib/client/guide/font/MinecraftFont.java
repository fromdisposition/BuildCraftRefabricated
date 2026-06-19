/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.font;

import buildcraft.lib.gui.BCGraphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

public enum MinecraftFont implements IFontRenderer {
   INSTANCE;

   private static BCGraphics currentGraphics;

   public static void setGuiGraphics(BCGraphics graphics) {
      currentGraphics = graphics;
   }

   private static Font getFontRenderer() {
      return Minecraft.getInstance().font;
   }

   @Override
   public int getStringWidth(String text) {
      return getFontRenderer().width(text);
   }

   @Override
   public int getFontHeight(String text) {
      return this.getMaxFontHeight();
   }

   @Override
   public int getMaxFontHeight() {
      return 9;
   }

   @Override
   public int drawString(String text, int x, int y, int colour, boolean shadow, boolean centered, float scale) {
      if (currentGraphics == null) {
         return (int)(this.getStringWidth(text) * scale);
      }

      Font font = getFontRenderer();
      int width = (int)(font.width(text) * scale);
      int drawX = x;
      if (centered) {
         drawX = x - width / 2;
      }

      if ((colour & 0xFF000000) == 0) {
         colour |= -16777216;
      }

      currentGraphics.text(font, text, drawX, y, colour, shadow);
      return width;
   }

   @Override
   public List<String> wrapString(String text, int maxWidth, boolean shadow, float scale) {
      Font font = getFontRenderer();
      int scaledWidth = (int)(maxWidth / scale);
      List<FormattedCharSequence> wrapped = font.split(Component.literal(text), scaledWidth);
      List<String> result = new ArrayList<>(wrapped.size());

      for (FormattedCharSequence seq : wrapped) {
         StringBuilder sb = new StringBuilder();
         Style[] last = new Style[]{Style.EMPTY};
         seq.accept((index, style, codePoint) -> {
            if (!stylesEquivalent(style, last[0])) {
               appendLegacyCodes(sb, style);
               last[0] = style;
            }

            sb.appendCodePoint(codePoint);
            return true;
         });
         result.add(sb.toString());
      }

      return result;
   }

   private static boolean stylesEquivalent(Style a, Style b) {
      return Objects.equals(a.getColor(), b.getColor())
         && a.isBold() == b.isBold()
         && a.isItalic() == b.isItalic()
         && a.isUnderlined() == b.isUnderlined()
         && a.isStrikethrough() == b.isStrikethrough()
         && a.isObfuscated() == b.isObfuscated();
   }

   //? if >= 26.2 {
   /*private static void appendLegacyCodes(StringBuilder sb, Style style) {
      sb.append(ChatFormatting.PREFIX_CODE).append('r');
      TextColor color = style.getColor();
      if (color != null) {
         for (char c : "0123456789abcdef".toCharArray()) {
            ChatFormatting fmt = ChatFormatting.getByCode(c);
            if (fmt != null && color.equals(TextColor.fromLegacyFormat(fmt))) {
               sb.append(ChatFormatting.PREFIX_CODE).append(c);
               break;
            }
         }
      }
      if (style.isBold()) sb.append(ChatFormatting.PREFIX_CODE).append('l');
      if (style.isItalic()) sb.append(ChatFormatting.PREFIX_CODE).append('o');
      if (style.isUnderlined()) sb.append(ChatFormatting.PREFIX_CODE).append('n');
      if (style.isStrikethrough()) sb.append(ChatFormatting.PREFIX_CODE).append('m');
      if (style.isObfuscated()) sb.append(ChatFormatting.PREFIX_CODE).append('k');
   }
   *///?} else {
   private static void appendLegacyCodes(StringBuilder sb, Style style) {
      sb.append('§').append(ChatFormatting.RESET.getChar());
      TextColor color = style.getColor();
      if (color != null) {
         for (ChatFormatting fmt : ChatFormatting.values()) {
            if (fmt.isColor() && fmt.getColor() != null && fmt.getColor() == color.getValue()) {
               sb.append('§').append(fmt.getChar());
               break;
            }
         }
      }

      if (style.isBold()) {
         sb.append('§').append(ChatFormatting.BOLD.getChar());
      }

      if (style.isItalic()) {
         sb.append('§').append(ChatFormatting.ITALIC.getChar());
      }

      if (style.isUnderlined()) {
         sb.append('§').append(ChatFormatting.UNDERLINE.getChar());
      }

      if (style.isStrikethrough()) {
         sb.append('§').append(ChatFormatting.STRIKETHROUGH.getChar());
      }

      if (style.isObfuscated()) {
         sb.append('§').append(ChatFormatting.OBFUSCATED.getChar());
      }
   }
   //?}
}
