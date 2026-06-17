package buildcraft.lib.fabric.mixin;

import java.util.List;
import java.util.Set;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class BCMixinPlugin implements IMixinConfigPlugin {
   private static final boolean LEVEL_EXTRACTOR_EXISTS;

   static {
      boolean exists;
      try {
         Class.forName("net.minecraft.client.renderer.extract.LevelExtractor");
         exists = true;
      } catch (ClassNotFoundException e) {
         exists = false;
      }
      LEVEL_EXTRACTOR_EXISTS = exists;
   }

   @Override
   public void onLoad(String mixinPackage) {}

   @Override
   public String getRefMapperConfig() {
      return null;
   }

   @Override
   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      return true;
   }

   @Override
   public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

   @Override
   public List<String> getMixins() {
      return LEVEL_EXTRACTOR_EXISTS ? List.of("client.LevelExtractorMixin") : List.of();
   }

   @Override
   public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

   @Override
   public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
