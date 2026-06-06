package buildcraft.lib.client.guide.loader;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.registry.IScriptableRegistry;
import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.GuidePageRegistry;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.entry.PageEntry;
import buildcraft.lib.client.guide.entry.PageValueType;
import buildcraft.lib.client.guide.parts.GuideChapterWithin;
import buildcraft.lib.client.guide.parts.GuideImageFactory;
import buildcraft.lib.client.guide.parts.GuideInlineLinkText;
import buildcraft.lib.client.guide.parts.GuidePageEntry;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuidePartCodeBlock;
import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.client.guide.parts.GuidePartGroup;
import buildcraft.lib.client.guide.parts.GuidePartLink;
import buildcraft.lib.client.guide.parts.GuidePartMulti;
import buildcraft.lib.client.guide.parts.GuidePartNewPage;
import buildcraft.lib.client.guide.parts.GuideText;
import buildcraft.lib.client.guide.parts.contents.PageLink;
import buildcraft.lib.client.guide.parts.contents.PageLinkNormal;
import buildcraft.lib.client.guide.parts.recipe.GuideAssemblyRecipes;
import buildcraft.lib.client.guide.parts.recipe.GuideCraftingFactory;
import buildcraft.lib.client.guide.parts.recipe.GuideCraftingRecipes;
import buildcraft.lib.client.guide.parts.recipe.IStackRecipes;
import buildcraft.lib.client.guide.parts.recipe.RecipeLookupHelper;
import buildcraft.lib.client.guide.ref.GuideGroupManager;
import buildcraft.lib.client.guide.ref.GuideGroupSet;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.RegistryKeyUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;

public enum XmlPageLoader implements IPageLoaderText {
   INSTANCE;

   public static final Map<String, XmlPageLoader.SpecialParser> TAG_FACTORIES = new HashMap<>();
   public static final Map<String, XmlPageLoader.MultiPartJoiner> GUIDE_PART_MULTIS = new HashMap<>();
   public static boolean SHOW_LORE = true;
   public static boolean SHOW_HINTS = false;
   public static boolean SHOW_DETAIL = false;
   public static final int RECIPE_BREAK_THRESHOLD = 30;

   public static boolean shouldShowDetail() {
      return SHOW_DETAIL;
   }

   public static void putDuelMultiPartType(String name, BooleanSupplier isVisible) {
      putSimpleMultiPartType(name, isVisible);
      putSimpleMultiPartType("no_" + name, () -> !isVisible.getAsBoolean());
   }

   public static void putSimpleMultiPartType(String name, BooleanSupplier isVisible) {
      putMultiPartType(name, (tag, factories, prof) -> gui -> {
         List<GuidePart> subParts = new ArrayList<>(factories.size());

         for (GuidePartFactory factory : factories) {
            subParts.add(factory.createNew(gui));
         }

         return new GuidePartMulti(gui, subParts, isVisible);
      });
   }

   public static void putCode(String name) {
      putMultiPartType(name, (tag, factories, prof) -> {
         List<String> lines = new ArrayList<>();

         for (GuidePartFactory factory : factories) {
            if (factory instanceof XmlPageLoader.GuideTextFactory) {
               lines.add(((XmlPageLoader.GuideTextFactory)factory).text);
            }
         }

         for (int i = 0; i < lines.size(); i++) {
            String str = lines.get(i);
            if (str.startsWith("~{") && str.endsWith("}") && str.indexOf(123, 2) == -1 && str.indexOf(125) == str.length() - 1) {
               lines.set(i, ChatFormatting.DARK_PURPLE + str);
            } else {
               str = str.replace("{", ChatFormatting.DARK_GREEN + "{" + ChatFormatting.RESET);
               str = str.replace("}", ChatFormatting.DARK_GREEN + "}" + ChatFormatting.RESET);
               str = str.replaceAll("\"(.+)\"", ChatFormatting.DARK_BLUE + "$0" + ChatFormatting.RESET);
               str = str.replaceAll("%[0-9]+", ChatFormatting.DARK_PURPLE + "$0" + ChatFormatting.RESET);
               str = str.replaceAll("//", ChatFormatting.DARK_GREEN + "//");
               lines.set(i, str);
            }
         }

         return gui -> new GuidePartCodeBlock(gui, lines);
      });
   }

   public static void putMultiPartType(String name, XmlPageLoader.MultiPartJoiner joiner) {
      GUIDE_PART_MULTIS.put(name, joiner);
   }

