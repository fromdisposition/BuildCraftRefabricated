package buildcraft.lib.client.model;

import java.util.Set;
import net.minecraft.resources.Identifier;

public abstract class ModelHolder {
   public final Identifier modelLocation;
   protected String failReason = "";

   public ModelHolder(Identifier modelLocation) {
      this.modelLocation = modelLocation;
      ModelHolderRegistry.HOLDERS.add(this);
   }

   public ModelHolder(String modelLocation) {
      this(Identifier.parse(modelLocation));
   }

   protected abstract void onModelBake();

   protected abstract void onTextureStitchPre(Set<Identifier> var1);

   public abstract boolean hasBakedQuads();
}
