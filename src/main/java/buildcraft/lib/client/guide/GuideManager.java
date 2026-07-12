/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.api.registry.EventBuildCraftReload;
import buildcraft.api.statements.IStatement;
import buildcraft.builders.BCBuildersStatements;
import buildcraft.lib.client.guide.block.BlockGuidePageMapperRegistry;
import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.client.guide.entry.IEntryLinkConsumer;
import buildcraft.lib.client.guide.entry.ItemStackValueFilter;
import buildcraft.lib.client.guide.entry.PageEntry;
import buildcraft.lib.client.guide.entry.PageEntryExternal;
import buildcraft.lib.client.guide.entry.PageValue;
import buildcraft.lib.client.guide.entry.PageValueType;
import buildcraft.lib.client.guide.loader.IPageLoader;
import buildcraft.lib.client.guide.loader.MarkdownPageLoader;
import buildcraft.lib.client.guide.loader.XmlPageLoader;
import buildcraft.lib.client.guide.parts.GuidePage;
import buildcraft.lib.client.guide.parts.GuidePageEntry;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.client.guide.parts.GuideRecipeFallbackPage;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.client.guide.parts.GuidePartGroup;
import buildcraft.lib.client.guide.parts.GuidePartLink;
import buildcraft.lib.client.guide.parts.contents.ContentsNode;
import buildcraft.lib.client.guide.parts.contents.ContentsNodeGui;
import buildcraft.lib.client.guide.parts.contents.GuidePageContents;
import buildcraft.lib.client.guide.parts.contents.IContentsNode;
import buildcraft.lib.client.guide.parts.contents.PageLink;
import buildcraft.lib.client.guide.parts.contents.PageLinkItemStack;
import buildcraft.lib.client.guide.parts.contents.PageLinkNormal;
import buildcraft.lib.client.guide.ref.GuideGroupManager;
import buildcraft.lib.client.guide.ref.GuideGroupSet;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.statement.GuiElementStatementSource;
import buildcraft.lib.guide.GuideBook;
import buildcraft.lib.guide.GuideBookRegistry;
import buildcraft.lib.guide.GuideContentsData;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.search.ISuffixArray;
import buildcraft.lib.misc.search.SimpleSuffixArray;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.BCTransportStatements;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public enum GuideManager {
   INSTANCE;

   public static final String DEFAULT_LANG = "en_us";
   public static final Map<String, IPageLoader> PAGE_LOADERS = new HashMap<>();
   public static final GuideContentsData BOOK_ALL_DATA = new GuideContentsData(null);
   public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.guide.loader");
   private final List<PageEntry<?>> entries = new ArrayList<>();
   private final Map<Identifier, GuidePageFactory> pages = new HashMap<>();
   // Keyed by ItemStackKey: ItemStack has identity equals/hashCode, so a raw-stack key never hits and the
   // "cache" grew one entry per lookup for the whole session.
   private final Map<ItemStackKey, GuidePageFactory> generatedPages = new HashMap<>();
   public ISuffixArray<PageLink> quickSearcher;
   private final Set<PageLink> pageLinksAdded = new HashSet<>();
   private final Map<GuideBook, Map<TypeOrder, ContentsNode>> contents = new HashMap<>();
   private final Map<Identifier, PageLink> categoryLinks = new HashMap<>();
   private final Map<Identifier, List<GuidePartFactory>> categoryBodies = new HashMap<>();
   private final Set<IStatement> hiddenStatements = new HashSet<>();
   public final Set<Object> objectsAdded = new HashSet<>();
   private boolean isInReload = false;
   private volatile int reloadGeneration = 0;
   private static final String[][] CATEGORY_BODY_SOURCES = new String[][]{
      {"buildcraft", "filler_patterns", "buildcraft:concept/filler_patterns"},
      {"buildcraft", "extraction_presets", "buildcraft:concept/emzuli_extraction_presets"},
      {"buildcraft", "pipe_signals", "buildcraft:concept/pipe_signals"},
      {"buildcraft", "set_pipe_direction", "buildcraft:concept/set_pipe_direction"},
      {"buildcraft", "paint_pipe_colour", "buildcraft:action/pipe_colour"},
      {"buildcraft", "set_power_limit", "buildcraft:concept/set_power_limit"}
   };

   public int getReloadGeneration() {
      return this.reloadGeneration;
   }

   @Nullable
   public PageLink getCategoryLink(Identifier id) {
      return this.categoryLinks.get(id);
   }

   public boolean isStatementHiddenByCategory(IStatement statement) {
      return this.hiddenStatements.contains(statement);
   }

   public void onRegistryReload(EventBuildCraftReload.FinishLoad event) {
      if (!this.isInReload) {
         if (!event.manager.isLoadingAll()) {
            if (event.reloadingRegistries.contains(GuideBookRegistry.INSTANCE)) {
               if (Minecraft.getInstance().level != null) {
                  this.reload();
               }
            }
         }
      }
   }

   public void onResourceManagerReload(ResourceManager resourceManager) {
      if (!this.contents.isEmpty()) {
         this.reload(resourceManager);
      }
   }

   public void reload() {
      this.reload(Minecraft.getInstance().getResourceManager());
   }

   public void ensureLoaded() {
      if (this.contents.isEmpty()) {
         this.reload();
      }
   }

   private void reload(ResourceManager resourceManager) {
      if (this.isInReload) {
         throw new IllegalStateException("Cannot reload while we are reloading!");
      }

      try {
         this.isInReload = true;
         this.reload0(resourceManager);
      } finally {
         this.isInReload = false;
      }
   }

   private void reload0(ResourceManager resourceManager) {
      Stopwatch watch = Stopwatch.createStarted();
      GuideBookRegistry.INSTANCE.reload();
      GuidePageRegistry.INSTANCE.reload(resourceManager);
      this.entries.clear();
      GuidePageRegistry manager = GuidePageRegistry.INSTANCE;
      Map<GuideBook, Set<String>> domains = new HashMap<>();
      domains.put(null, new HashSet<>());

      for (GuideBook book : GuideBookRegistry.INSTANCE.getAllEntries()) {
         domains.put(book, new HashSet<>());
      }

      for (PageEntry<?> entry : manager.getAllEntries()) {
         domains.get(null).add(entry.typeTags.domain);
         GuideBook book = GuideBookRegistry.INSTANCE.getBook(entry.book.toString());
         Set<String> domainSet = domains.get(book);
         if (domainSet != null && book != null) {
            domainSet.add(entry.typeTags.domain);
         }

         this.entries.add(entry);
      }

      BOOK_ALL_DATA.generate(domains.get(null));

      for (Entry<GuideBook, Set<String>> entry : domains.entrySet()) {
         if (entry.getKey() != null) {
            entry.getKey().data.generate(entry.getValue());
         }
      }

      this.pages.clear();
      String currentLanguage = Minecraft.getInstance().getLanguageManager().getSelected();
      String langCode;
      if (currentLanguage == null) {
         BCLog.logger.warn("Current language was null!");
         langCode = "en_us";
      } else {
         langCode = currentLanguage;
      }

      this.loadLangInternal(resourceManager, "en_us");
      if (!"en_us".equals(langCode)) {
         this.loadLangInternal(resourceManager, langCode);
      }

      GuideGroupManager.populateDefaultGroups();
      this.loadCategoryBodies(resourceManager, langCode);
      this.generateContentsPage();
      watch.stop();
      long time = watch.elapsed(TimeUnit.MICROSECONDS);
      int p = this.entries.size();
      int a = this.pages.size();
      int e = p - a;
      BCLog.logger.info("[lib.guide] Loaded " + p + " possible and " + a + " actual guide pages (" + e + " not found) in " + time / 1000L + "ms.");
      this.reloadGeneration++;
   }

   private void loadLangInternal(ResourceManager resourceManager, String lang) {
      for (Entry<Object, PageEntry<?>> mapEntry : GuidePageRegistry.INSTANCE.getReloadableEntryMap().entrySet()) {
         Identifier entryKey = (Identifier)mapEntry.getKey();
         String domain = entryKey.getNamespace();
         String path = "compat/buildcraft/guide/" + lang + "/" + entryKey.getPath();
         boolean loadedPage = false;

         for (Entry<String, IPageLoader> entry : PAGE_LOADERS.entrySet()) {
            Identifier fLoc = Identifier.fromNamespaceAndPath(domain, path + "." + entry.getKey());

            try {
               Optional<Resource> resource = resourceManager.getResource(fLoc);
               if (resource.isPresent()) {
                  byte[] bytes;
                  try (InputStream stream = resource.get().open()) {
                     bytes = stream.readAllBytes();
                  }

                  if (bytes.length != 0 && !new String(bytes, StandardCharsets.UTF_8).trim().isEmpty()) {
                     // Defer the (expensive) markdown parse + recipe-usage scan until this page is actually
                     // opened, then cache it. Parsing every page up front is what made the first book open stall
                     // for hundreds of ms; now only the page you click pays its own parse cost, once.
                     this.pages.put(entryKey, lazyPage(entry.getValue(), bytes, entryKey, mapEntry.getValue()));
                     loadedPage = true;
                     break;
                  }

                  if (DEBUG) {
                     BCLog.logger.info("[lib.guide.loader] Empty page '" + entryKey + "' — using stub.");
                  }
                  break;
               }
            } catch (IOException io) {
               BCLog.logger.warn("[lib.guide.loader] Failed to load guide page '" + entryKey + "'", io);
            }
         }

         if (loadedPage) {
            continue;
         }

         if (!this.pages.containsKey(entryKey)) {
            this.pages.put(entryKey, stubFactory(entryKey, mapEntry.getValue()));
            if (DEBUG) {
               BCLog.logger.info("[lib.guide.loader] Generated stub page for '" + entryKey + "'.");
            }
         }
      }
   }

   /**
    * A lazily-parsed page: the markdown is parsed — and its {@code <recipes_usages>} tags resolved against the
    * whole recipe manager — only the first time the page is opened, then cached. This keeps the first book open
    * cheap; each page pays its own parse cost once, on demand.
    */
   private GuidePageFactory lazyPage(IPageLoader loader, byte[] bytes, Identifier entryKey, PageEntry<?> entry) {
      GuidePageFactory[] cached = new GuidePageFactory[1];
      return gui -> {
         if (cached[0] == null) {
            try (InputStream stream = new ByteArrayInputStream(bytes)) {
               cached[0] = loader.loadPage(stream, entryKey, entry, InactiveProfiler.INSTANCE);
            } catch (Exception ex) {
               BCLog.logger.warn("[lib.guide.loader] Failed to parse guide page '" + entryKey + "' on open.", ex);
               cached[0] = stubFactory(entryKey, entry);
            }
         }

         return cached[0].createNew(gui);
      };
   }

   /** A "not written yet" placeholder page, also used as a fallback when a real page fails to parse. */
   private GuidePageFactory stubFactory(Identifier entryKey, PageEntry<?> entry) {
      String title = entry.title != null ? entry.title : entryKey.getPath();
      String stubContent = "<chapter name=\"" + title + " (WIP)\"/>\nThis guide book entry is a placeholder and has not been written yet.\n";
      try (BufferedReader stubReader = new BufferedReader(new StringReader(stubContent))) {
         return XmlPageLoader.INSTANCE.loadPage(stubReader, entryKey, entry, InactiveProfiler.INSTANCE);
      } catch (IOException io) {
         BCLog.logger.warn("[lib.guide.loader] Stub synthesis failed for '" + entryKey + "'.", io);
         return gui -> new GuidePageEntry(gui, Collections.emptyList(), entry, entryKey);
      }
   }

   private void generateContentsPage() {
      this.objectsAdded.clear();
      this.contents.clear();
      this.populateHiddenStatements();
      this.genTypeMap(null);

      for (GuideBook book : GuideBookRegistry.INSTANCE.getAllEntries()) {
         this.genTypeMap(book);
      }

      this.quickSearcher = new SimpleSuffixArray<>();
      this.pageLinksAdded.clear();

      for (Entry<Object, PageEntry<?>> mapEntry : GuidePageRegistry.INSTANCE.getReloadableEntryMap().entrySet()) {
         Identifier partialLocation = (Identifier)mapEntry.getKey();
         GuidePageFactory entryFactory = INSTANCE.getFactoryFor(partialLocation);
         PageEntry<?> entry = mapEntry.getValue();
         Object basicValue = entry.getBasicValue();
         boolean hidden = basicValue instanceof IStatement stmt && this.isStatementHiddenByCategory(stmt);
         String translatedTitle = entry.title;
         ISimpleDrawable icon = entry.createDrawable();
         PageLine line = new PageLine(icon, icon, 2, translatedTitle, true);
         if (entryFactory != null) {
            this.objectsAdded.add(basicValue);
            PageLinkNormal pageLink = new PageLinkNormal(line, !hidden, entry.getTooltip(), entryFactory, entry.creativeOnly);
            pageLink.setSortIndex(entry.sortIndex);
            this.addChild(entry.book, entry.typeTags, pageLink);
         }
      }

      IEntryLinkConsumer adder = (tags, page) -> this.fileExtraEntry(tags.type, null, page);

      for (PageValueType<?> type : GuidePageRegistry.INSTANCE.types.values()) {
         type.iterateAllDefault(adder, InactiveProfiler.INSTANCE);
      }

      this.addCategoryEntries(adder);
      this.quickSearcher.generate(InactiveProfiler.INSTANCE);

      for (Map<TypeOrder, ContentsNode> map : this.contents.values()) {
         for (ContentsNode node : map.values()) {
            node.sort();
         }
      }
   }

   private void populateHiddenStatements() {
      this.hiddenStatements.clear();

      for (String[] row : CATEGORY_BODY_SOURCES) {
         GuideGroupSet set = GuideGroupManager.get(row[0], row[1]);
         if (set != null) {
            for (PageValue<?> entry : set.entries) {
               if (entry.value instanceof IStatement stmt) {
                  this.hiddenStatements.add(stmt);
               }
            }
         }
      }
   }

   private void loadCategoryBodies(ResourceManager rm, String langCode) {
      this.categoryBodies.clear();

      for (String[] row : CATEGORY_BODY_SOURCES) {
         String domain = row[0];
         String groupName = row[1];
         Identifier mdRel = Identifier.parse(row[2]);
         List<GuidePartFactory> factories = this.tryLoadCategoryBody(rm, mdRel, langCode);
         if (factories == null && !"en_us".equals(langCode)) {
            factories = this.tryLoadCategoryBody(rm, mdRel, "en_us");
         }

         if (factories != null) {
            this.categoryBodies.put(Identifier.fromNamespaceAndPath(domain, groupName), factories);
         } else {
            BCLog.logger
               .warn(
                  "[lib.guide] Missing category body markdown at "
                     + mdRel
                     + " — the "
                     + domain
                     + ":"
                     + groupName
                     + " category page will render without its description."
               );
         }
      }
   }

   @Nullable
   private List<GuidePartFactory> tryLoadCategoryBody(ResourceManager rm, Identifier mdRel, String lang) {
      Identifier full = Identifier.fromNamespaceAndPath(mdRel.getNamespace(), "compat/buildcraft/guide/" + lang + "/" + mdRel.getPath() + ".md");

      try {
         Optional<Resource> resource = rm.getResource(full);
         if (resource.isEmpty()) {
            return null;
         }

         try (
            InputStream in = resource.get().open();
            InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
         ) {
            return MarkdownPageLoader.INSTANCE.loadParts(br, InactiveProfiler.INSTANCE);
         }
      } catch (IOException io) {
         BCLog.logger.warn("[lib.guide] Failed to read category body " + full + ": " + io);
         return null;
      }
   }

   private void addCategoryEntries(IEntryLinkConsumer adder) {
      this.categoryLinks.clear();
      this.addFillerPatternsCategory(adder);
      this.addEmzuliExtractionPresetsCategory(adder);
      this.addPipeSignalsCategory(adder);
      this.addSetPipeDirectionCategory(adder);
      this.addPaintPipeColourCategory(adder);
      this.addSetPowerLimitCategory(adder);
   }

   private void fileExtraEntry(String chapterKey, @Nullable String subtypeKey, PageLink page) {
      if (this.pageLinksAdded.add(page)) {
         this.quickSearcher.add(page, page.getSearchName());
      }

      String chapterTitle = LocaleUtil.localize(chapterKey);
      String subtypeTitle = subtypeKey != null && !subtypeKey.isEmpty() ? LocaleUtil.localize(subtypeKey) : null;
      String modTitle = LocaleUtil.localize(ETypeTag.MOD.preText + "buildcraft");

      for (Entry<GuideBook, Map<TypeOrder, ContentsNode>> bookEntry : this.contents.entrySet()) {
         GuideBook book = bookEntry.getKey();
         if (book == null || book.appendAllEntries) {
            for (Entry<TypeOrder, ContentsNode> orderEntry : bookEntry.getValue().entrySet()) {
               placeExtraEntryInOrder(orderEntry.getKey(), orderEntry.getValue(), modTitle, chapterTitle, subtypeTitle, page);
            }
         }
      }
   }

   static void placeExtraEntryInOrder(
      TypeOrder order, ContentsNode root, String modTitle, String chapterTitle, @Nullable String subtypeTitle, IContentsNode leaf
   ) {
      ContentsNode node = root;
      int indent = 0;
      UnmodifiableIterator var8 = order.tags.iterator();

      while (var8.hasNext()) {
         ETypeTag tag = (ETypeTag)var8.next();

         String title = switch (tag) {
            case MOD -> modTitle;
            case TYPE -> chapterTitle;
            case SUB_TYPE -> subtypeTitle;
            case SUB_MOD -> null;
         };
         if (title != null && !title.isEmpty()) {
            node = getOrCreateChapter(node, title, indent++);
         }
      }

      node.addChild(leaf);
   }

   private void addToChapterSubtype(String chapterKey, @Nullable String subtypeKey, PageLink page) {
      this.fileExtraEntry(chapterKey, subtypeKey, page);
   }

   private static ContentsNode getOrCreateChapter(ContentsNode parent, String title, int indent) {
      IContentsNode subNode = parent.getChild(title);
      if (subNode instanceof ContentsNode) {
         return (ContentsNode)subNode;
      } else if (subNode == null) {
         ContentsNode created = new ContentsNode(title, indent);
         parent.addChild(created);
         return created;
      } else {
         throw new IllegalStateException("Unknown node type " + subNode.getClass());
      }
   }

   private void registerCategory(
      IEntryLinkConsumer adder,
      String domain,
      String groupName,
      String[] chapterTagTypes,
      @Nullable String[] chapterSubtypes,
      ISimpleDrawable icon,
      String title,
      @Nullable Function<GuiGuide, List<GuidePart>> extraParts
   ) {
      GuideGroupSet groupSet = GuideGroupManager.get(domain, groupName);
      if (groupSet != null) {
         Identifier groupId = Identifier.fromNamespaceAndPath(domain, groupName);
         PageLine line = new PageLine(icon, icon, 2, title, true);
         GuidePageFactory factory = g -> {
            List<GuidePart> parts = new ArrayList<>();
            List<GuidePartFactory> bodyFactories = this.categoryBodies.get(groupId);
            if (bodyFactories != null) {
               for (GuidePartFactory bf : bodyFactories) {
                  GuidePart part = bf.createNew(g);
                  if (part != null) {
                     parts.add(part);
                  }
               }
            }

            if (extraParts != null) {
               parts.addAll(extraParts.apply(g));
            }

            parts.add(new GuidePartGroup(g, groupSet, GuideGroupSet.GroupDirection.SRC_TO_ENTRY));
            return new GuidePage(g, parts, new PageValue<>(PageEntryExternal.INSTANCE, title));
         };
         PageLinkNormal link = new PageLinkNormal(line, true, ImmutableList.of(title), factory);
         this.categoryLinks.put(groupId, link);

         for (int i = 0; i < chapterTagTypes.length; i++) {
            String chapterKey = chapterTagTypes[i];
            String subtypeKey = chapterSubtypes != null && i < chapterSubtypes.length ? chapterSubtypes[i] : null;
            if (subtypeKey != null && !subtypeKey.isEmpty()) {
               this.addToChapterSubtype(chapterKey, subtypeKey, link);
            } else {
               adder.addChild(new JsonTypeTags(chapterKey), link);
            }
         }
      }
   }

   private void addFillerPatternsCategory(IEntryLinkConsumer adder) {
      ISimpleDrawable icon = (x, y) -> GuiElementStatementSource.drawGuiSlot(BCBuildersStatements.PATTERN_STAIRS, x, y);
      this.registerCategory(
         adder,
         "buildcraft",
         "filler_patterns",
         new String[]{"buildcraft.guide.contents.actions"},
         new String[]{"buildcraft.guide.chapter.subtype.automation"},
         icon,
         "Filler Patterns",
         null
      );
   }

   private void addEmzuliExtractionPresetsCategory(IEntryLinkConsumer adder) {
      ISimpleDrawable icon = (x, y) -> GuiElementStatementSource.drawGuiSlot(BCTransportStatements.ACTION_EXTRACTION_PRESET[0], x, y);
      this.registerCategory(
         adder,
         "buildcraft",
         "extraction_presets",
         new String[]{"buildcraft.guide.contents.actions"},
         new String[]{"buildcraft.guide.chapter.subtype.pipe_item"},
         icon,
         "Emzuli Extraction Presets",
         g -> {
            ItemStack emzuliStack = new ItemStack(BCTransportItems.PIPE_EMZULI_ITEM);
            PageLink emzuliLink = PageLinkItemStack.create(true, emzuliStack, InactiveProfiler.INSTANCE);
            return ImmutableList.of(new GuidePartLink(g, emzuliLink));
         }
      );
   }

   private void addPipeSignalsCategory(IEntryLinkConsumer adder) {
      ISimpleDrawable icon = (x, y) -> GuiElementStatementSource.drawGuiSlot(BCTransportStatements.ACTION_PIPE_SIGNAL[DyeColor.BLACK.ordinal()], x, y);
      this.registerCategory(
         adder,
         "buildcraft",
         "pipe_signals",
         new String[]{"buildcraft.guide.contents.triggers", "buildcraft.guide.contents.actions"},
         new String[]{"buildcraft.guide.chapter.subtype.pipe_plug", "buildcraft.guide.chapter.subtype.pipe_plug"},
         icon,
         "Pipe Signals",
         null
      );
   }

   private void addSetPipeDirectionCategory(IEntryLinkConsumer adder) {
      SpriteHolderRegistry.SpriteHolder compassFrame = SpriteHolderRegistry.getHolder("minecraft:item/compass_16");
      ISimpleDrawable icon = (x, y) -> {
         BCGraphics graphics = GuiIcon.getGuiGraphics();
         if (graphics != null) {
            TextureAtlasSprite sprite = compassFrame.getSprite();
            if (sprite != null) {
               graphics.blitSprite(sprite, (int)x, (int)y, 16, 16, -1);
            }
         }
      };
      this.registerCategory(
         adder,
         "buildcraft",
         "set_pipe_direction",
         new String[]{"buildcraft.guide.contents.actions"},
         new String[]{"buildcraft.guide.chapter.subtype.pipe_item"},
         icon,
         "Set Pipe Direction",
         null
      );
   }

   private void addPaintPipeColourCategory(IEntryLinkConsumer adder) {
      ISimpleDrawable icon = (x, y) -> GuiElementStatementSource.drawGuiSlot(BCTransportStatements.ACTION_PIPE_COLOUR[DyeColor.BLACK.ordinal()], x, y);
      this.registerCategory(
         adder,
         "buildcraft",
         "paint_pipe_colour",
         new String[]{"buildcraft.guide.contents.actions"},
         new String[]{"buildcraft.guide.chapter.subtype.pipe_item"},
         icon,
         "Paint Passing Items",
         null
      );
   }

   private void addSetPowerLimitCategory(IEntryLinkConsumer adder) {
      ISimpleDrawable icon = (x, y) -> GuiElementStatementSource.drawGuiSlot(BCTransportStatements.ACTION_IRON_POWER_LIMIT[3], x, y);
      this.registerCategory(
         adder,
         "buildcraft",
         "set_power_limit",
         new String[]{"buildcraft.guide.contents.actions"},
         new String[]{"buildcraft.guide.chapter.subtype.pipe_item"},
         icon,
         "Set Power Limit",
         null
      );
   }

   private void genTypeMap(GuideBook book) {
      Map<TypeOrder, ContentsNode> map = new HashMap<>();
      this.contents.put(book, map);

      for (TypeOrder order : GuiGuide.SORTING_TYPES) {
         map.put(order, new ContentsNode("root", -1, order.tags.isEmpty()));
      }
   }

   private void addChild(Identifier bookType, JsonTypeTags tags, PageLink page) {
      if (this.pageLinksAdded.add(page)) {
         this.quickSearcher.add(page, page.getSearchName());
      }

      for (Entry<GuideBook, Map<TypeOrder, ContentsNode>> bookEntry : this.contents.entrySet()) {
         GuideBook book = bookEntry.getKey();
         if (book == null || book.name.equals(bookType)) {
            Map<TypeOrder, ContentsNode> map = bookEntry.getValue();

            for (Entry<TypeOrder, ContentsNode> entry : map.entrySet()) {
               TypeOrder order = entry.getKey();
               String[] ordered = tags.getOrdered(order);
               ContentsNode[] nodePath = new ContentsNode[ordered.length];
               ContentsNode node = entry.getValue();

               for (int i = 0; i < ordered.length; i++) {
                  String title = LocaleUtil.localize(ordered[i]);
                  IContentsNode subNode = node.getChild(title);
                  if (subNode instanceof ContentsNode) {
                     node = (ContentsNode)subNode;
                     nodePath[i] = node;
                  } else {
                     if (subNode != null) {
                        throw new IllegalStateException("Unknown node type " + subNode.getClass());
                     }

                     ContentsNode subContents = new ContentsNode(title, i);
                     node.addChild(subContents);
                     node = subContents;
                     nodePath[i] = node;
                  }
               }

               if (nodePath.length == 0) {
                  node.addChild(page);
               } else {
                  nodePath[nodePath.length - 1].addChild(page);
               }
            }
         }
      }
   }

   @Nullable
   public GuidePageFactory getFactoryFor(Identifier partialLocation) {
      return this.pages.get(partialLocation);
   }

   @Nullable
   public GuidePageFactory getFactoryFor(Object value) {
      if (value instanceof ItemStackValueFilter) {
         value = ((ItemStackValueFilter)value).stack.baseStack;
      } else if (value instanceof ItemStackKey) {
         value = ((ItemStackKey)value).baseStack;
      }

      return value instanceof ItemStack ? this.getPageFor((ItemStack)value) : this.getFactoryFor(getEntryFor(value));
   }

   public static Identifier getEntryFor(Object obj) {
      if (obj instanceof BlockState state) {
         Identifier mapped = BlockGuidePageMapperRegistry.resolvePageId(state);
         if (mapped != null && GuidePageRegistry.INSTANCE.getReloadableEntryMap().containsKey(mapped)) {
            return mapped;
         }
      }

      if (obj instanceof ItemStack stack && !stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
         Identifier mapped = BlockGuidePageMapperRegistry.resolvePageIdFromItemBlock(blockItem.getBlock());
         if (mapped != null && GuidePageRegistry.INSTANCE.getReloadableEntryMap().containsKey(mapped)) {
            return mapped;
         }
      }

      for (Entry<Object, PageEntry<?>> entry : GuidePageRegistry.INSTANCE.getReloadableEntryMap().entrySet()) {
         if (entry.getValue().matches(obj)) {
            return (Identifier)entry.getKey();
         }
      }

      return null;
   }

   @Nonnull
   public GuidePageFactory getPageFor(@Nonnull ItemStack stack) {
      Identifier entry = getEntryFor(stack);
      if (entry != null) {
         GuidePageFactory factory = this.getFactoryFor(entry);
         if (factory != null) {
            return factory;
         }
      }

      return this.generatedPages.computeIfAbsent(new ItemStackKey(stack), key -> GuideRecipeFallbackPage.createFactory(stack));
   }

   public ContentsNodeGui getGuiContents(GuiGuide gui, GuidePageContents guidePageContents, TypeOrder sortingOrder) {
      if (this.contents.isEmpty()) {
         this.reload();
      }

      Map<TypeOrder, ContentsNode> map = this.contents.get(gui.book);
      if (map == null) {
         map = this.contents.get(null);
      }

      if (map == null) {
         throw new IllegalStateException("Unknown book " + gui.book + " and no fallback contents available");
      }

      ContentsNode node = map.get(sortingOrder);
      if (node == null) {
         throw new IllegalStateException("Unknown sorting order " + sortingOrder);
      }

      node.resetVisibility();
      return new ContentsNodeGui(gui, node);
   }

   static {
      PAGE_LOADERS.put("md", MarkdownPageLoader.INSTANCE);
   }
}