   public static void putSingle(String string, XmlPageLoader.SpecialParserSingle parser) {
      putMulti(string, parser);
   }

   public static void putMulti(String string, XmlPageLoader.SpecialParser parser) {
      TAG_FACTORIES.put(string, parser);
   }

   @Override
   public GuidePageFactory loadPage(BufferedReader reader, Identifier name, PageEntry<?> entry, ProfilerFiller prof) throws IOException {
      prof.push("xml");

      try {
         return loadPage0(reader, name, entry, prof);
      } finally {
         prof.pop();
      }
   }

   public static List<GuidePartFactory> loadParts(BufferedReader reader, ProfilerFiller prof) throws IOException {
      prof.push("xml");

      try {
         return parsePartFactories(reader, prof);
      } finally {
         prof.pop();
      }
   }

   private static GuidePageFactory loadPage0(BufferedReader reader, Identifier name, PageEntry<?> entry, ProfilerFiller prof) throws IOException, InvalidInputDataException {
      List<GuidePartFactory> factories = parsePartFactories(reader, prof);
      return gui -> {
         List<GuidePart> parts = new ArrayList<>();

         for (GuidePartFactory factory : factories) {
            parts.add(factory.createNew(gui));
         }

         return new GuidePageEntry(gui, parts, entry, name);
      };
   }

