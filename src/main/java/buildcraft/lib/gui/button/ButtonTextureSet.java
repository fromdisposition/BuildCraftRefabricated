package buildcraft.lib.gui.button;

import net.minecraft.resources.Identifier;

public class ButtonTextureSet implements IButtonTextureSet {
   private final Identifier texture;
   private final int x;
   private final int y;
   private final int height;
   private final int width;

   public ButtonTextureSet(int x, int y, int height, int width) {
      this(x, y, height, width, StandardButtonTextureSets.BUTTON_TEXTURES);
   }

   public ButtonTextureSet(int x, int y, int height, int width, Identifier texture) {
      this.x = x;
      this.y = y;
      this.height = height;
      this.width = width;
      this.texture = texture;
   }

   @Override
   public int getX() {
      return this.x;
   }

   @Override
   public int getY() {
      return this.y;
   }

   @Override
   public int getHeight() {
      return this.height;
   }

   @Override
   public int getWidth() {
      return this.width;
   }

   @Override
   public Identifier getTexture() {
      return this.texture;
   }
}
