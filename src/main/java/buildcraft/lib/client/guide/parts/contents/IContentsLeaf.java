package buildcraft.lib.client.guide.parts.contents;

public interface IContentsLeaf extends IContentsNode {
    @Override
    default int getSortIndex() {
        return 0;
    }

    @Override
    default void calcVisibility() {}

    @Override
    default void sort() {}

    @Override
    default void addChild(IContentsNode node) {}

    @Override
    default IContentsNode[] getVisibleChildren() {
        return new IContentsNode[0];
    }
}
