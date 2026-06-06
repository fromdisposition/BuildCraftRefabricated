package buildcraft.lib.client.guide.parts.contents;

import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.entry.ItemStackValueFilter;
import buildcraft.lib.client.guide.entry.PageEntryItemStack;
import buildcraft.lib.client.guide.entry.PageValue;
import buildcraft.lib.client.guide.parts.GuidePage;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.misc.ItemStackKey;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;

public final class PageLinkItemPermutations extends PageLink {
   private final List<ItemStack> permutations;

   private PageLinkItemPermutations(PageLine text, boolean startVisible, List<ItemStack> permutations) {
      super(text, startVisible);
      this.permutations = permutations;
   }

   @Override
   public GuidePageFactory getFactoryLink() {
      return gui -> {
         List<GuidePart> parts = new ArrayList<>();

         for (ItemStack var4 : this.permutations) {
            ;
         }

         ItemStackValueFilter filter = new ItemStackValueFilter(new ItemStackKey(this.permutations.get(0)), false, false);
         return new GuidePage(gui, parts, new PageValue<>(PageEntryItemStack.INSTANCE, filter));
      };
   }

   public static PageLinkItemPermutations create(boolean startVisible, List<ItemStack> stacks, ProfilerFiller prof) {
      PageLinkItemStack link = PageLinkItemStack.create(startVisible, stacks.get(0), prof);
      return new PageLinkItemPermutations(link.text, startVisible, stacks);
   }
}