   private static List<GuidePartFactory> parsePartFactories(BufferedReader reader, ProfilerFiller prof) throws IOException, InvalidInputDataException {
      Deque<List<GuidePartFactory>> nestedParts = new ArrayDeque<>();
      Deque<XmlPageLoader.XmlTag> nestedTags = new ArrayDeque<>();
      nestedParts.push(new ArrayList<>());

      while (true) {
         String line;
         while (true) {
            if ((line = reader.readLine()) == null) {
               List<GuidePartFactory> factories = nestedParts.pop();
               if (nestedParts.size() != 0) {
                  throw new InvalidInputDataException("We haven't closed " + nestedTags);
               }

               while (!factories.isEmpty()) {
                  GuidePartFactory last = factories.get(factories.size() - 1);
                  if (!(last instanceof XmlPageLoader.GuideTextFactory text) || !text.text.isBlank()) {
                     break;
                  }

                  factories.remove(factories.size() - 1);
               }

               return factories;
            }

            if (!line.startsWith("//")) {
               if (line.startsWith("\\/\\/")) {
                  line = "//" + line.substring(4);
               }

               prof.push("parse_tag");
               XmlPageLoader.XmlTag tag = parseTag(line);
               prof.pop();
               if (tag == null) {
                  break;
               }

               if (tag.state == XmlPageLoader.XmlTagState.COMPLETE) {
                  XmlPageLoader.SpecialParser parser = TAG_FACTORIES.get(tag.name);
                  if (parser != null) {
                     prof.push("use_" + tag.name);
                     List<GuidePartFactory> factories = parser.parse(tag, prof);
                     prof.pop();
                     if (factories != null) {
                        nestedParts.peek().addAll(factories);
                        line = line.substring(tag.originalString.length());
                     } else {
                        int len = tag.originalString.length();
                        line = "<red>" + line.substring(0, len) + "</red>" + line.substring(len);
                     }
                  }
               } else if (tag.state == XmlPageLoader.XmlTagState.START) {
                  XmlPageLoader.MultiPartJoiner joiner = GUIDE_PART_MULTIS.get(tag.name);
                  if (joiner != null) {
                     nestedTags.push(tag);
                     nestedParts.push(new ArrayList<>());
                     line = line.substring(tag.originalString.length());
                  } else {
                     int len = tag.originalString.length();
                     line = "<red>" + line.substring(0, len) + "</red>" + line.substring(len);
                  }
               } else {
                  XmlPageLoader.MultiPartJoiner joiner = GUIDE_PART_MULTIS.get(tag.name);
                  if (joiner != null) {
                     if (nestedTags.isEmpty()) {
                        throw new InvalidInputDataException("Tried to close " + tag.name + " before openining it!");
                     }

                     XmlPageLoader.XmlTag nameTag = nestedTags.pop();
                     if (!tag.name.equals(nameTag.name)) {
                        throw new InvalidInputDataException("Tried to close " + tag.name + " before instead of " + nameTag.name + "!");
                     }

                     List<GuidePartFactory> subParts = nestedParts.pop();
                     prof.push("join_" + tag.name);
                     GuidePartFactory joined = joiner.join(nameTag, subParts, prof);
                     prof.pop();
                     if (joined == null) {
                        nestedParts.peek().addAll(subParts);
                        int len = tag.originalString.length();
                        line = "<red>" + line.substring(0, len) + "</red>" + line.substring(len);
                     } else {
                        nestedParts.peek().add(joined);
                        line = line.substring(tag.originalString.length());
                     }
                  }
               }

               if (line.length() != 0) {
                  break;
               }
            }
         }

         if (line.length() == 0) {
            line = " ";
         }

         prof.push("text_format");
         Set<ChatFormatting> formattingElements = EnumSet.noneOf(ChatFormatting.class);
         Deque<ChatFormatting> formatColours = new ArrayDeque<>();
         String completeLine = "";
         List<GuideInlineLinkText.InlineLinkSpan> inlineLinks = new ArrayList<>();
         int i = 0;

         while (i < line.length()) {
            char c = line.charAt(i);
            if (c == '<') {
               XmlPageLoader.XmlTag currentTag = parseTag(line.substring(i));
               if (currentTag != null) {
                  ChatFormatting formatting = ChatFormatting.getByName(currentTag.name.replace("_", ""));
                  if (formatting != null) {
                     if (currentTag.state == XmlPageLoader.XmlTagState.END) {
                        formattingElements.remove(formatting);
                        if (!formatColours.isEmpty() && formatColours.peek() == formatting) {
                           formatColours.remove();
                        }
                     } else if (currentTag.state == XmlPageLoader.XmlTagState.START) {
                        if (formatting.isColor()) {
                           formatColours.push(formatting);
                        } else {
                           formattingElements.add(formatting);
                        }
                     }

                     completeLine = completeLine + ChatFormatting.RESET;
                     if (!formatColours.isEmpty() && formatColours.peek() != null) {
                        completeLine = completeLine + formatColours.peek();
                     }

                     for (ChatFormatting format : formattingElements) {
                        completeLine = completeLine + format;
                     }

                     i += currentTag.originalString.length();
                     continue;
                  }

                  if ("link".equals(currentTag.name) && currentTag.get("inline") != null) {
                     XmlPageLoader.ResolvedLink resolved = resolveLinkTarget(currentTag, prof);
                     if (resolved != null) {
                        int visibleStart = GuideInlineLinkText.visibleLengthOf(completeLine);
                        completeLine = completeLine + ChatFormatting.RESET;
                        completeLine = completeLine + ChatFormatting.UNDERLINE;
                        completeLine = completeLine + ChatFormatting.BLUE;
                        String var32 = completeLine + resolved.title;
                        completeLine = var32 + ChatFormatting.RESET;
                        if (!formatColours.isEmpty() && formatColours.peek() != null) {
                           completeLine = completeLine + formatColours.peek();
                        }

                        for (ChatFormatting format : formattingElements) {
                           completeLine = completeLine + format;
                        }

                        inlineLinks.add(new GuideInlineLinkText.InlineLinkSpan(visibleStart, resolved.title.length(), resolved.factoryFn, resolved.title));
                     } else {
                        completeLine = completeLine + currentTag.originalString;
                     }

                     i += currentTag.originalString.length();
                     continue;
                  }
               }
            } else if (line.startsWith("&lt;", i)) {
               c = '<';
               i += 3;
            } else if (line.startsWith("&gt;", i)) {
               c = '>';
               i += 3;
            }

            completeLine = completeLine + c;
            i++;
         }

         String modLine = completeLine;
         if (inlineLinks.isEmpty()) {
            nestedParts.peek().add(new XmlPageLoader.GuideTextFactory(modLine));
         } else {
            List<GuideInlineLinkText.InlineLinkSpan> spans = ImmutableList.copyOf(inlineLinks);
            nestedParts.peek().add(gui -> new GuideInlineLinkText(gui, modLine, spans));
         }

         prof.pop();
      }
   }

