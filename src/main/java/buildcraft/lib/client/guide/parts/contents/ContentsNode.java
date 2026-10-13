/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.contents;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.parts.GuideChapterWithin;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuideText;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;

public class ContentsNode implements IContentsNode {
   public final String title;
   public final int indent;
   private final boolean ignoreSortWeight;
   private final Map<String, IContentsNode> nodes = new HashMap<>();
   private IContentsNode[] sortedNodes = new IContentsNode[0];
   IContentsNode[] visibleNodes = new IContentsNode[0];
   private boolean needsSorting = false;

   public ContentsNode(String title, int indent) {
      this(title, indent, false);
   }

   public ContentsNode(String title, int indent, boolean ignoreSortWeight) {
      this.title = title;
      this.indent = indent;
      this.ignoreSortWeight = ignoreSortWeight;
   }

   @Override
   public String getSearchName() {
      return this.title;
   }

   @Override
   public int getSortIndex() {
      int min = Integer.MAX_VALUE;

      for (IContentsNode child : this.nodes.values()) {
         min = Math.min(min, child.getSortIndex());
      }

      return min == Integer.MAX_VALUE ? 0 : min;
   }

   @Override
   public GuidePart createGuidePart(GuiGuide gui) {
      return this.indent == 0
         ? new GuideChapterWithin(gui, ChatFormatting.UNDERLINE + this.title)
         : new GuideText(gui, new PageLine(this.indent + 1, ChatFormatting.UNDERLINE + this.title, false));
   }

   @Nullable
   public IContentsNode getChild(String childKey) {
      return this.nodes.get(childKey);
   }

   @Override
   public void addChild(IContentsNode node) {
      this.nodes.put(node.getSearchName(), node);
      this.needsSorting = true;
   }

   @Override
   public IContentsNode[] getVisibleChildren() {
      return this.visibleNodes;
   }

   @Override
   public boolean isVisible() {
      return this.visibleNodes.length != 0;
   }

   @Override
   public void sort() {
      if (this.needsSorting) {
         this.needsSorting = false;
         this.sortedNodes = this.nodes.values().toArray(new IContentsNode[0]);
         Comparator<IContentsNode> byName = Comparator.comparing(IContentsNode::getSearchName, String.CASE_INSENSITIVE_ORDER);
         Arrays.sort(this.sortedNodes, this.ignoreSortWeight ? byName : Comparator.comparingInt(IContentsNode::getSortIndex).thenComparing(byName));

         for (IContentsNode node : this.sortedNodes) {
            node.sort();
         }

         this.calcVisibility();
      }
   }

   @Override
   public void calcVisibility() {
      List<IContentsNode> visible = new ArrayList<>();

      for (IContentsNode node : this.sortedNodes) {
         if (node.isVisible()) {
            visible.add(node);
         }
      }

      this.visibleNodes = visible.toArray(new IContentsNode[0]);
   }

   @Override
   public void resetVisibility() {
      for (IContentsNode node : this.sortedNodes) {
         node.resetVisibility();
      }

      this.calcVisibility();
   }

   @Override
   public void setVisible(Set<PageLink> matches) {
      for (IContentsNode node : this.sortedNodes) {
         node.setVisible(matches);
      }

      this.calcVisibility();
   }
}
