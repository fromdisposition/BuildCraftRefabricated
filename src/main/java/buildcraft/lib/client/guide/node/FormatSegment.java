package buildcraft.lib.client.guide.node;

import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;

public final class FormatSegment {
   public final String text;
   public final ChatFormatting colour;
   public final Set<ChatFormatting> misc;

   FormatSegment(String text, ChatFormatting colour, Set<ChatFormatting> misc) {
      this.text = text;
      this.colour = colour;
      this.misc = misc;
   }

   @Nullable
   public FormatSegment join(FormatSegment other) {
      return this.colour == other.colour && this.misc.equals(other.misc) ? new FormatSegment(this.text + other.text, this.colour, this.misc) : null;
   }

   public String toFormatString() {
      StringBuilder miscString = new StringBuilder();

      for (ChatFormatting format : this.misc) {
         miscString.append(format.toString());
      }

      return ChatFormatting.RESET + (this.colour == null ? "" : this.colour.toString()) + miscString + this.text;
   }

   @Override
   public String toString() {
      StringBuilder miscStr = new StringBuilder();

      for (ChatFormatting format : this.misc) {
         miscStr.append(format.getName());
         miscStr.append(' ');
      }

      return (this.colour == null ? "" : this.colour.getName() + "") + miscStr + this.text;
   }
}
