package buildcraft.lib.client.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

public final class BcTextureAtlases {
   public static final Identifier BLOCKS_TEXTURE = Identifier.withDefaultNamespace("textures/atlas/blocks.png");
   public static final Identifier ITEMS_TEXTURE = Identifier.withDefaultNamespace("textures/atlas/items.png");
   public static final Identifier BLOCKS = AtlasIds.BLOCKS;
   public static final Identifier ITEMS = AtlasIds.ITEMS;

   private BcTextureAtlases() {
   }

   public static TextureAtlas blocks(Minecraft minecraft) {
      return (TextureAtlas)minecraft.getTextureManager().getTexture(BLOCKS_TEXTURE);
   }
}
