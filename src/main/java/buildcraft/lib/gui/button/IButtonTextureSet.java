package buildcraft.lib.gui.button;

import net.minecraft.resources.Identifier;

@Deprecated
public interface IButtonTextureSet {
   int getX();

   int getY();

   int getHeight();

   int getWidth();

   Identifier getTexture();
}
