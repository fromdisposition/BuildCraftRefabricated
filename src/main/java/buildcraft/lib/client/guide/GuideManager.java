/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.profiling.ProfilerFiller;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.api.registry.EventBuildCraftReload;
import buildcraft.api.statements.IStatement;

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
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.client.guide.parts.GuidePageStandInRecipes;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.client.guide.parts.GuidePartGroup;
import buildcraft.lib.client.guide.parts.GuidePartLink;
import buildcraft.lib.client.guide.parts.GuideText;
import buildcraft.lib.client.guide.parts.contents.ContentsNode;
import buildcraft.lib.client.guide.parts.contents.ContentsNodeGui;
import buildcraft.lib.client.guide.parts.contents.GuidePageContents;
import buildcraft.lib.client.guide.parts.contents.IContentsNode;
import buildcraft.lib.client.guide.parts.contents.PageLink;
import buildcraft.lib.client.guide.parts.contents.PageLinkItemStack;
import buildcraft.lib.client.guide.parts.contents.PageLinkNormal;
import buildcraft.lib.client.guide.ref.GuideGroupManager;
import buildcraft.lib.client.guide.ref.GuideGroupSet;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.statement.GuiElementStatementSource;
import buildcraft.lib.guide.GuideBook;
import buildcraft.lib.guide.GuideBookRegistry;
import buildcraft.lib.guide.GuideContentsData;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.search.ISuffixArray;
import buildcraft.lib.misc.search.SimpleSuffixArray;

@SuppressWarnings("deprecation")
public enum GuideManager {
    INSTANCE;

    public static final String DEFAULT_LANG = "en_us";
    public static final Map<String, IPageLoader> PAGE_LOADERS = new HashMap<>();
    public static final GuideContentsData BOOK_ALL_DATA = new GuideContentsData(null);
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.guide.loader");

    private final List<PageEntry<?>> entries = new ArrayList<>();

    private final Map<Identifier, GuidePageFactory> pages = new HashMap<>();
    private final Map<ItemStack, GuidePageFactory> generatedPages = new HashMap<>();

    public ISuffixArray<PageLink> quickSearcher;
    private final Set<PageLink> pageLinksAdded = new HashSet<>();
    private final Map<GuideBook, Map<TypeOrder, ContentsNode>> contents = new HashMap<>();

    private final Map<Identifier, PageLink> categoryLinks = new HashMap<>();

    private final Map<Identifier, List<GuidePartFactory>> categoryBodies = new HashMap<>();

    private final Set<buildcraft.api.statements.IStatement> hiddenStatements = new HashSet<>();

    public final Set<Object> objectsAdded = new HashSet<>();

    private boolean isInReload = false;

    private volatile int reloadGeneration = 0;

    public int getReloadGeneration() {
        return reloadGeneration;
    }

    @Nullable
    public PageLink getCategoryLink(Identifier id) {
        return categoryLinks.get(id);
    }

    public boolean isStatementHiddenByCategory(buildcraft.api.statements.IStatement statement) {
        return hiddenStatements.contains(statement);
    }

    static {
        PAGE_LOADERS.put("md", MarkdownPageLoader.INSTANCE);
    }

    public void onRegistryReload(EventBuildCraftReload.FinishLoad event) {
        if (isInReload) return;
        if (event.manager.isLoadingAll()) return;
        if (!event.reloadingRegistries.contains(GuideBookRegistry.INSTANCE)) {
            return;
        }
        // World-creation UI reloads data packs before registries are stable; defer until in-world.
        if (Minecraft.getInstance().level == null) {
            return;
        }
        reload();
    }

    public void onResourceManagerReload(ResourceManager resourceManager) {

        if (contents.isEmpty()) return;
        reload(resourceManager);
    }

    public void reload() {
        reload(Minecraft.getInstance().getResourceManager());
    }

    public void ensureLoaded() {
        if (contents.isEmpty()) {
            reload();
        }
    }

    private void reload(ResourceManager resourceManager) {
        if (isInReload) {
            throw new IllegalStateException("Cannot reload while we are reloading!");
        }
        try {
            isInReload = true;
            reload0(resourceManager);
        } finally {
            isInReload = false;
        }
    }

    private void reload0(ResourceManager resourceManager) {
        Stopwatch watch = Stopwatch.createStarted();

        GuideBookRegistry.INSTANCE.reload();
        GuidePageRegistry.INSTANCE.reload();

        entries.clear();
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
            entries.add(entry);
        }

