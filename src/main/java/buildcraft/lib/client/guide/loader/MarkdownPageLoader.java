package buildcraft.lib.client.guide.loader;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.api.registry.IScriptableRegistry;
import buildcraft.lib.client.guide.entry.PageEntry;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.client.guide.parts.GuidePartFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public enum MarkdownPageLoader implements IPageLoaderText {
   INSTANCE;

   public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.markdown");

   public static ItemStack loadComplexItemStack(String line) {
      IScriptableRegistry.OptionallyDisabled<ItemStack> stackq = parseItemStack(line);
      if (stackq.isPresent()) {
         return stackq.get();
      }

      BCLog.logger.warn("[lib.guide.loader.markdown] " + stackq.getDisabledReason());
      return ItemStack.EMPTY;
   }

   public static IScriptableRegistry.OptionallyDisabled<ItemStack> parseItemStack(String line) {
      String[] args = line.split(",");
      if (args.length == 0) {
         return new IScriptableRegistry.OptionallyDisabled<>(line + " was not a valid complex item string!");
      }

      Identifier itemId = Identifier.tryParse(args[0].trim());
      if (itemId == null) {
         return new IScriptableRegistry.OptionallyDisabled<>(args[0] + " was not a valid item identifier!");
      }

      Item item = BuiltInRegistries.ITEM.get(itemId).map(ref -> (Item)ref.value()).orElse(null);
      if (item == null) {
         return new IScriptableRegistry.OptionallyDisabled<>(args[0] + " was not a valid item!");
      }

      ItemStack stack = new ItemStack(item);
      if (args.length == 1) {
         return new IScriptableRegistry.OptionallyDisabled<>(stack);
      }

      int stackSize;
      try {
         stackSize = Integer.parseInt(args[1].trim());
      } catch (NumberFormatException nfe) {
         return new IScriptableRegistry.OptionallyDisabled<>(args[1] + " was not a valid number: " + nfe.getLocalizedMessage());
      }

      stack.setCount(stackSize);
      return new IScriptableRegistry.OptionallyDisabled<>(stack);
   }

   @Override
   public GuidePageFactory loadPage(BufferedReader reader, Identifier name, PageEntry<?> entry, ProfilerFiller prof) throws IOException {
      BufferedReader nReader = preprocess(reader);
      return XmlPageLoader.INSTANCE.loadPage(nReader, name, entry, prof);
   }

   public List<GuidePartFactory> loadParts(BufferedReader reader, ProfilerFiller prof) throws IOException {
      return XmlPageLoader.loadParts(preprocess(reader), prof);
   }

   private static BufferedReader preprocess(BufferedReader reader) throws IOException {
      StringBuilder replaced = new StringBuilder();

      String line;
      while ((line = reader.readLine()) != null) {
         replaced.append(replaceSpecialForXml(line));
         replaced.append('\n');
      }

      return new BufferedReader(new StringReader(replaced.toString()));
   }

   private static String replaceSpecialForXml(String line) {
      if (!line.startsWith("#")) {
         return line;
      }

      int level;
      for (level = -1; line.startsWith("#"); level++) {
         line = line.substring(1);
      }

      line = line.trim();
      return level == 0 ? "<chapter name=\"" + line + "\"/>" : "<chapter name=\"" + line + "\" level=\"" + level + "\"/>";
   }
}
