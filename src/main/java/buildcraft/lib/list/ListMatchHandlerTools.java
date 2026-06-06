package buildcraft.lib.list;

import buildcraft.api.lists.ListMatchHandler;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ListMatchHandlerTools extends ListMatchHandler {
   private static final TagKey<Item>[] TOOL_TAGS = new TagKey[]{ItemTags.AXES, ItemTags.PICKAXES, ItemTags.SHOVELS, ItemTags.HOES, ItemTags.SWORDS};

   @Override
   public boolean matches(ListMatchHandler.Type type, @Nonnull ItemStack stack, @Nonnull ItemStack target, boolean precise) {
      if (type == ListMatchHandler.Type.TYPE) {
         if (this.isValidSource(type, stack) && this.isValidSource(type, target)) {
            for (TagKey<Item> tag : TOOL_TAGS) {
               if (stack.is(tag) && target.is(tag)) {
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean isValidSource(ListMatchHandler.Type type, @Nonnull ItemStack stack) {
      for (TagKey<Item> tag : TOOL_TAGS) {
         if (stack.is(tag)) {
            return true;
         }
      }

      return false;
   }

   @Nonnull
   @Override
   public List<String> describeMatch(ListMatchHandler.Type type, @Nonnull ItemStack stack) {
      if (type != ListMatchHandler.Type.TYPE) {
         return List.of();
      }

      List<String> out = new ArrayList<>();

      for (TagKey<Item> tag : TOOL_TAGS) {
         if (stack.is(tag)) {
            out.add("#" + tag.location());
         }
      }

      return out;
   }
}
