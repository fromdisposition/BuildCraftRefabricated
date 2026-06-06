package buildcraft.api.robots;

import buildcraft.api.core.BCLog;
import buildcraft.api.mj.MjAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class AIRobot {
   public EntityRobotBase robot;
   private AIRobot delegateAI;
   private AIRobot parentAI;
   private boolean success;

   public AIRobot(EntityRobotBase iRobot) {
      this.robot = iRobot;
      this.success = true;
   }

   public void start() {
   }

   public void preempt(AIRobot ai) {
   }

   public void update() {
      this.terminate();
   }

   public void end() {
   }

   public void delegateAIEnded(AIRobot ai) {
   }

   public void delegateAIAborted(AIRobot ai) {
   }

   public void writeSelfToNBT(CompoundTag nbt) {
   }

   public void loadSelfFromNBT(CompoundTag nbt) {
   }

   public boolean success() {
      return this.success;
   }

   protected void setSuccess(boolean iSuccess) {
      this.success = iSuccess;
   }

   public long getPowerCost() {
      return MjAPI.MJ / 10L;
   }

   public boolean canLoadFromNBT() {
      return false;
   }

   public ItemStack receiveItem(ItemStack stack) {
      return stack;
   }

   public final void terminate() {
      this.abortDelegateAI();
      this.end();
      if (this.parentAI != null) {
         this.parentAI.delegateAI = null;
         this.parentAI.delegateAIEnded(this);
      }
   }

   public final void abort() {
      this.abortDelegateAI();

      try {
         this.end();
         if (this.parentAI != null) {
            this.parentAI.delegateAI = null;
            this.parentAI.delegateAIAborted(this);
         }
      } catch (Throwable e) {
         BCLog.logger.warn("[robots] Robot delegate AI threw; aborting it", e);
         this.delegateAI = null;
         if (this.parentAI != null) {
            this.parentAI.delegateAI = null;
         }
      }
   }

   public final void cycle() {
      try {
         this.preempt(this.delegateAI);
         if (this.delegateAI != null) {
            this.delegateAI.cycle();
         } else {
            this.robot.getBattery().extractPower(1L, this.getPowerCost());
            this.update();
         }
      } catch (Throwable e) {
         BCLog.logger.warn("[robots] Robot AI update threw; aborting", e);
         this.abort();
      }
   }

   public final void startDelegateAI(AIRobot ai) {
      this.abortDelegateAI();
      this.delegateAI = ai;
      ai.parentAI = this;
      this.delegateAI.start();
   }

   public final void abortDelegateAI() {
      if (this.delegateAI != null) {
         this.delegateAI.abort();
      }
   }

   public final AIRobot getActiveAI() {
      return this.delegateAI != null ? this.delegateAI.getActiveAI() : this;
   }

   public final AIRobot getDelegateAI() {
      return this.delegateAI;
   }

   public final void writeToNBT(CompoundTag nbt) {
      nbt.putString("aiName", RobotManager.getAIRobotName((Class<? extends AIRobot>)this.getClass()));
      CompoundTag data = new CompoundTag();
      this.writeSelfToNBT(data);
      nbt.put("data", data);
      if (this.delegateAI != null && this.delegateAI.canLoadFromNBT()) {
         CompoundTag sub = new CompoundTag();
         this.delegateAI.writeToNBT(sub);
         nbt.put("delegateAI", sub);
      }
   }

   public final void loadFromNBT(CompoundTag nbt) {
      this.loadSelfFromNBT(nbt.getCompound("data").orElse(new CompoundTag()));
      if (nbt.contains("delegateAI")) {
         CompoundTag sub = nbt.getCompound("delegateAI").orElse(new CompoundTag());

         try {
            Class<?> aiRobotClass;
            if (sub.contains("class")) {
               aiRobotClass = RobotManager.getAIRobotByLegacyClassName(sub.getString("class").orElse(""));
            } else {
               aiRobotClass = RobotManager.getAIRobotByName(sub.getString("aiName").orElse(""));
            }

            if (aiRobotClass != null) {
               this.delegateAI = (AIRobot)aiRobotClass.getConstructor(EntityRobotBase.class).newInstance(this.robot);
               this.delegateAI.parentAI = this;
               if (this.delegateAI.canLoadFromNBT()) {
                  this.delegateAI.loadFromNBT(sub);
               }
            }
         } catch (Throwable e) {
            BCLog.logger.warn("[robots] Failed to load robot delegate AI from NBT", e);
         }
      }
   }

   public static AIRobot loadAI(CompoundTag nbt, EntityRobotBase robot) {
      AIRobot ai = null;

      try {
         Class<?> aiRobotClass;
         if (nbt.contains("class")) {
            aiRobotClass = RobotManager.getAIRobotByLegacyClassName(nbt.getString("class").orElse(""));
         } else {
            aiRobotClass = RobotManager.getAIRobotByName(nbt.getString("aiName").orElse(""));
         }

         if (aiRobotClass != null) {
            ai = (AIRobot)aiRobotClass.getConstructor(EntityRobotBase.class).newInstance(robot);
            ai.loadFromNBT(nbt);
         }
      } catch (Throwable e) {
         BCLog.logger.warn("[robots] Failed to instantiate robot AI from NBT", e);
      }

      return ai;
   }
}
