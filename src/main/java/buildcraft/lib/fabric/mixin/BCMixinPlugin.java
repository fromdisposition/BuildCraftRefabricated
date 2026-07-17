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
      //? if < 1.21.10 {
      /*// 1.21.1 has no Fabric UnbakedModelDeserializer, so vanilla's bulk model pre-load chokes on BC's
      // "buildcraftlib:variable" engine models; this mixin skips them at parse time (see the mixin javadoc).
      return List.of("client.BlockModelVariableSkipMixin");
      *///?} else {
      return List.of();
      //?}
   }

   @Override
   public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

   @Override
   public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
