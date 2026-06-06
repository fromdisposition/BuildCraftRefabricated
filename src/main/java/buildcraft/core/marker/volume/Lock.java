package buildcraft.core.marker.volume;

import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class Lock {
   public Lock.Cause cause;
   public List<Lock.Target> targets = new ArrayList<>();

   public Lock() {
   }

   public Lock(Lock.Cause cause, Lock.Target... targets) {
      this.cause = cause;
      this.targets.addAll(Arrays.asList(targets));
   }

   public CompoundTag writeToNBT() {
      CompoundTag nbt = new CompoundTag();
      CompoundTag causeTag = new CompoundTag();
      causeTag.putString("type", Lock.Cause.EnumCause.getForClass((Class<? extends Lock.Cause>)this.cause.getClass()).name());
      causeTag.put("data", this.cause.writeToNBT(new CompoundTag()));
      nbt.put("cause", causeTag);
      ListTag targetsList = new ListTag();

      for (Lock.Target target : this.targets) {
         CompoundTag targetTag = new CompoundTag();
         targetTag.putString("type", Lock.Target.EnumTarget.getForClass((Class<? extends Lock.Target>)target.getClass()).name());
         targetTag.put("data", target.writeToNBT(new CompoundTag()));
         targetsList.add(targetTag);
      }

      nbt.put("targets", targetsList);
      return nbt;
   }

   public void readFromNBT(CompoundTag nbt) {
      CompoundTag causeTag = (CompoundTag)nbt.getCompound("cause").orElseGet(CompoundTag::new);
      String causeType = causeTag.getString("type").orElse("BLOCK");

      try {
         this.cause = Lock.Cause.EnumCause.valueOf(causeType).supplier.get();
         this.cause.readFromNBT((CompoundTag)causeTag.getCompound("data").orElseGet(CompoundTag::new));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("Unknown lock cause type: " + causeType, e);
      }

      if (nbt.contains("targets")) {
         ListTag targetsList = (ListTag)nbt.getList("targets").orElseGet(ListTag::new);

         for (int i = 0; i < targetsList.size(); i++) {
            CompoundTag targetTag = (CompoundTag)targetsList.getCompound(i).orElseGet(CompoundTag::new);
            String targetType = targetTag.getString("type").orElse("REMOVE");

            try {
               Lock.Target target = Lock.Target.EnumTarget.valueOf(targetType).supplier.get();
               target.readFromNBT((CompoundTag)targetTag.getCompound("data").orElseGet(CompoundTag::new));
               this.targets.add(target);
            } catch (IllegalArgumentException e) {
               throw new IllegalArgumentException("Unknown lock target type: " + targetType, e);
            }
         }
      }
   }

   public abstract static class Cause {
      public abstract CompoundTag writeToNBT(CompoundTag var1);

      public abstract void readFromNBT(CompoundTag var1);

      public abstract boolean stillWorks(Level var1);

      public static class CauseBlock extends Lock.Cause {
         public BlockPos pos;
         public Block block;

         public CauseBlock() {
         }

         public CauseBlock(BlockPos pos, Block block) {
            this.pos = pos;
            this.block = block;
         }

         @Override
         public CompoundTag writeToNBT(CompoundTag nbt) {
            if (this.pos != null) {
               CompoundTag posTag = new CompoundTag();
               posTag.putInt("X", this.pos.getX());
               posTag.putInt("Y", this.pos.getY());
               posTag.putInt("Z", this.pos.getZ());
               nbt.put("pos", posTag);
            }

            nbt.putString("block", BuiltInRegistries.BLOCK.getKey(this.block).toString());
            return nbt;
         }

         @Override
         public void readFromNBT(CompoundTag nbt) {
            if (nbt.contains("pos")) {
               CompoundTag posTag = (CompoundTag)nbt.getCompound("pos").orElseGet(CompoundTag::new);
               this.pos = new BlockPos(posTag.getInt("X").orElse(0), posTag.getInt("Y").orElse(0), posTag.getInt("Z").orElse(0));
            }

            String blockKey = nbt.getString("block").orElse("minecraft:air");
            this.block = (Block)BuiltInRegistries.BLOCK.getValue(Identifier.parse(blockKey));
         }

         @Override
         public boolean stillWorks(Level world) {
            return world.getBlockState(this.pos).getBlock() == this.block;
         }
      }

      enum EnumCause {
         BLOCK(Lock.Cause.CauseBlock::new);

         public final Supplier<? extends Lock.Cause> supplier;

         EnumCause(Supplier<? extends Lock.Cause> supplier) {
            this.supplier = supplier;
         }

         public static Lock.Cause.EnumCause getForClass(Class<? extends Lock.Cause> clazz) {
            return Arrays.stream(values()).filter(enumCause -> enumCause.supplier.get().getClass() == clazz).findFirst().orElse(null);
         }
      }
   }

   public abstract static class Target {
      public abstract CompoundTag writeToNBT(CompoundTag var1);

      public abstract void readFromNBT(CompoundTag var1);

      enum EnumTarget {
         REMOVE(Lock.Target.TargetRemove::new),
         RESIZE(Lock.Target.TargetResize::new),
         ADDON(Lock.Target.TargetAddon::new),
         USED_BY_MACHINE(Lock.Target.TargetUsedByMachine::new);

         public final Supplier<? extends Lock.Target> supplier;

         EnumTarget(Supplier<? extends Lock.Target> supplier) {
            this.supplier = supplier;
         }

         public static Lock.Target.EnumTarget getForClass(Class<? extends Lock.Target> clazz) {
            return Arrays.stream(values()).filter(enumTarget -> enumTarget.supplier.get().getClass() == clazz).findFirst().orElse(null);
         }
      }

      public static class TargetAddon extends Lock.Target {
         public EnumAddonSlot slot;

         public TargetAddon() {
         }

         public TargetAddon(EnumAddonSlot slot) {
            this.slot = slot;
         }

         @Override
         public CompoundTag writeToNBT(CompoundTag nbt) {
            nbt.putString("slot", this.slot.name());
            return nbt;
         }

         @Override
         public void readFromNBT(CompoundTag nbt) {
            this.slot = EnumAddonSlot.valueOf(nbt.getString("slot").orElse(""));
         }
      }

      public static class TargetRemove extends Lock.Target {
         @Override
         public CompoundTag writeToNBT(CompoundTag nbt) {
            return nbt;
         }

         @Override
         public void readFromNBT(CompoundTag nbt) {
         }
      }

      public static class TargetResize extends Lock.Target {
         @Override
         public CompoundTag writeToNBT(CompoundTag nbt) {
            return nbt;
         }

         @Override
         public void readFromNBT(CompoundTag nbt) {
         }
      }

      public static class TargetUsedByMachine extends Lock.Target {
         public Lock.Target.TargetUsedByMachine.EnumType type;

         public TargetUsedByMachine() {
         }

         public TargetUsedByMachine(Lock.Target.TargetUsedByMachine.EnumType type) {
            this.type = type;
         }

         @Override
         public CompoundTag writeToNBT(CompoundTag nbt) {
            nbt.putString("type", this.type.name());
            return nbt;
         }

         @Override
         public void readFromNBT(CompoundTag nbt) {
            this.type = Lock.Target.TargetUsedByMachine.EnumType.valueOf(nbt.getString("type").orElse("STRIPES_WRITE"));
         }

         public enum EnumType {
            STRIPES_WRITE {
               @Override
               public LaserData_BC8.LaserType getLaserType() {
                  return BuildCraftLaserManager.STRIPES_WRITE;
               }
            },
            STRIPES_READ {
               @Override
               public LaserData_BC8.LaserType getLaserType() {
                  return BuildCraftLaserManager.STRIPES_READ;
               }
            };

            public abstract LaserData_BC8.LaserType getLaserType();
         }
      }
   }
}
