package buildcraft.builders.snapshot;

import buildcraft.lib.fluids.FluidStack;
import com.google.gson.JsonDeserializer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public abstract class RequiredExtractor {
   public static final JsonDeserializer<RequiredExtractor> DESERIALIZER = (json, typeOfT, context) -> {
      RequiredExtractor.EnumType type = RequiredExtractor.EnumType.byName(json.getAsJsonObject().get("type").getAsString());
      json.getAsJsonObject().remove("type");
      return (RequiredExtractor)context.deserialize(json, type.clazz);
   };

   @Nonnull
   public List<ItemStack> extractItemsFromBlock(@Nonnull BlockState blockState, @Nullable CompoundTag tileNbt) {
      return Collections.emptyList();
   }

   @Nonnull
   public List<FluidStack> extractFluidsFromBlock(@Nonnull BlockState blockState, @Nullable CompoundTag tileNbt) {
      return Collections.emptyList();
   }

   @Nonnull
   public List<ItemStack> extractItemsFromEntity(@Nonnull CompoundTag entityNbt) {
      return Collections.emptyList();
   }

   @Nonnull
   public List<FluidStack> extractFluidsFromEntity(@Nonnull CompoundTag entityNbt) {
      return Collections.emptyList();
   }

   public void clearItemsFromBlock(@Nonnull CompoundTag tileNbt) {
   }

   public enum EnumType {
      CONSTANT(RequiredExtractorConstant.class),
      ITEM_FROM_BLOCK(RequiredExtractorItemFromBlock.class),
      ITEM(RequiredExtractorItem.class),
      ITEMS_LIST(RequiredExtractorItemsList.class),
      TANK(RequiredExtractorTank.class);

      public final Class<? extends RequiredExtractor> clazz;

      EnumType(Class<? extends RequiredExtractor> clazz) {
         this.clazz = clazz;
      }

      public String getName() {
         return this.name().toLowerCase(Locale.ROOT);
      }

      public static RequiredExtractor.EnumType byName(String name) {
         return Arrays.stream(values())
            .filter(type -> type.getName().equals(name))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Required extractor type not found"));
      }
   }
}