   @Nullable
   public static XmlPageLoader.XmlTag parseTag(String string) throws InvalidInputDataException {
      if (!string.startsWith("<")) {
         return null;
      }

      int end = string.indexOf(62);
      if (end < 0) {
         throw new InvalidInputDataException("Didn't find an end tag for " + string);
      }

      String tagContents = string.substring(1, end);
      boolean hasStart = tagContents.startsWith("/");
      if (hasStart) {
         tagContents = tagContents.substring(1);
      }

      boolean hasEnd = tagContents.endsWith("/");
      if (hasEnd) {
         tagContents = tagContents.substring(0, tagContents.length() - 1);
      }

      int paramStart = tagContents.indexOf(32);
      String tag;
      Map<String, String> attributes;
      if (paramStart < 0) {
         tag = tagContents;
         attributes = ImmutableMap.of();
      } else {
         tag = tagContents.substring(0, paramStart);
         attributes = new HashMap<>();
         String attribs = tagContents.substring(paramStart + 1);

         while (attribs.length() > 0) {
            attribs = attribs.trim();
            int index = attribs.indexOf(61);
            if (index < 0) {
               break;
            }

            String key = attribs.substring(0, index);
            String after = attribs.substring(index + 1);
            int totalLength = index + 1;
            String value;
            if (after.startsWith("\"")) {
               int closeQuote = after.indexOf(34, 1);
               if (closeQuote < 0) {
                  throw new InvalidInputDataException("Not a valid tag value " + after);
               }

               value = after.substring(1, closeQuote);
               totalLength += closeQuote + 1;
            } else {
               int spaceIdx = after.indexOf(32);
               if (spaceIdx < 0) {
                  value = after;
               } else {
                  value = after.substring(0, spaceIdx);
               }

               totalLength += value.length();
            }

            attributes.put(key, value);
            attribs = attribs.substring(totalLength);
         }
      }

      XmlPageLoader.XmlTagState state;
      if (hasEnd) {
         state = XmlPageLoader.XmlTagState.COMPLETE;
      } else if (hasStart) {
         state = XmlPageLoader.XmlTagState.END;
      } else {
         state = XmlPageLoader.XmlTagState.START;
      }

      return new XmlPageLoader.XmlTag(tag, attributes, state, string.substring(0, end + 1));
   }

   private static GuidePartFactory loadChapter(XmlPageLoader.XmlTag tag, ProfilerFiller prof) {
      String name = tag.get("name");
      String level = tag.get("level");
      if (name == null) {
         BCLog.logger.warn("[lib.guide.loader.xml] Found a chapter tag without a name!" + tag);
         return null;
      }

      if (level == null) {
         level = "0";
      }

      try {
         int intLevel = Integer.parseInt(level);
         return chapter(name, intLevel);
      } catch (NumberFormatException nfe) {
         String str = "§4" + tag.originalString + "§r";
         str = str.replace(level, "§c" + level + "§4");
         return new XmlPageLoader.GuideTextFactory(str);
      }
   }

   private static GuidePartFactory loadLink(XmlPageLoader.XmlTag tag, ProfilerFiller prof) {
      if (tag.get("inline") != null) {
         XmlPageLoader.ResolvedLink resolved = resolveLinkTarget(tag, prof);
         if (resolved == null) {
            return null;
         }

         String formatted = "" + ChatFormatting.RESET + ChatFormatting.UNDERLINE + ChatFormatting.BLUE + resolved.title + ChatFormatting.RESET;
         List<GuideInlineLinkText.InlineLinkSpan> spans = ImmutableList.of(
            new GuideInlineLinkText.InlineLinkSpan(0, resolved.title.length(), resolved.factoryFn, resolved.title)
         );
         return gui -> new GuideInlineLinkText(gui, formatted, spans);
      } else {
         String to = tag.get("to");
         String type = tag.get("type");
         if (to == null) {
            BCLog.logger.warn("[lib.guide.loader.xml] Found a link tag without a 'to' tag! " + tag);
            return null;
         }

         PageLink link;
         if (type == null) {
            Identifier location = Identifier.parse(to);
            PageEntry<?> entry = GuidePageRegistry.INSTANCE.getReloadableEntryMap().get(location);
            if (entry == null) {
               return gui -> {
                  PageLink categoryLink = GuideManager.INSTANCE.getCategoryLink(location);
                  if (categoryLink == null) {
                     BCLog.logger.warn("[lib.guide.loader.xml] Found a link tag to an unknown page! " + tag);
                     return null;
                  } else {
                     return new GuidePartLink(gui, categoryLink);
                  }
               };
            }

            String translatedTitle = entry.title;
            ISimpleDrawable icon = entry.createDrawable();
            PageLine line = new PageLine(icon, icon, 2, translatedTitle, true);
            link = new PageLinkNormal(line, true, entry.getTooltip(), gui -> {
               GuidePageFactory factory = GuideManager.INSTANCE.getFactoryFor(location);
               return factory == null ? null : factory.createNew(gui);
            });
         } else {
            PageValueType<?> valueType = GuidePageRegistry.INSTANCE.types.get(type);
            if (valueType == null) {
               BCLog.logger
                  .warn(
                     "[lib.guide.loader.xml] Found a link tag with an unknown 'type'! (valid ones are "
                        + GuidePageRegistry.INSTANCE.types.keySet()
                        + ") "
                        + tag
                  );
               return null;
            }

            IScriptableRegistry.OptionallyDisabled<PageLink> linkq = uncheckedCreateLink(valueType, to, prof);
            if (!linkq.isPresent()) {
               BCLog.logger.warn("[lib.guide.loader.xml] Found a link tag that didn't link to anything valid: " + linkq.getDisabledReason() + " " + tag);
               return null;
            }

            link = linkq.get();
         }

         return gui -> new GuidePartLink(gui, link);
      }
   }

