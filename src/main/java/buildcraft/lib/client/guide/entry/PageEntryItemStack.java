package buildcraft.lib.client.guide.entry;

import buildcraft.api.core.BCLog;
import buildcraft.api.registry.IScriptableRegistry;
import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.client.guide.loader.MarkdownPageLoader;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.contents.PageLinkItemStack;
import buildcraft.lib.client.guide.ref.GuideGroupManager;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.ItemStackKey;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PageEntryItemStack extends PageValueType<ItemStackValueFilter> {
   public static final PageEntryItemStack INSTANCE = new PageEntryItemStack();
   private static final JsonTypeTags TAGS = new JsonTypeTags("buildcraft.guide.contents.item_stacks");
   private static final JsonTypeTags OTHER_ITEMS_TAGS = new JsonTypeTags("buildcraft.guide.contents.other_items");

   @Override
   public Class<ItemStackValueFilter> getEntryClass() {
      return ItemStackValueFilter.class;
   }

   protected boolean isValid(ItemStackValueFilter typed) {
      return !typed.stack.baseStack.isEmpty();
   }

   @Override
   public void iterateAllDefault(IEntryLinkConsumer consumer, ProfilerFiller prof) {
      prof.push("iterate_all_items");

      for (Item item : BuiltInRegistries.ITEM) {
         if (item != Items.AIR) {
            ItemStack stack = new ItemStack(item);
            if (!stack.isEmpty() && GuideManager.INSTANCE.objectsAdded.add(item)) {
               try {
                  String displayName = stack.getHoverName().getString();
                  if (displayName != null && !displayName.trim().isEmpty()) {
                     consumer.addChild(OTHER_ITEMS_TAGS, PageLinkItemStack.create(false, stack, prof));
                  }
               } catch (Exception e) {
                  BCLog.logger
                     .warn("[lib.guide] Skipping item " + BuiltInRegistries.ITEM.getKey(item) + " in the guide index — resolving its name/link threw.", e);
               }
            }
         }
      }

      prof.pop();
   }

   @Override
   public IScriptableRegistry.OptionallyDisabled<PageEntry<ItemStackValueFilter>> deserialize(Identifier name, JsonObject json, JsonDeserializationContext ctx) {
      if (!json.has("stack")) {
         throw new JsonSyntaxException("Expected either a string or an object for 'stack', but got nothing for " + json);
      }

      String str = json.get("stack").getAsString();
      if (str.startsWith("(") && str.endsWith(")")) {
         str = str.substring(1, str.length() - 1);
      }

      if (str.startsWith("{") && str.endsWith("}")) {
         str = str.substring(1, str.length() - 1);
      }

      if (str.contains(",")) {
         str = str.substring(0, str.indexOf(44)).trim();
      }

      str = str.trim();
      Identifier loc = Identifier.parse(str);
      Item item = BuiltInRegistries.ITEM.get(loc).map(ref -> (Item)ref.value()).orElse(null);
      if (item == null) {
         return new IScriptableRegistry.OptionallyDisabled<>("Unknown item '" + str + "' (from stack '" + json.get("stack").getAsString() + "')");
      }

      ItemStack stack = new ItemStack(item);
      ItemStackValueFilter filter = new ItemStackValueFilter(new ItemStackKey(stack), false, false);
      return new IScriptableRegistry.OptionallyDisabled<>(new PageEntry<>(this, name, json, filter));
   }

   public String getTitle(ItemStackValueFilter value) {
      return value.stack.baseStack.getHoverName().getString();
   }

   public List<String> getTooltip(ItemStackValueFilter value) {
      return Collections.singletonList(value.stack.baseStack.getHoverName().getString());
   }

   public boolean matches(ItemStackValueFilter entry, Object obj) {
      if (obj instanceof ItemStackValueFilter) {
         obj = ((ItemStackValueFilter)obj).stack.baseStack;
      }

      if (obj instanceof ItemStackKey) {
         obj = ((ItemStackKey)obj).baseStack;
      }

      if (obj instanceof ItemStack) {
         ItemStack base = entry.stack.baseStack;
         ItemStack test = (ItemStack)obj;
         return !base.isEmpty() && !test.isEmpty() ? base.getItem() == test.getItem() : false;
      } else {
         return false;
      }
   }

   @Nullable
   public ISimpleDrawable createDrawable(ItemStackValueFilter value) {
      return new GuiStack(value.stack.baseStack);
   }

   public Object getBasicValue(ItemStackValueFilter value) {
      return value.stack.baseStack.getItem();
   }

   public void addPageEntries(ItemStackValueFilter value, GuiGuide gui, List<GuidePart> parts) {
      GuideGroupManager.appendLinkedChapters(INSTANCE.wrap(value), gui, parts);
   }

   @Override
   public IScriptableRegistry.OptionallyDisabled<Object> createLink(String to, ProfilerFiller prof) {
      IScriptableRegistry.OptionallyDisabled<ItemStack> stackq = MarkdownPageLoader.parseItemStack(to);
      return stackq.isPresent()
         ? new IScriptableRegistry.OptionallyDisabled<>(PageLinkItemStack.create(true, stackq.get(), prof))
         : new IScriptableRegistry.OptionallyDisabled<>(stackq.getDisabledReason());
   }
}
