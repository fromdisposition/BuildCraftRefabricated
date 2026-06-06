package buildcraft.lib.gui;

import buildcraft.lib.gui.help.ElementHelpInfo;
import java.util.List;

@FunctionalInterface
public interface IHelpElement {
   void addHelpElements(List<ElementHelpInfo.HelpPosition> var1);
}
