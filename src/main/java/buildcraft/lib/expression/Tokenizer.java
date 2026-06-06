package buildcraft.lib.expression;

import buildcraft.lib.expression.api.InvalidExpressionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

public class Tokenizer {
   public static final char END_OF_LINE = '\n';
   private final List<Tokenizer.ITokenizerGobbler> tokenizers;

   public Tokenizer(List<Tokenizer.ITokenizerGobbler> tokenizers) {
      this.tokenizers = new ArrayList<>(tokenizers);
   }

   public Tokenizer(Tokenizer.ITokenizerGobbler... tokenizers) {
      this(Arrays.asList(tokenizers));
   }

   public String[] tokenize(String src) throws InvalidExpressionException {
      return this.tokenizeInternal(src, (str, g) -> str, new String[0]);
   }

   public Tokenizer.Token[] tokenizeWithInfo(String src) throws InvalidExpressionException {
      return this.tokenizeInternal(src, Tokenizer.Token::new, new Tokenizer.Token[0]);
   }

   private <T> T[] tokenizeInternal(String src, BiFunction<String, Tokenizer.ITokenizerGobbler, T> fn, T[] array) throws InvalidExpressionException {
      List<T> tokens = new ArrayList<>();
      int index = 0;

      while (index < src.length()) {
         int contextStart = index;
         Tokenizer.ITokenizingContext ctx = Tokenizer.ITokenizingContext.createFromString(contextStart, src);
         boolean consumed = false;
         Iterator var9 = this.tokenizers.iterator();

         while (true) {
            if (var9.hasNext()) {
               Tokenizer.ITokenizerGobbler token = (Tokenizer.ITokenizerGobbler)var9.next();
               Tokenizer.TokenResult res = token.tokenizePart(ctx);
               if (res == Tokenizer.ResultSpecific.IGNORE) {
                  continue;
               }

               if (res == Tokenizer.ResultSpecific.INVALID) {
                  throw new InvalidExpressionException("Invalid src \"" + ctx.get(10).replace("\n", "\\n") + "\"");
               }

               if (res instanceof Tokenizer.ResultInvalid) {
                  throw new InvalidExpressionException("Invalid src \"" + ctx.get(((Tokenizer.ResultInvalid)res).length).replace("\n", "\\n") + "\"");
               }

               if (res instanceof Tokenizer.ResultDiscard) {
                  int discardLength = ((Tokenizer.ResultDiscard)res).length;
                  index += discardLength;
                  consumed = true;
               } else {
                  if (!(res instanceof Tokenizer.ResultConsume)) {
                     continue;
                  }

                  int consumedLength = ((Tokenizer.ResultConsume)res).length;
                  String at = ctx.get(consumedLength);
                  tokens.add(fn.apply(at, token));
                  index += consumedLength;
                  consumed = true;
               }
            }

            if (!consumed) {
               throw new InvalidExpressionException("Did not consume:" + ctx.get(50));
            }
            break;
         }
      }

      return (T[])tokens.toArray(array);
   }

   @FunctionalInterface
   public interface ITokenizerGobbler {
      Tokenizer.TokenResult tokenizePart(Tokenizer.ITokenizingContext var1);
   }

   @FunctionalInterface
   public interface ITokenizingContext {
      String get(int var1, int var2);

      default String get(int length) {
         return this.get(0, length);
      }

      default char getCharAt(int rel) {
         return this.get(rel, rel + 1).charAt(0);
      }

      static Tokenizer.ITokenizingContext createFromString(String src) {
         return createFromString(0, src);
      }

      static Tokenizer.ITokenizingContext createFromString(int contextStart, String src) {
         return (relStart, relEnd) -> {
            int start = contextStart + relStart;
            int end = contextStart + relEnd;
            int stringEnd = src.length();
            StringBuilder gotten = new StringBuilder(src.substring(start, Math.min(end, stringEnd)));

            while (gotten.length() < end - start) {
               gotten.append('\n');
            }

            return gotten.toString();
         };
      }
   }

   public static class ResultConsume implements Tokenizer.TokenResult {
      public static final Tokenizer.ResultConsume ONE = new Tokenizer.ResultConsume(1);
      public static final Tokenizer.ResultConsume TWO = new Tokenizer.ResultConsume(2);
      public final int length;

      public ResultConsume(int length) {
         this.length = length;
      }
   }

   public static class ResultDiscard implements Tokenizer.TokenResult {
      public static final Tokenizer.ResultDiscard SINGLE = new Tokenizer.ResultDiscard(1);
      public final int length;

      public ResultDiscard(int length) {
         this.length = length;
      }
   }

   public static class ResultInvalid implements Tokenizer.TokenResult {
      public final int length;

      public ResultInvalid(int length) {
         this.length = length;
      }
   }

   public enum ResultSpecific implements Tokenizer.TokenResult {
      IGNORE,
      INVALID;
   }

   public static final class Token {
      public final String text;
      public final Tokenizer.ITokenizerGobbler gobbler;

      public Token(String text, Tokenizer.ITokenizerGobbler gobbler) {
         this.text = text;
         this.gobbler = gobbler;
      }
   }

   public interface TokenResult {
   }
}
