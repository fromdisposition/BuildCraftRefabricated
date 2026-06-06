package buildcraft.lib.gui.button;

import net.minecraft.resources.Identifier;

public enum StandardButtonTextureSets implements IButtonTextureSet {
   LARGE_BUTTON(0, 0, 20, 200),
   SMALL_BUTTON(0, 100, 15, 200),
   LEFT_BUTTON(204, 0, 16, 10),
   RIGHT_BUTTON(214, 0, 16, 10);

   public static final Identifier BUTTON_TEXTURES = Identifier.parse("buildcraftlib:textures/gui/buttons.png");
   private final int x;
   private final int y;
   private final int height;
   private final int width;

   StandardButtonTextureSets(int x, int y, int height, int width) {
      this.x = x;
      this.y = y;
      this.height = height;
      this.width = width;
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
      return BUTTON_TEXTURES;
   }
}
