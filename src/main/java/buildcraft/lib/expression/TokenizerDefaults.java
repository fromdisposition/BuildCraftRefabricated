package buildcraft.lib.expression;

import java.util.ArrayList;
import java.util.List;

public class TokenizerDefaults {
   public static final Tokenizer.ITokenizerGobbler GOBBLER_QUOTE = ctx -> {
      int length = 1;
      char type = ctx.getCharAt(0);
      if (type != '\'' && type != '"') {
         return Tokenizer.ResultSpecific.IGNORE;
      }

      while (true) {
         char c = ctx.getCharAt(length);
         if (c == '\\') {
            length++;
         } else {
            if (c == type) {
               return new Tokenizer.ResultConsume(length + 1);
            }

            if (c == '\n') {
               return new Tokenizer.ResultInvalid(length + 1);
            }
         }

         length++;
      }
   };
   private static final String[] MATH_OPS_2_CHAR = new String[]{"||", "&&", "<=", ">=", "==", "!=", "<<", ">>"};
   public static final Tokenizer.ITokenizerGobbler GOBBLER_MATH_OPERATOR = ctx -> {
      String possible = ctx.get(2);

      for (String s : MATH_OPS_2_CHAR) {
         if (s.equals(possible)) {
            return Tokenizer.ResultConsume.TWO;
         }
      }

      return Tokenizer.ResultSpecific.IGNORE;
   };
   public static final Tokenizer.ITokenizerGobbler GOBBLER_HEXADECIMAL = ctx -> {
      if (!"0x".equals(ctx.get(2))) {
         return Tokenizer.ResultSpecific.IGNORE;
      }

      int size = 2;

      while (true) {
         char c = ctx.getCharAt(size);
         if ('_' != c && (c < '0' || c > '9') && (c < 'a' || c > 'f') && (c < 'A' || c > 'F')) {
            return size > 2 ? new Tokenizer.ResultConsume(size) : Tokenizer.ResultSpecific.IGNORE;
         }

         size++;
      }
   };
   public static final Tokenizer.ITokenizerGobbler GOBBLER_NUMBER = ctx -> {
      int i = 0;
      int dot = -1;

      while (true) {
         char c = ctx.getCharAt(i);
         if (c == '.') {
            if (dot >= 0) {
               break;
            }

            dot = i;
         } else if (!Character.isDigit(c)) {
            break;
         }

         i++;
      }

      if (i == 0) {
         return Tokenizer.ResultSpecific.IGNORE;
      } else {
         boolean digitsBeforeDot = dot > 0;
         boolean digitsAfterDot = i > dot + 1;
         if (digitsBeforeDot) {
            return digitsAfterDot ? new Tokenizer.ResultConsume(i) : new Tokenizer.ResultConsume(i - 1);
         } else {
            return digitsAfterDot ? new Tokenizer.ResultConsume(i) : Tokenizer.ResultSpecific.IGNORE;
         }
      }
   };
   public static final Tokenizer.ITokenizerGobbler GOBBLER_WORD = ctx -> {
      int i = 0;

      while (true) {
         char c = ctx.getCharAt(i);
         if (i == 0 ? c != '.' && !Character.isJavaIdentifierStart(c) : !Character.isJavaIdentifierPart(c)) {
            return i == 0 ? Tokenizer.ResultSpecific.IGNORE : new Tokenizer.ResultConsume(i);
         }

         i++;
      }
   };
   public static final Tokenizer.ITokenizerGobbler GOBBLER_NON_WHITESPACE = ctx -> {
      char c = ctx.getCharAt(0);
      if (c == '\n') {
         return Tokenizer.ResultSpecific.IGNORE;
      } else {
         return Character.isWhitespace(c) ? Tokenizer.ResultSpecific.IGNORE : Tokenizer.ResultConsume.ONE;
      }
   };
   public static final Tokenizer.ITokenizerGobbler GOBBLER_DISCARD = ctx -> Tokenizer.ResultDiscard.SINGLE;

   public static List<Tokenizer.ITokenizerGobbler> createParts() {
      List<Tokenizer.ITokenizerGobbler> list = new ArrayList<>();
      list.add(GOBBLER_QUOTE);
      list.add(GOBBLER_MATH_OPERATOR);
      list.add(GOBBLER_HEXADECIMAL);
      list.add(GOBBLER_NUMBER);
      list.add(GOBBLER_WORD);
      list.add(GOBBLER_NON_WHITESPACE);
      list.add(GOBBLER_DISCARD);
      return list;
   }

   public static Tokenizer createTokenizer() {
      return new Tokenizer(createParts());
   }
}
