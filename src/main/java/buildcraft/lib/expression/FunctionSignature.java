package buildcraft.lib.expression;

import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public final class FunctionSignature {
   private static final Tokenizer FUNCTION_TOKENIZER = new Tokenizer(
      TokenizerDefaults.GOBBLER_WORD, TokenizerDefaults.GOBBLER_NON_WHITESPACE, TokenizerDefaults.GOBBLER_DISCARD
   );
   public final String name;
   public final Argument[] args;
   @Nullable
   public final String func;

   public FunctionSignature(String name, Argument[] args, String func) {
      this.name = name;
      this.args = args;
      this.func = func;
   }

   public static FunctionSignature parse(String desc) throws InvalidExpressionException {
      try {
         return parse0(desc);
      } catch (InvalidExpressionException iee) {
         throw new InvalidExpressionException("Invalid function signature '" + desc + "'", iee);
      } catch (IllegalStateException ise) {
         throw new IllegalStateException("Very badly broken function signature '" + desc + "'", ise);
      }
   }

   private static FunctionSignature parse0(String desc) throws InvalidExpressionException {
      Tokenizer t = FUNCTION_TOKENIZER;
      String name = null;
      List<Argument> args = new ArrayList<>();
      int state = 0;
      Class<?> argType = null;

      for (Tokenizer.Token token : t.tokenizeWithInfo(desc)) {
         if (name == null) {
            if (token.gobbler != TokenizerDefaults.GOBBLER_WORD) {
               throw new InvalidExpressionException("Expected to find a name, but actually found " + token.text);
            }

            name = token.text;
         } else {
            switch (state) {
               case 0:
                  if (!"(".equals(token.text)) {
                     throw new InvalidExpressionException("Expected '(', but found " + token.text);
                  }

                  state = 1;
                  break;
               case 1:
                  if (")".equals(token.text)) {
                     state = 5;
                     break;
                  } else {
                     int var10 = 2;
                  }
               case 2:
                  argType = NodeTypes.parseType(token.text);
                  state = 3;
                  break;
               case 3:
                  if (token.gobbler != TokenizerDefaults.GOBBLER_WORD) {
                     throw new InvalidExpressionException("Expected to find a name, but actually found " + token.text);
                  }

                  args.add(new Argument(token.text, argType));
                  state = 4;
                  break;
               case 4:
                  if (",".equals(token.text)) {
                     state = 2;
                  } else {
                     if (!")".equals(token.text)) {
                        throw new InvalidExpressionException("Expected to find either ',' or ')', but found '" + token.text + "'");
                     }

                     state = 5;
                  }
                  break;
               case 5:
                  if (!"{".equals(token.text)) {
                     throw new InvalidExpressionException("Expected to find either the end of the function signature, or a '{', but found '" + token.text + "'");
                  }

                  state = 6;
                  break;
               default:
                  throw new IllegalStateException("Unknown state " + state + "!");
            }
         }
      }

      if (state == 5) {
         return new FunctionSignature(name, args.toArray(new Argument[0]), null);
      }

      if (state == 6) {
         int idxOpen = desc.indexOf("{");
         int idxClose = desc.lastIndexOf("}");
         if (!desc.trim().endsWith("}")) {
            throw new InvalidExpressionException("Expected to find '}' at the end of the function!");
         } else {
            return new FunctionSignature(name, args.toArray(new Argument[0]), desc.substring(idxOpen + 1, idxClose));
         }
      } else {
         throw new InvalidExpressionException("Missing more tokens!");
      }
   }
}
