package buildcraft.lib.client.guide.node;

import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.parts.GuideChapter;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuideText;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Deprecated
public class NodePageLine implements Comparable<NodePageLine> {
   public final NodePageLine parent;
   public final GuidePart part;
   public boolean visible = true;
   private final List<NodePageLine> children = Lists.newArrayList();

   public NodePageLine(NodePageLine parent, GuidePart part) {
      this.parent = parent;
      this.part = part;
   }

   public NodePageLine addChild(GuidePart child) {
      NodePageLine node = new NodePageLine(this, child);
      this.children.add(node);
      return node;
   }

   public void setFontRenderer(IFontRenderer fontRenderer) {
      if (this.part != null) {
         this.part.setFontRenderer(fontRenderer);
      }

      for (NodePageLine node : this.children) {
         node.setFontRenderer(fontRenderer);
      }
   }

   public Iterable<NodePageLine> iterateNonNullNodes() {
      return () -> new NodePageLine.NodePartIterator();
   }

   public Iterable<GuidePart> iterateNonNullLines() {
      return () -> new NodePageLine.NodeGuidePartIterator();
   }

   public List<NodePageLine> getChildren() {
      return Collections.unmodifiableList(this.children);
   }

   public NodePageLine getChildNode(GuidePart line) {
      for (NodePageLine node : this.iterateNonNullNodes()) {
         if (node.part == line) {
            return node;
         }
      }

      return null;
   }

   public void sortChildrenRecursively() {
      Collections.sort(this.children);

      for (NodePageLine child : this.children) {
         child.sortChildrenRecursively();
      }
   }

   private String getString() {
      if (this.part instanceof GuideText) {
         return ((GuideText)this.part).text.text;
      } else if (this.part instanceof GuideChapter) {
         return ((GuideChapter)this.part).chapter.text;
      } else {
         return this.part == null ? "null" : this.part.toString();
      }
   }

   public int compareTo(NodePageLine o) {
      return this.getString().compareTo(o.getString());
   }

   private class NodeGuidePartIterator implements Iterator<GuidePart> {
      private final NodePageLine.NodePartIterator iterator = NodePageLine.this.new NodePartIterator();

      @Override
      public boolean hasNext() {
         return this.iterator.hasNext();
      }

      public GuidePart next() {
         return this.iterator.next().part;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException("remove");
      }
   }

   private class NodePartIterator implements Iterator<NodePageLine> {
      private NodePageLine current;
      private int childrenDone = 0;

      NodePartIterator() {
         this.current = NodePageLine.this;
      }

      @Override
      public boolean hasNext() {
         return this.next(true) != null;
      }

      public NodePageLine next() {
         return this.next(false);
      }

      private NodePageLine next(boolean simulate) {
         NodePageLine next = this.current;
         int visited = this.childrenDone;

         while (visited == next.getChildren().size()) {
            NodePageLine child = next;
            next = next.parent;
            if (next == null) {
               return null;
            }

            visited = next.getChildren().indexOf(child) + 1;
         }

         next = next.getChildren().get(visited++);
         int var7 = 0;
         if (!simulate) {
            this.current = next;
            this.childrenDone = var7;
         }

         return next;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException("remove");
      }
   }
}