   @SuppressWarnings("unchecked")
   private static IScriptableRegistry.OptionallyDisabled<PageLink> uncheckedCreateLink(PageValueType<?> valueType, String to, ProfilerFiller prof) {
      return (IScriptableRegistry.OptionallyDisabled<PageLink>)(Object)valueType.createLink(to, prof);
   }

   @Nullable
   private static XmlPageLoader.ResolvedLink resolveLinkTarget(XmlPageLoader.XmlTag tag, ProfilerFiller prof) {
      String to = tag.get("to");
      String type = tag.get("type");
      if (to == null) {
         String inline = tag.get("inline");
         if (inline != null && !inline.isEmpty() && !"true".equals(inline)) {
            to = inline;
         }
      }

      if (to == null) {
         BCLog.logger.warn("[lib.guide.loader.xml] Found a link tag without a 'to' tag! " + tag);
         return null;
      } else if (type == null) {
         Identifier location = Identifier.parse(to);
         PageEntry<?> entry = GuidePageRegistry.INSTANCE.getReloadableEntryMap().get(location);
         return entry != null
            ? new XmlPageLoader.ResolvedLink(entry.title, gui -> GuideManager.INSTANCE.getFactoryFor(location))
            : new XmlPageLoader.ResolvedLink(to, gui -> {
               PageLink categoryLink = GuideManager.INSTANCE.getCategoryLink(location);
               if (categoryLink == null) {
                  BCLog.logger.warn("[lib.guide.loader.xml] Found a link tag to an unknown page! " + tag);
                  return null;
               } else {
                  return categoryLink.getFactoryLink();
               }
            });
      } else {
         PageValueType<?> valueType = GuidePageRegistry.INSTANCE.types.get(type);
         if (valueType == null) {
            BCLog.logger
               .warn(
                  "[lib.guide.loader.xml] Found a link tag with an unknown 'type'! (valid ones are " + GuidePageRegistry.INSTANCE.types.keySet() + ") " + tag
               );
            return null;
         } else {
            IScriptableRegistry.OptionallyDisabled<PageLink> linkq = uncheckedCreateLink(valueType, to, prof);
            if (!linkq.isPresent()) {
               BCLog.logger.warn("[lib.guide.loader.xml] Found a link tag that didn't link to anything valid: " + linkq.getDisabledReason() + " " + tag);
               return null;
            } else {
               PageLink resolved = linkq.get();
               return new XmlPageLoader.ResolvedLink(resolved.text.text, gui -> resolved.getFactoryLink());
            }
         }
      }
   }

   private static GuidePartFactory loadImage(XmlPageLoader.XmlTag tag, ProfilerFiller prof) {
      String src = tag.get("src");
      if (src == null) {
         BCLog.logger.warn("[lib.guide.loader.xml] Found an image tag without an src!" + tag);
         return null;
      } else {
         int width = parseInt("width", -1, tag);
         int height = parseInt("height", -1, tag);
         return new GuideImageFactory(src, width, height);
      }
   }

   private static int parseInt(String name, int _default, XmlPageLoader.XmlTag tag) {
      String value = tag.get(name);
      if (value == null) {
         return _default;
      }

      try {
         return Integer.parseInt(value);
      } catch (NumberFormatException nfe) {
         BCLog.logger.warn("[lib.guide.loader.xml] Found an invalid number for image tag (" + name + ") " + tag + nfe.getMessage());
         return _default;
      }
   }

