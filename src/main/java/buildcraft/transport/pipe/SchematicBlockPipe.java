package buildcraft.transport.pipe;

import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.builders.snapshot.SchematicBlockDefault;
import buildcraft.transport.block.BlockPipeHolder;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;

public class SchematicBlockPipe extends SchematicBlockDefault {
   public static boolean predicate(SchematicBlockContext context) {
      return context.block instanceof BlockPipeHolder && SchematicBlockDefault.predicate(context);
   }

   @Nonnull
   @Override
   public List<ItemStack> computeRequiredItems(boolean includeContainerContents) {
      ItemStack pipeStack = this.resolvePipeItem();
      if (pipeStack != null && !pipeStack.isEmpty()) {
         List<ItemStack> required = new ArrayList<>();
         required.add(pipeStack);
         this.addPluggableItems(required);
         return required;
      } else {
         return super.computeRequiredItems(includeContainerContents);
      }
   }

   private void addPluggableItems(@Nonnull List<ItemStack> out) {
      if (this.tileNbt != null && PipeApi.pluggableRegistry != null) {
         CompoundTag plugTag = this.tileNbt.getCompoundOrEmpty("plugs");
         if (!plugTag.isEmpty()) {
            for (Direction face : Direction.values()) {
               CompoundTag entry = plugTag.getCompoundOrEmpty(face.getName());
               String plugId = entry.getStringOr("id", "");
               if (!plugId.isEmpty()) {
                  PluggableDefinition def = PipeApi.pluggableRegistry.getDefinition(Identifier.parse(plugId));
                  if (def != null) {
                     try {
                        PipePluggable plug = def.readFromNbt(null, face, entry.getCompoundOrEmpty("data"));
                        if (plug != null) {
                           ItemStack stack = plug.getPickStack();
                           if (stack != null && !stack.isEmpty()) {
                              out.add(stack);
                           }
                        }
                     } catch (Throwable var12) {
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public SchematicBlockDefault getRotated(Rotation rotation) {
      SchematicBlockDefault rotated = super.getRotated(rotation);
      if (rotated instanceof SchematicBlockPipe pipe && this.tileNbt != null) {
         pipe.tileNbt = rotatePluggableFaces(this.tileNbt, rotation);
      }

      return rotated;
   }

   @Nonnull
   private static CompoundTag rotatePluggableFaces(@Nonnull CompoundTag original, Rotation rotation) {
      if (rotation != Rotation.NONE && original.contains("plugs")) {
         CompoundTag copy = original.copy();
         CompoundTag oldPlugs = copy.getCompoundOrEmpty("plugs");
         CompoundTag newPlugs = new CompoundTag();

         for (Direction face : Direction.values()) {
            CompoundTag entry = oldPlugs.getCompoundOrEmpty(face.getName());
            if (!entry.isEmpty()) {
               newPlugs.put(rotation.rotate(face).getName(), entry);
            }
         }

         copy.put("plugs", newPlugs);
         return copy;
      } else {
         return original;
      }
   }

   @Nullable
   private ItemStack resolvePipeItem() {
      if (this.tileNbt == null) {
         return null;
      } else {
         CompoundTag pipeTag = this.tileNbt.getCompoundOrEmpty("pipe");
         String defId = pipeTag.getStringOr("def", "");
         if (defId.isEmpty()) {
            return null;
         } else {
            PipeDefinition def = PipeRegistry.INSTANCE.getDefinition(defId);
            if (def == null) {
               return null;
            } else {
               return PipeRegistry.INSTANCE.getItemForPipe(def) instanceof Item item ? new ItemStack(item) : null;
            }
         }
      }
   }
}