        BOOK_ALL_DATA.generate(domains.get(null));
        for (Entry<GuideBook, Set<String>> entry : domains.entrySet()) {
            if (entry.getKey() == null) continue;
            entry.getKey().data.generate(entry.getValue());
        }
        pages.clear();

        String currentLanguage = Minecraft.getInstance().getLanguageManager().getSelected();
        String langCode;
        if (currentLanguage == null) {
            BCLog.logger.warn("Current language was null!");
            langCode = DEFAULT_LANG;
        } else {
            langCode = currentLanguage;
        }

        loadLangInternal(resourceManager, DEFAULT_LANG);
        if (!DEFAULT_LANG.equals(langCode)) {
            loadLangInternal(resourceManager, langCode);
        }

        GuideGroupManager.populateDefaultGroups();

        loadCategoryBodies(resourceManager, langCode);

        generateContentsPage();

        watch.stop();
        long time = watch.elapsed(TimeUnit.MICROSECONDS);
        int p = entries.size();
        int a = pages.size();
        int e = p - a;
        BCLog.logger.info(
            "[lib.guide] Loaded " + p + " possible and " + a + " actual guide pages (" + e + " not found) in "
                + time / 1000 + "ms."
        );

        reloadGeneration++;
    }

    @SuppressWarnings("unchecked")
    private void loadLangInternal(ResourceManager resourceManager, String lang) {
        main_iteration:
        for (Entry<Object, PageEntry<?>> mapEntry : GuidePageRegistry.INSTANCE
            .getReloadableEntryMap().entrySet()) {
            Identifier entryKey = (Identifier) mapEntry.getKey();
            String domain = entryKey.getNamespace();
            String path = "compat/buildcraft/guide/" + lang + "/" + entryKey.getPath();

            for (Entry<String, IPageLoader> entry : PAGE_LOADERS.entrySet()) {
                Identifier fLoc = Identifier.fromNamespaceAndPath(domain, path + "." + entry.getKey());

                try {
                    var resource = resourceManager.getResource(fLoc);
                    if (resource.isPresent()) {

                        byte[] bytes;
                        try (InputStream stream = resource.get().open()) {
                            bytes = stream.readAllBytes();
                        }
                        if (bytes.length == 0 || new String(bytes, java.nio.charset.StandardCharsets.UTF_8).trim().isEmpty()) {
                            if (GuideManager.DEBUG) {
                                BCLog.logger.info("[lib.guide.loader] Empty page '" + entryKey + "' — using stub.");
                            }
                            break;
                        }
                        try (InputStream stream = new java.io.ByteArrayInputStream(bytes)) {
                            GuidePageFactory factory = entry.getValue().loadPage(
                                stream, entryKey, mapEntry.getValue(),
                                net.minecraft.util.profiling.InactiveProfiler.INSTANCE
                            );
                            pages.put(entryKey, factory);
                            if (GuideManager.DEBUG) {
                                BCLog.logger.info("[lib.guide.loader] Loaded page '" + entryKey + "'.");
                            }
                            continue main_iteration;
                        }
                    }
                } catch (IOException io) {
                    BCLog.logger.warn("[lib.guide.loader] Failed to load guide page '" + entryKey + "'", io);
                }
            }

            if (pages.containsKey(entryKey)) continue;

            try {
                PageEntry<?> stubEntry = mapEntry.getValue();
                String title = stubEntry.title != null ? stubEntry.title : entryKey.getPath();
                String stubContent =
                    "<chapter name=\"" + title + " (WIP)\"/>\n"
                        + "This guide book entry is a placeholder and has not been written yet.\n";
                try (BufferedReader stubReader = new BufferedReader(new StringReader(stubContent))) {
                    GuidePageFactory factory = XmlPageLoader.INSTANCE.loadPage(
                        stubReader, entryKey, stubEntry,
                        net.minecraft.util.profiling.InactiveProfiler.INSTANCE
                    );
                    pages.put(entryKey, factory);
                }
                if (GuideManager.DEBUG) {
                    BCLog.logger.info("[lib.guide.loader] Generated stub page for '" + entryKey + "'.");
                }
            } catch (IOException io) {
                String endings;
                if (PAGE_LOADERS.size() == 1) {
                    endings = PAGE_LOADERS.keySet().iterator().next();
                } else {
                    endings = PAGE_LOADERS.keySet().toString();
                }
                BCLog.logger.warn(
                    "[lib.guide.loader] Unable to load guide page '" + entryKey + "' (full path = '" + domain + ":"
                        + path + "." + endings + "') and stub synthesis failed!",
                    io
                );
            }
        }
    }

    private void generateContentsPage() {
        objectsAdded.clear();
        contents.clear();

        populateHiddenStatements();
        genTypeMap(null);
        for (GuideBook book : GuideBookRegistry.INSTANCE.getAllEntries()) {
            genTypeMap(book);
        }
        quickSearcher = new SimpleSuffixArray<>();
        pageLinksAdded.clear();

        for (Entry<Object, PageEntry<?>> mapEntry : GuidePageRegistry.INSTANCE.getReloadableEntryMap()
            .entrySet()) {
            Identifier partialLocation = (Identifier) mapEntry.getKey();
            GuidePageFactory entryFactory = GuideManager.INSTANCE.getFactoryFor(partialLocation);

            PageEntry<?> entry = mapEntry.getValue();

            Object basicValue = entry.getBasicValue();
            boolean hidden = basicValue instanceof buildcraft.api.statements.IStatement stmt
                && isStatementHiddenByCategory(stmt);

            String translatedTitle = entry.title;
            ISimpleDrawable icon = entry.createDrawable();
            PageLine line = new PageLine(icon, icon, 2, translatedTitle, true);

            if (entryFactory != null) {
                objectsAdded.add(basicValue);
                PageLinkNormal pageLink = new PageLinkNormal(line, !hidden, entry.getTooltip(), entryFactory,
                    entry.creativeOnly);
                pageLink.setSortIndex(entry.sortIndex);
                addChild(entry.book, entry.typeTags, pageLink);
            }

        }

        final IEntryLinkConsumer adder = (tags, page) -> fileExtraEntry(tags.type, null, page);

        for (PageValueType<?> type : GuidePageRegistry.INSTANCE.types.values()) {
            type.iterateAllDefault(adder, net.minecraft.util.profiling.InactiveProfiler.INSTANCE);
        }

        addCategoryEntries(adder);

        quickSearcher.generate(net.minecraft.util.profiling.InactiveProfiler.INSTANCE);

        for (Map<TypeOrder, ContentsNode> map : contents.values()) {
            for (ContentsNode node : map.values()) {
                node.sort();
            }
        }
    }

    private static final String[][] CATEGORY_BODY_SOURCES = {
        { "buildcraft", "filler_patterns",     "buildcraft:concept/filler_patterns" },
        { "buildcraft", "extraction_presets",  "buildcraft:concept/emzuli_extraction_presets" },
        { "buildcraft", "pipe_signals",        "buildcraft:concept/pipe_signals" },
        { "buildcraft", "set_pipe_direction",  "buildcraft:concept/set_pipe_direction" },
        { "buildcraft", "paint_pipe_colour",   "buildcraft:action/pipe_colour" },
        { "buildcraft", "set_power_limit",     "buildcraft:concept/set_power_limit" },
    };

    private void populateHiddenStatements() {
        hiddenStatements.clear();
        for (String[] row : CATEGORY_BODY_SOURCES) {
            GuideGroupSet set = GuideGroupManager.get(row[0], row[1]);
            if (set == null) continue;
            for (PageValue<?> entry : set.entries) {
                if (entry.value instanceof buildcraft.api.statements.IStatement stmt) {
                    hiddenStatements.add(stmt);
                }
            }
        }
    }

    private void loadCategoryBodies(ResourceManager rm, String langCode) {
        categoryBodies.clear();
        for (String[] row : CATEGORY_BODY_SOURCES) {
            String domain = row[0];
            String groupName = row[1];
            Identifier mdRel = Identifier.parse(row[2]);

            List<GuidePartFactory> factories = tryLoadCategoryBody(rm, mdRel, langCode);
            if (factories == null && !DEFAULT_LANG.equals(langCode)) {
                factories = tryLoadCategoryBody(rm, mdRel, DEFAULT_LANG);
            }
            if (factories != null) {
                categoryBodies.put(Identifier.fromNamespaceAndPath(domain, groupName), factories);
            } else {
                BCLog.logger.warn("[lib.guide] Missing category body markdown at "
                    + mdRel + " — the " + domain + ":" + groupName
                    + " category page will render without its description.");
            }
        }
    }

    @Nullable
    private List<GuidePartFactory> tryLoadCategoryBody(ResourceManager rm, Identifier mdRel, String lang) {
        Identifier full = Identifier.fromNamespaceAndPath(mdRel.getNamespace(),
            "compat/buildcraft/guide/" + lang + "/" + mdRel.getPath() + ".md");
        try {
            var resource = rm.getResource(full);
            if (resource.isEmpty()) return null;
            try (InputStream in = resource.get().open();
                 java.io.InputStreamReader isr =
                     new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)) {
                return MarkdownPageLoader.INSTANCE.loadParts(
                    br, net.minecraft.util.profiling.InactiveProfiler.INSTANCE);
            }
        } catch (IOException io) {
            BCLog.logger.warn("[lib.guide] Failed to read category body " + full + ": " + io);
            return null;
        }
    }

    private void addCategoryEntries(IEntryLinkConsumer adder) {

        categoryLinks.clear();
        if (buildcraft.fabric.guide.GuideFabricSupport.EXTENDED_MODULES) {
            addFillerPatternsCategory(adder);
            addEmzuliExtractionPresetsCategory(adder);
            addPipeSignalsCategory(adder);
            addSetPipeDirectionCategory(adder);
            addPaintPipeColourCategory(adder);
            addSetPowerLimitCategory(adder);
        }
    }

    private void fileExtraEntry(String chapterKey, @Nullable String subtypeKey, PageLink page) {
        if (pageLinksAdded.add(page)) {
            quickSearcher.add(page, page.getSearchName());
        }
        String chapterTitle = LocaleUtil.localize(chapterKey);
        String subtypeTitle = (subtypeKey == null || subtypeKey.isEmpty())
            ? null : LocaleUtil.localize(subtypeKey);

        String modTitle = LocaleUtil.localize(ETypeTag.MOD.preText + "buildcraft");
        for (Entry<GuideBook, Map<TypeOrder, ContentsNode>> bookEntry : contents.entrySet()) {
            @Nullable GuideBook book = bookEntry.getKey();
            if (book != null && !book.appendAllEntries) continue;
            for (Entry<TypeOrder, ContentsNode> orderEntry : bookEntry.getValue().entrySet()) {
                placeExtraEntryInOrder(orderEntry.getKey(), orderEntry.getValue(),
                    modTitle, chapterTitle, subtypeTitle, page);
            }
        }
    }

    static void placeExtraEntryInOrder(TypeOrder order, ContentsNode root, String modTitle,
        String chapterTitle, @Nullable String subtypeTitle, IContentsNode leaf) {
        ContentsNode node = root;
        int indent = 0;
        for (ETypeTag tag : order.tags) {
            String title = switch (tag) {
                case MOD -> modTitle;
                case TYPE -> chapterTitle;
                case SUB_TYPE -> subtypeTitle;
                case SUB_MOD -> null;
            };
            if (title == null || title.isEmpty()) {
                continue;
            }
            node = getOrCreateChapter(node, title, indent++);
        }
        node.addChild(leaf);
    }

    private void addToChapterSubtype(String chapterKey, @Nullable String subtypeKey, PageLink page) {
        fileExtraEntry(chapterKey, subtypeKey, page);
    }

    private static ContentsNode getOrCreateChapter(ContentsNode parent, String title, int indent) {
        IContentsNode subNode = parent.getChild(title);
        if (subNode instanceof ContentsNode) {
            return (ContentsNode) subNode;
        } else if (subNode == null) {
            ContentsNode created = new ContentsNode(title, indent);
            parent.addChild(created);
            return created;
        } else {
            throw new IllegalStateException("Unknown node type " + subNode.getClass());
        }
    }

    private void registerCategory(IEntryLinkConsumer adder, String domain, String groupName,
        String[] chapterTagTypes, @Nullable String[] chapterSubtypes,
        ISimpleDrawable icon, String title,
        @Nullable java.util.function.Function<GuiGuide, List<GuidePart>> extraParts) {
        GuideGroupSet groupSet = GuideGroupManager.get(domain, groupName);
        if (groupSet == null) return;

        Identifier groupId = Identifier.fromNamespaceAndPath(domain, groupName);
        PageLine line = new PageLine(icon, icon, 2, title, true);
        GuidePageFactory factory = g -> {
            List<GuidePart> parts = new ArrayList<>();

            List<GuidePartFactory> bodyFactories = categoryBodies.get(groupId);
            if (bodyFactories != null) {
                for (GuidePartFactory bf : bodyFactories) {
                    GuidePart part = bf.createNew(g);
                    if (part != null) parts.add(part);
                }
            }
            if (extraParts != null) {
                parts.addAll(extraParts.apply(g));
            }
            parts.add(new GuidePartGroup(g, groupSet, GuideGroupSet.GroupDirection.SRC_TO_ENTRY));

            return new GuidePage(g, parts, new PageValue<>(PageEntryExternal.INSTANCE, title));
        };

        PageLinkNormal link = new PageLinkNormal(line, true, ImmutableList.of(title), factory);
        categoryLinks.put(groupId, link);
        for (int i = 0; i < chapterTagTypes.length; i++) {
            String chapterKey = chapterTagTypes[i];
            String subtypeKey = (chapterSubtypes != null && i < chapterSubtypes.length)
                ? chapterSubtypes[i] : null;
            if (subtypeKey == null || subtypeKey.isEmpty()) {
                adder.addChild(new JsonTypeTags(chapterKey), link);
            } else {
                addToChapterSubtype(chapterKey, subtypeKey, link);
            }
        }
    }

    private void addFillerPatternsCategory(IEntryLinkConsumer adder) {

        ISimpleDrawable icon = (x, y) -> GuiElementStatementSource.drawGuiSlot(
            buildcraft.builders.BCBuildersStatements.PATTERN_STAIRS, x, y);
        registerCategory(adder, "buildcraft", "filler_patterns",
            new String[] { "buildcraft.guide.contents.actions" },

            new String[] { "buildcraft.guide.chapter.subtype.automation" },
            icon,
            "Filler Patterns",
            null);
    }

    private void addEmzuliExtractionPresetsCategory(IEntryLinkConsumer adder) {
        ISimpleDrawable icon = (x, y) -> GuiElementStatementSource.drawGuiSlot(

            buildcraft.transport.BCTransportStatements.ACTION_EXTRACTION_PRESET[0], x, y);
        registerCategory(adder, "buildcraft", "extraction_presets",
            new String[] { "buildcraft.guide.contents.actions" },
            new String[] { "buildcraft.guide.chapter.subtype.pipe_item" },
            icon,
            "Emzuli Extraction Presets",
            g -> {
                ItemStack emzuliStack = new ItemStack(
                    buildcraft.transport.BCTransportItems.PIPE_EMZULI_ITEM.get());
                PageLink emzuliLink = PageLinkItemStack.create(true, emzuliStack,
                    net.minecraft.util.profiling.InactiveProfiler.INSTANCE);
                return ImmutableList.of(new GuidePartLink(g, emzuliLink));
            });
    }

    private void addPipeSignalsCategory(IEntryLinkConsumer adder) {

        ISimpleDrawable icon = (x, y) -> GuiElementStatementSource.drawGuiSlot(
            buildcraft.transport.BCTransportStatements.ACTION_PIPE_SIGNAL[
                net.minecraft.world.item.DyeColor.BLACK.ordinal()], x, y);
        registerCategory(adder, "buildcraft", "pipe_signals",
            new String[] {
                "buildcraft.guide.contents.triggers",
                "buildcraft.guide.contents.actions",
            },

            new String[] {
                "buildcraft.guide.chapter.subtype.pipe_plug",
                "buildcraft.guide.chapter.subtype.pipe_plug",
            },
            icon,
            "Pipe Signals",
            null);
    }

    private void addSetPipeDirectionCategory(IEntryLinkConsumer adder) {
        buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder compassFrame =
            buildcraft.lib.client.sprite.SpriteHolderRegistry.getHolder("minecraft:item/compass_16");
        ISimpleDrawable icon = (x, y) -> {
            buildcraft.lib.gui.BCGraphics graphics =
                buildcraft.lib.gui.GuiIcon.getGuiGraphics();
            if (graphics == null) return;
            net.minecraft.client.renderer.texture.TextureAtlasSprite sprite = compassFrame.getSprite();
            if (sprite == null) return;
            graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                sprite, (int) x, (int) y, 16, 16, 0xFFFFFFFF);
        };
        registerCategory(adder, "buildcraft", "set_pipe_direction",
            new String[] { "buildcraft.guide.contents.actions" },
            new String[] { "buildcraft.guide.chapter.subtype.pipe_item" },
            icon,
            "Set Pipe Direction",
            null);
    }

    private void addPaintPipeColourCategory(IEntryLinkConsumer adder) {
        ISimpleDrawable icon = (x, y) -> GuiElementStatementSource.drawGuiSlot(
            buildcraft.transport.BCTransportStatements.ACTION_PIPE_COLOUR[
                net.minecraft.world.item.DyeColor.BLACK.ordinal()], x, y);
        registerCategory(adder, "buildcraft", "paint_pipe_colour",
            new String[] { "buildcraft.guide.contents.actions" },
            new String[] { "buildcraft.guide.chapter.subtype.pipe_item" },
            icon,
            "Paint Passing Items",
            null);
    }

    private void addSetPowerLimitCategory(IEntryLinkConsumer adder) {

        ISimpleDrawable icon = (x, y) -> GuiElementStatementSource.drawGuiSlot(
            buildcraft.transport.BCTransportStatements.ACTION_IRON_POWER_LIMIT[3], x, y);
        registerCategory(adder, "buildcraft", "set_power_limit",
            new String[] { "buildcraft.guide.contents.actions" },
            new String[] { "buildcraft.guide.chapter.subtype.pipe_item" },
            icon,
            "Set Power Limit",
            null);
    }

    private void genTypeMap(GuideBook book) {
        Map<TypeOrder, ContentsNode> map = new HashMap<>();
        contents.put(book, map);
        for (TypeOrder order : GuiGuide.SORTING_TYPES) {

            map.put(order, new ContentsNode("root", -1, order.tags.isEmpty()));
        }
    }

    private void addChild(Identifier bookType, JsonTypeTags tags, PageLink page) {
        if (pageLinksAdded.add(page)) {
            quickSearcher.add(page, page.getSearchName());
        }

        for (Entry<GuideBook, Map<TypeOrder, ContentsNode>> bookEntry : contents.entrySet()) {
            @Nullable GuideBook book = bookEntry.getKey();
            if (book != null && !book.name.equals(bookType)) continue;
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
                        node = (ContentsNode) subNode;
                        nodePath[i] = node;
                    } else if (subNode == null) {
                        ContentsNode subContents = new ContentsNode(title, i);
                        node.addChild(subContents);
                        node = subContents;
                        nodePath[i] = node;
                    } else {
                        throw new IllegalStateException("Unknown node type " + subNode.getClass());
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

    @Nullable
    public GuidePageFactory getFactoryFor(Identifier partialLocation) {
        return pages.get(partialLocation);
    }

    @Nullable
    public GuidePageFactory getFactoryFor(Object value) {
        if (value instanceof ItemStackValueFilter) {
            value = ((ItemStackValueFilter) value).stack.baseStack;
        } else if (value instanceof ItemStackKey) {
            value = ((ItemStackKey) value).baseStack;
        }
        if (value instanceof ItemStack) {
            return getPageFor((ItemStack) value);
        }
        return getFactoryFor(getEntryFor(value));
    }

    public static Identifier getEntryFor(Object obj) {
        for (Entry<Object, PageEntry<?>> entry : GuidePageRegistry.INSTANCE.getReloadableEntryMap()
            .entrySet()) {
            if (entry.getValue().matches(obj)) {
                return (Identifier) entry.getKey();
            }
        }
        return null;
    }

    @Nonnull
    public GuidePageFactory getPageFor(@Nonnull ItemStack stack) {
        Identifier entry = getEntryFor(stack);
        if (entry != null) {
            GuidePageFactory factory = getFactoryFor(entry);
            if (factory != null) {
                return factory;
            }
        }
        return generatedPages.computeIfAbsent(stack, GuidePageStandInRecipes::createFactory);
    }

    public ContentsNodeGui getGuiContents(GuiGuide gui, GuidePageContents guidePageContents, TypeOrder sortingOrder) {
        if (contents.isEmpty()) {

            reload();
        }
        Map<TypeOrder, ContentsNode> map = contents.get(gui.book);
        if (map == null) {

            map = contents.get(null);
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
}
