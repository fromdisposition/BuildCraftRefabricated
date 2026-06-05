package buildcraft.api.core.render;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface ISprite {

    void bindTexture();

    double getInterpU(double u);

    double getInterpV(double v);
}
