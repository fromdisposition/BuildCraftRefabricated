/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.node;

import buildcraft.lib.client.guide.font.IFontRenderer;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
//? if >= 26.2 {
import net.minecraft.network.chat.TextColor;
//?}

public class FormatString {
   private static final String WORD_GAP = " \n\t";
   private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
   public final FormatSegment[] segments;
   private final String unformatted;
   private final String formatted;

   public FormatString(FormatSegment[] segments) {
      this.segments = segments;
      String s = "";
      String sf = "";

      for (FormatSegment seg : segments) {
         s = s + seg.text;
         sf = sf + seg.toFormatString();
      }

      this.unformatted = s;
      this.formatted = sf;
   }

   public String getFormatted() {
      return this.formatted;
   }

   public String getUnformatted() {
      return this.unformatted;
   }

   public static FormatString split(String formattedText) {
      List<FormatSegment> segments = new ArrayList<>();
      ChatFormatting lastColour = null;
      Set<ChatFormatting> lastMisc = EnumSet.noneOf(ChatFormatting.class);
      int lastEnd = 0;

      for (Matcher matcher = FORMATTING_CODE_PATTERN.matcher(formattedText); matcher.find(); lastEnd = matcher.end()) {
         int start = matcher.start();
         if (start != lastEnd) {
            String subText = formattedText.substring(lastEnd, start);
            ImmutableSet<ChatFormatting> miscCopy = ImmutableSet.copyOf(lastMisc);
            segments.add(new FormatSegment(subText, lastColour, miscCopy));
         }

         String matched = matcher.group();
         ChatFormatting format = null;

         for (ChatFormatting f : ChatFormatting.values()) {
            if (f.toString().equals(matched)) {
               format = f;
               break;
            }
         }

         if (format != null) {
            if (format == ChatFormatting.RESET) {
               lastColour = null;
               lastMisc.clear();
            //? if >= 26.2 {
            } else if (TextColor.fromLegacyFormat(format) != null) {
            //?} else {
            /*} else if (format.isColor()) {
            *///?}
               lastColour = format;
            } else {
               lastMisc.add(format);
            }
         }
      }

      if (lastEnd != formattedText.length()) {
         String subText = formattedText.substring(lastEnd);
         ImmutableSet<ChatFormatting> miscCopy = ImmutableSet.copyOf(lastMisc);
         segments.add(new FormatSegment(subText, lastColour, miscCopy));
      }

      return new FormatString(segments.toArray(new FormatSegment[0]));
   }

   public FormatString[] wrap(IFontRenderer font, int maxWidth) {
      return this.wrap(font, maxWidth, true);
   }

   public FormatString[] wrap(IFontRenderer font, int maxWidth, boolean onWords) {
      List<FormatSegment> thisLine = new ArrayList<>();
      int widthUsed = 0;

      for (int segmentIndex = 0; segmentIndex < this.segments.length; segmentIndex++) {
         FormatSegment segment = this.segments[segmentIndex];
         int width = font.getStringWidth(segment.toFormatString());
         if (width + widthUsed > maxWidth) {
            String text = segment.toFormatString();
            int allowedLength = 1;
            boolean words = onWords;
            boolean foundSplit = false;

            while (!foundSplit) {
               for (int i = text.length(); i > 1; i--) {
                  String c = text.substring(i - 1, i);
                  if (!words || " \n\t".contains(c)) {
                     String subText = text.substring(0, i);
                     int w = font.getStringWidth(subText);
                     if (w + widthUsed <= maxWidth) {
                        allowedLength = i;
                        foundSplit = true;
                        break;
                     }
                  }
               }

               if (foundSplit || !words || segmentIndex != 0) {
                  break;
               }

               words = false;
            }

            int i = allowedLength;
            if (i > 1) {
               String subText = text.substring(0, allowedLength);
               int left = this.segments.length - segmentIndex;
               FormatSegment[] next = new FormatSegment[left];
               thisLine.add(new FormatSegment(subText, segment.colour, segment.misc));
               next[0] = new FormatSegment(text.substring(i), segment.colour, segment.misc);

               for (int j = 1; j < left; j++) {
                  next[j] = this.segments[segmentIndex + j];
               }

               return new FormatString[]{new FormatString(thisLine.toArray(new FormatSegment[0])), new FormatString(next)};
            }

            int left = this.segments.length - segmentIndex;
            FormatSegment[] next = new FormatSegment[left];

            for (int j = 0; j < left; j++) {
               next[j] = this.segments[segmentIndex + j];
            }

            return new FormatString[]{new FormatString(thisLine.toArray(new FormatSegment[0])), new FormatString(next)};
         }

         thisLine.add(segment);
         widthUsed += width;
      }

      return new FormatString[]{this};
   }
}
