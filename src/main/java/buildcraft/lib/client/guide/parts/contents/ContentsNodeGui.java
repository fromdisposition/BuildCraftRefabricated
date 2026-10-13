/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.contents;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.parts.GuideChapter;
import buildcraft.lib.client.guide.parts.GuidePageBase;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.client.guide.parts.GuidePart;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;

public class ContentsNodeGui {
   public final GuiGuide gui;
   public final ContentsNode node;
   private IFontRenderer fontRenderer;
   private List<GuideChapter> chapters;
   private GuidePart[] parts;
   private PageLink[] links;

   public ContentsNodeGui(GuiGuide gui, ContentsNode node) {
      this.gui = gui;
      this.node = node;
   }

   public List<GuideChapter> getChapters() {
      if (this.populate() || this.chapters == null) {
         this.chapters = new ArrayList<>();

         for (GuidePart part : this.parts) {
            if (part instanceof GuideChapter) {
               this.chapters.add((GuideChapter)part);
            }
         }
      }

      return this.chapters;
   }

   public void setFontRenderer(IFontRenderer fontRenderer) {
      this.fontRenderer = fontRenderer;
      if (this.parts != null) {
         for (GuidePart part : this.parts) {
            part.setFontRenderer(fontRenderer);
         }
      }
   }

   public void invalidate() {
      this.parts = null;
      this.links = null;
      this.chapters = null;
   }

   private boolean populate() {
      if (this.parts != null) {
         return false;
      }

      List<GuidePart> allText = new ArrayList<>();
      List<PageLink> allLinks = new ArrayList<>();
      Deque<IContentsNode> queue = new ArrayDeque<>();
      IContentsNode[] children = this.node.getVisibleChildren();

      for (int i = children.length - 1; i >= 0; i--) {
         queue.addLast(children[i]);
      }

      while (!queue.isEmpty()) {
         IContentsNode next = queue.removeLast();
         GuidePart part = next.createGuidePart(this.gui);
         if (this.fontRenderer != null) {
            part.setFontRenderer(this.fontRenderer);
         }

         allText.add(part);
         if (next instanceof PageLink) {
            allLinks.add((PageLink)next);
         } else {
            allLinks.add(null);
         }

         IContentsNode[] nextChildren = next.getVisibleChildren();

         for (int i = nextChildren.length - 1; i >= 0; i--) {
            queue.addLast(nextChildren[i]);
         }
      }

      this.parts = allText.toArray(new GuidePart[0]);
      this.links = allLinks.toArray(new PageLink[0]);
      return true;
   }

   public GuidePart.PagePosition render(int x, int y, int width, int height, GuidePart.PagePosition current, int index) {
      return this.iterate(current, height, (pos, part, link) -> part.renderIntoArea(x, y, width, height, pos, index));
   }

   public void onClicked(int x, int y, int width, int height, GuidePart.PagePosition current, int index) {
      this.iterate(current, height, (pos, part, link) -> {
         pos = part.renderIntoArea(x, y, width, height, pos, -1);
         if (pos.page == index && part.wasHovered() && link != null) {
            GuidePageFactory factory = link.getFactoryLink();
            if (factory != null) {
               GuidePageBase page = factory.createNew(this.gui);
               if (page != null) {
                  this.gui.openPage(page);
                  return null;
               }
            }
         }

         return pos;
      });
   }

   @Nullable
   private GuidePart.PagePosition iterate(GuidePart.PagePosition pos, int height, ContentsNodeGui.IGuideBitIter iter) {
      this.populate();

      for (int i = 0; i < this.links.length; i++) {
         GuidePart part = this.parts[i];
         PageLink link = this.links[i];
         int space = 16;
         if (link == null) {
            for (int j = i; j < this.links.length; j++) {
               if (this.links[j] != null) {
                  pos = pos.guaranteeSpace(space * (1 + j - i), height);
                  break;
               }
            }
         }

         pos = iter.iterate(pos, part, link);
         if (pos == null) {
            return null;
         }
      }

      return pos;
   }

   @FunctionalInterface
   private interface IGuideBitIter {
      @Nullable
      GuidePart.PagePosition iterate(GuidePart.PagePosition var1, GuidePart var2, PageLink var3);
   }
}