   private static GuidePartFactory loadRecipe(XmlPageLoader.XmlTag tag, ProfilerFiller prof) {
      String recipeId = tag.get("id");
      if (recipeId != null) {
         return loadRecipeById(recipeId, tag.get("type"));
      }

      ItemStack stack = loadItemStack(tag);
      if (stack == null) {
         return null;
      }

      String type = tag.get("type");
      if (type != null) {
         IStackRecipes recipes = RecipeLookupHelper.handlerTypes.get(type);
         if (recipes == null) {
            BCLog.logger.warn("[lib.guide.loader.xml] Unknown recipe type " + type + " - must be one of " + RecipeLookupHelper.handlerTypes.keySet());
         } else {
            List<GuidePartFactory> list = recipes.getRecipes(stack);
            if (list.size() > 0) {
               return list.get(0);
            }
         }
      }

      List<GuidePartFactory> list = RecipeLookupHelper.getAllRecipes(stack);
      return list.isEmpty() ? null : list.get(0);
   }

   @Nullable
   private static GuidePartFactory loadRecipeById(String id, @Nullable String type) {
      if ("assembling".equals(type)) {
         GuidePartFactory factory = GuideAssemblyRecipes.getFactoryByName(id.trim());
         if (factory == null) {
            BCLog.logger.warn("[lib.guide.loader.xml] No assembly recipe found with name " + id);
         }

         return factory;
      } else {
         Identifier rl = Identifier.tryParse(id.trim());
         if (rl == null) {
            BCLog.logger.warn("[lib.guide.loader.xml] " + id + " is not a valid recipe id!");
            return null;
         }

         RecipeManager manager = GuideCraftingRecipes.getRecipeManager();
         if (manager == null) {
            return null;
         }

         for (RecipeHolder<?> holder : manager.getRecipes()) {
            if (RegistryKeyUtil.id(holder.id()).equals(rl)) {
               if (holder.value() instanceof CraftingRecipe crafting) {
                  return GuideCraftingFactory.getFactory(crafting);
               }

               BCLog.logger.warn("[lib.guide.loader.xml] Recipe " + id + " is not a crafting recipe!");
               return null;
            }
         }

         BCLog.logger.warn("[lib.guide.loader.xml] No recipe found with id " + id);
         return null;
      }
   }

   private static GuidePartFactory loadRecipeCycle(XmlPageLoader.XmlTag tag, ProfilerFiller prof) {
      String match = tag.get("match");
      if (match != null && !match.isEmpty()) {
         boolean assembling = "assembling".equals(tag.get("type"));
         GuidePartFactory factory = assembling
            ? GuideAssemblyRecipes.getCyclingFactoryByNameMatch(match)
            : GuideCraftingRecipes.getCyclingFactoryByIdMatch(match);
         if (factory == null) {
            BCLog.logger
               .warn("[lib.guide.loader.xml] <recipe_cycle> matched no " + (assembling ? "assembly" : "crafting") + " recipes for match=\"" + match + "\"");
         }

         return factory;
      } else {
         BCLog.logger.warn("[lib.guide.loader.xml] <recipe_cycle> needs a non-empty `match` (a recipe id/name substring): " + tag);
         return null;
      }
   }

   private static List<GuidePartFactory> loadAllRecipes(XmlPageLoader.XmlTag tag, ProfilerFiller prof) {
      ItemStack stack = loadItemStack(tag);
      return stack == null ? null : RecipeLookupHelper.getAllRecipes(stack);
   }

   private static List<GuidePartFactory> loadAllUsages(XmlPageLoader.XmlTag tag, ProfilerFiller prof) {
      ItemStack stack = loadItemStack(tag);
      return stack == null ? null : RecipeLookupHelper.getAllUsages(stack);
   }

   private static List<GuidePartFactory> loadAllRecipesAndUsages(XmlPageLoader.XmlTag tag, ProfilerFiller prof) {
      ItemStack stack = loadItemStack(tag);
      if (stack == null) {
         return null;
      }

      String chapterLevelStr = tag.get("chapter_level");
      int chapterLevel = 0;
      if (chapterLevelStr != null) {
         try {
            chapterLevel = Integer.parseInt(chapterLevelStr);
         } catch (NumberFormatException nfe) {
            String str = "§4" + tag.originalString + "§r";
            str = str.replace(chapterLevelStr, "§c" + chapterLevelStr + "§4");
            return Collections.singletonList(new XmlPageLoader.GuideTextFactory(str));
         }
      }

      return loadAllCrafting(stack, chapterLevel);
   }

