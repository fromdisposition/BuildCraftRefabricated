package buildcraft.lib.fabric.mixin;

import java.util.List;
import java.util.Set;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class BCMixinPlugin implements IMixinConfigPlugin {
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
      //? if < 26.1.3 {
      return List.of();
      //?}
      //? if >= 26.1.3 {
      /*return List.of("client.LevelExtractorMixin");*/
      //?}
   }

   @Override
   public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

   @Override
   public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
