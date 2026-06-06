package buildcraft.api.transport.pipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;

public final class PipeDefinition {
   public final String identifier;
   public final PipeDefinition.IPipeCreator logicConstructor;
   public final PipeDefinition.IPipeLoader logicLoader;
   public final PipeFlowType flowType;
   public final String[] textures;
   @Deprecated
   public final int itemTextureTop;
   @Deprecated
   public final int itemTextureCenter;
   @Deprecated
   public final int itemTextureBottom;
   public final PipeFaceTex itemModelTop;
   public final PipeFaceTex itemModelCenter;
   public final PipeFaceTex itemModelBottom;
   public final boolean canBeColoured;
   private EnumPipeColourType colourType;

   public PipeDefinition(PipeDefinition.PipeDefinitionBuilder builder) {
      this.identifier = builder.identifier;
      this.textures = new String[builder.textureSuffixes.length];

      for (int i = 0; i < this.textures.length; i++) {
         this.textures[i] = builder.texturePrefix + builder.textureSuffixes[i];
      }

      this.logicConstructor = builder.logicConstructor;
      this.logicLoader = builder.logicLoader;
      this.flowType = builder.flowType;
      this.itemTextureTop = builder.itemTextureTop;
      this.itemTextureCenter = builder.itemTextureCenter;
      this.itemTextureBottom = builder.itemTextureBottom;
      this.itemModelBottom = builder.itemModelBottom;
      this.itemModelCenter = builder.itemModelCenter;
      this.itemModelTop = builder.itemModelTop;
      this.canBeColoured = builder.canBeColoured;
      this.colourType = builder.colourType;
   }

   @Nonnull
   public EnumPipeColourType getColourType() {
      if (this.colourType != null) {
         return this.colourType;
      } else {
         return this.flowType.fallbackColourType != null ? this.flowType.fallbackColourType : EnumPipeColourType.TRANSLUCENT;
      }
   }

   public void setColourType(@Nullable EnumPipeColourType colourType) {
      this.colourType = colourType;
   }

   @FunctionalInterface
   public interface IPipeCreator {
      PipeBehaviour createBehaviour(IPipe var1);
   }

   @FunctionalInterface
   public interface IPipeLoader {
      PipeBehaviour loadBehaviour(IPipe var1, CompoundTag var2);
   }

   public static class PipeDefinitionBuilder {
      public String identifier;
      public String texturePrefix;
      public String[] textureSuffixes = new String[]{""};
      public PipeDefinition.IPipeCreator logicConstructor;
      public PipeDefinition.IPipeLoader logicLoader;
      public PipeFlowType flowType;
      @Deprecated
      public int itemTextureTop = 0;
      @Deprecated
      public int itemTextureCenter = 0;
      @Deprecated
      public int itemTextureBottom = 0;
      public PipeFaceTex itemModelTop = PipeFaceTex.get(0);
      public PipeFaceTex itemModelCenter = PipeFaceTex.get(0);
      public PipeFaceTex itemModelBottom = PipeFaceTex.get(0);
      public boolean canBeColoured;
      public EnumPipeColourType colourType;

      public PipeDefinitionBuilder() {
      }

      public PipeDefinitionBuilder(
         String identifier, PipeDefinition.IPipeCreator logicConstructor, PipeDefinition.IPipeLoader logicLoader, PipeFlowType flowType
      ) {
         this.identifier = identifier;
         this.logicConstructor = logicConstructor;
         this.logicLoader = logicLoader;
         this.flowType = flowType;
      }

      public PipeDefinition.PipeDefinitionBuilder idTexPrefix(String modid, String both) {
         return this.id(modid, both).texPrefix(modid, both);
      }

      public PipeDefinition.PipeDefinitionBuilder idTex(String modid, String both) {
         return this.id(modid, both).tex(modid, both);
      }