   public static List<GuidePartFactory> loadAllCrafting(@Nonnull ItemStack stack, int chapterLevel) {
      List<GuidePartFactory> list = new ArrayList<>();
      List<GuidePartFactory> recipeParts = RecipeLookupHelper.getAllRecipes(stack);
      if (recipeParts.size() > 0) {
         list.add(gui -> new GuidePartNewPage(gui, 30));
         if (recipeParts.size() == 1) {
            list.add(chapter("buildcraft.guide.recipe.create", chapterLevel));
         } else {
            list.add(chapter("buildcraft.guide.recipe.create.plural", chapterLevel));
         }

         list.addAll(recipeParts);
      }

      List<GuidePartFactory> usageParts = RecipeLookupHelper.getAllUsages(stack);
      usageParts.removeAll(recipeParts);
      if (usageParts.size() > 0) {
         if (recipeParts.size() != 1) {
            list.add(gui -> new GuidePartNewPage(gui, 30));
         }

         if (usageParts.size() == 1) {
            list.add(chapter("buildcraft.guide.recipe.use", chapterLevel));
         } else {
            list.add(chapter("buildcraft.guide.recipe.use.plural", chapterLevel));
         }

         list.addAll(usageParts);
      }

      return list;
   }

   public static void appendAllCrafting(ItemStack stack, List<GuidePart> parts, GuiGuide gui) {
      List<GuidePartFactory> recipeFactories = RecipeLookupHelper.getAllRecipes(stack);
      List<GuidePart> recipeParts = new ArrayList<>();

      for (GuidePartFactory factory : recipeFactories) {
         recipeParts.add(factory.createNew(gui));
      }

      recipeParts.removeAll(parts);
      if (recipeParts.size() > 0) {
         parts.add(new GuidePartNewPage(gui, 30));
         if (recipeParts.size() == 1) {
            parts.add(chapter("buildcraft.guide.recipe.create", 0).createNew(gui));
         } else {
            parts.add(chapter("buildcraft.guide.recipe.create.plural", 0).createNew(gui));
         }

         parts.addAll(recipeParts);
      }

      List<GuidePartFactory> usageFactories = RecipeLookupHelper.getAllUsages(stack);
      List<GuidePart> usageParts = new ArrayList<>();

      for (GuidePartFactory factory : usageFactories) {
         usageParts.add(factory.createNew(gui));
      }

      usageParts.removeAll(parts);
      if (usageParts.size() > 0) {
         if (usageParts.size() != 1) {
            parts.add(new GuidePartNewPage(gui, 30));
         }

         if (usageParts.size() == 1) {
            parts.add(chapter("buildcraft.guide.recipe.use", 0).createNew(gui));
         } else {
            parts.add(chapter("buildcraft.guide.recipe.use.plural", 0).createNew(gui));
         }

         parts.addAll(usageParts);
      }
   }

   public static GuidePartFactory chapter(String after) {
      return chapter(after, 0);
   }

   public static GuidePartFactory chapter(String after, int level) {
      return gui -> new GuideChapterWithin(gui, level, LocaleUtil.localize(after));
   }

   public static GuidePartFactory translate(String text) {
      return gui -> new GuideText(gui, new PageLine(0, LocaleUtil.localize(text), false));
   }

   public static GuidePartFactory loadGroup(XmlPageLoader.XmlTag tag, ProfilerFiller prof) {
      String domain = tag.get("domain");
      String group = tag.get("group");
      if (domain == null) {
         BCLog.logger.warn("[lib.guide.loader.xml] Missing domain tag in " + tag);
      }

      if (group == null) {
         BCLog.logger.warn("[lib.guide.loader.xml] Missing group tag in " + tag);
      }

      if (domain != null && group != null) {
         GuideGroupSet set = GuideGroupManager.get(domain, group);
         if (set == null) {
            BCLog.logger.warn("[lib.guide.loader.xml] Unknown group " + domain + ":" + group);
            return null;
         } else {
            String dirAttr = tag.get("direction");
            GuideGroupSet.GroupDirection direction = "from".equalsIgnoreCase(dirAttr)
               ? GuideGroupSet.GroupDirection.ENTRY_TO_SRC
               : GuideGroupSet.GroupDirection.SRC_TO_ENTRY;
            return gui -> new GuidePartGroup(gui, set, direction);
         }
      } else {
         return null;
      }
   }

