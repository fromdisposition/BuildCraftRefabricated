package buildcraft.lib.gui;

public interface IInteractionElement extends IGuiElement {

    default void onMouseClicked(int button) {}

    default void onMouseDragged(int button, long ticksSinceClick) {}

    default void onMouseReleased(int button) {}

    default boolean onKeyPress(char typedChar, int keyCode) {
        return false;
    }
}