      public PipeDefinition.PipeDefinitionBuilder id(String modid, String path) {
         this.identifier = modid + ":" + path;
         return this;
      }

      public PipeDefinition.PipeDefinitionBuilder tex(String both, String... suffixes) {
         return this.texPrefix(both).texSuffixes(suffixes);
      }

      public PipeDefinition.PipeDefinitionBuilder texPrefix(String prefix) {
         return this.texPrefixDirect(prefix);
      }

      public PipeDefinition.PipeDefinitionBuilder texPrefix(String modid, String prefix) {
         return this.texPrefixDirect(modid + ":pipes/" + prefix);
      }

      public PipeDefinition.PipeDefinitionBuilder texPrefixDirect(String prefix) {
         this.texturePrefix = prefix;
         return this;
      }

      public PipeDefinition.PipeDefinitionBuilder texSuffixes(String... suffixes) {
         if (suffixes != null && suffixes.length != 0) {
            this.textureSuffixes = suffixes;
         } else {
            this.textureSuffixes = new String[]{""};
         }

         return this;
      }

      public PipeDefinition.PipeDefinitionBuilder itemTex(int all) {
         this.itemModelBottom = PipeFaceTex.get(all);
         this.itemModelCenter = this.itemModelBottom;
         this.itemModelTop = this.itemModelBottom;
         this.itemTextureTop = all;
         this.itemTextureCenter = all;
         this.itemTextureBottom = all;
         return this;
      }

      public PipeDefinition.PipeDefinitionBuilder itemTex(int top, int center, int bottom) {
         this.itemModelBottom = PipeFaceTex.get(bottom);
         this.itemModelCenter = PipeFaceTex.get(center);
         this.itemModelTop = PipeFaceTex.get(top);
         this.itemTextureTop = top;
         this.itemTextureCenter = center;
         this.itemTextureBottom = bottom;
         return this;
      }

      public PipeDefinition.PipeDefinitionBuilder logic(PipeDefinition.IPipeCreator creator, PipeDefinition.IPipeLoader loader) {
         this.logicConstructor = creator;
         this.logicLoader = loader;
         return this;
      }

      public PipeDefinition.PipeDefinitionBuilder disableColouring() {
         this.canBeColoured = false;
         return this;
      }

      public PipeDefinition.PipeDefinitionBuilder enableColouring(EnumPipeColourType type) {
         this.canBeColoured = true;
         this.colourType = type;
         return this;
      }

      public PipeDefinition.PipeDefinitionBuilder enableColouring() {
         return this.enableColouring(null);
      }

      public PipeDefinition.PipeDefinitionBuilder enableTranslucentColouring() {
         return this.enableColouring(EnumPipeColourType.TRANSLUCENT);
      }

      public PipeDefinition.PipeDefinitionBuilder enableBorderColouring() {
         return this.enableColouring(EnumPipeColourType.BORDER_OUTER);
      }

      public PipeDefinition.PipeDefinitionBuilder enableInnerBorderColouring() {
         return this.enableColouring(EnumPipeColourType.BORDER_INNER);
      }

      public PipeDefinition.PipeDefinitionBuilder enableCustomColouring() {
         return this.enableColouring(EnumPipeColourType.CUSTOM);
      }

      public PipeDefinition.PipeDefinitionBuilder flowItem() {
         return this.flow(PipeApi.flowItems);
      }

      public PipeDefinition.PipeDefinitionBuilder flowFluid() {
         return this.flow(PipeApi.flowFluids);
      }

      public PipeDefinition.PipeDefinitionBuilder flowPower() {
         return this.flow(PipeApi.flowPower);
      }

      public PipeDefinition.PipeDefinitionBuilder flow(PipeFlowType flow) {
         this.flowType = flow;
         return this;
      }

      public PipeDefinition define() {
         PipeDefinition def = new PipeDefinition(this);
         PipeApi.pipeRegistry.registerPipe(def);
         return def;
      }
   }
}