   public static ItemStack loadItemStack(XmlPageLoader.XmlTag tag) {
      String id = tag.get("stack");
      String count = tag.get("count");
      String nbt = tag.get("nbt");
      if (id == null) {
         BCLog.logger.warn("[lib.guide.loader.xml] Missing 'stack' for an itemstack from " + tag);
         return null;
      }

      Identifier itemId = Identifier.parse(id.trim());
      Optional<Item> optionalItem = BuiltInRegistries.ITEM.getOptional(itemId);
      if (optionalItem.isEmpty()) {
         BCLog.logger.warn("[lib.guide.loader.xml] " + id + " was not a valid item!");
         return null;
      }

      ItemStack stack = new ItemStack((ItemLike)optionalItem.get());
      if (count != null) {
         int stackSize = 1;

         try {
            stackSize = Integer.parseInt(count.trim());
         } catch (NumberFormatException nfe) {
            BCLog.logger.warn("[lib.guide.loader.xml] " + count + " was not a valid number: " + nfe.getMessage());
         }

         stack.setCount(stackSize);
      }

      if (nbt != null) {
         BCLog.logger.info("[lib.guide.loader.xml] NBT attribute on item stacks is not supported in 1.21, ignoring: " + nbt);
      }

      return stack;
   }

   static {
      putDuelMultiPartType("lore", () -> SHOW_LORE);
      putDuelMultiPartType("detail", () -> shouldShowDetail());
      putDuelMultiPartType("hint", () -> SHOW_HINTS);
      putSingle("new_page", (attr, prof) -> GuidePartNewPage::new);
      putSingle("chapter", XmlPageLoader::loadChapter);
      putSingle("recipe", XmlPageLoader::loadRecipe);
      putSingle("recipe_cycle", XmlPageLoader::loadRecipeCycle);
      putSingle("group", XmlPageLoader::loadGroup);
      putSingle("link", XmlPageLoader::loadLink);
      putMulti("recipes", XmlPageLoader::loadAllRecipes);
      putMulti("usages", XmlPageLoader::loadAllUsages);
      putMulti("recipes_usages", XmlPageLoader::loadAllRecipesAndUsages);
      putSingle("image", XmlPageLoader::loadImage);
      putCode("json_insn");
      putCode("guide_md");
   }

   private static final class GuideTextFactory implements GuidePartFactory {
      public final String text;

      private GuideTextFactory(String text) {
         this.text = text;
      }

      @Override
      public GuidePart createNew(GuiGuide gui) {
         return new GuideText(gui, this.text);
      }
   }

   @FunctionalInterface
   public interface MultiPartJoiner {
      GuidePartFactory join(XmlPageLoader.XmlTag var1, List<GuidePartFactory> var2, ProfilerFiller var3);
   }

   private static final class ResolvedLink {
      final String title;
      final Function<GuiGuide, GuidePageFactory> factoryFn;

      ResolvedLink(String title, Function<GuiGuide, GuidePageFactory> factoryFn) {
         this.title = title;
         this.factoryFn = factoryFn;
      }
   }

   @FunctionalInterface
   public interface SpecialParser {
      List<GuidePartFactory> parse(XmlPageLoader.XmlTag var1, ProfilerFiller var2);
   }

   @FunctionalInterface
   public interface SpecialParserSingle extends XmlPageLoader.SpecialParser {
      @Override
      default List<GuidePartFactory> parse(XmlPageLoader.XmlTag tag, ProfilerFiller prof) {
         GuidePartFactory single = this.parseSingle(tag, prof);
         return single == null ? null : ImmutableList.of(single);
      }

      GuidePartFactory parseSingle(XmlPageLoader.XmlTag var1, ProfilerFiller var2);
   }

   public static class XmlTag {
      public final String name;
      public final Map<String, String> attributes;
      public final XmlPageLoader.XmlTagState state;
      public final String originalString;

      public XmlTag(String name, Map<String, String> attributes, XmlPageLoader.XmlTagState state, String originalString) {
         this.name = name;
         this.attributes = attributes;
         this.state = state;
         this.originalString = originalString;
      }

      @Nullable
      public String get(String key) {
         return this.attributes.get(key);
      }

      @Override
      public String toString() {
         return this.originalString;
      }
   }

   public enum XmlTagState {
      START,
      COMPLETE,
      END;
   }
}
