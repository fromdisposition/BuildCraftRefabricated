package buildcraft.lib.client.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

public final class BcTextureAtlases {
   public static final Identifier BLOCKS_TEXTURE = Identifier.withDefaultNamespace("textures/atlas/blocks.png");
   public static final Identifier ITEMS_TEXTURE = Identifier.withDefaultNamespace("textures/atlas/items.png");
   public static final Identifier BLOCKS = AtlasIds.BLOCKS;
   public static final Identifier ITEMS = AtlasIds.ITEMS;

   private BcTextureAtlases() {
   }

   public static SpriteGetter blocksSpriteGetter(Minecraft minecraft) {
      return minecraft.getAtlasManager();
   }

   public static TextureAtlasSprite getBlockSprite(Identifier texture) {
      return getBlockSprite(Minecraft.getInstance(), texture);
   }

   public static TextureAtlasSprite getBlockSprite(Minecraft minecraft, Identifier texture) {
      return blocksSpriteGetter(minecraft).get(Sheets.BLOCKS_MAPPER.apply(texture));
   }

   public static SpriteId blockSpriteId(Identifier texture) {
      return Sheets.BLOCKS_MAPPER.apply(texture);
   }
}
