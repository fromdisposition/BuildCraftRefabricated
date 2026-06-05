package buildcraft.lib.gui;

public interface IMenuElement extends IInteractionElement {

    default boolean shouldFullyOverride() {
        return true;
    }
}
