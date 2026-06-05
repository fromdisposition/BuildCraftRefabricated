/* Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.robots;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import buildcraft.api.core.BCLog;
import buildcraft.api.mj.MjAPI;

public class AIRobot {
    public EntityRobotBase robot;

    private AIRobot delegateAI;
    private AIRobot parentAI;

    private boolean success;

    public AIRobot(EntityRobotBase iRobot) {
        robot = iRobot;
        success = true;
    }

    public void start() {

    }

    public void preempt(AIRobot ai) {

    }

    public void update() {

        terminate();
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
        return success;
    }

    protected void setSuccess(boolean iSuccess) {
        success = iSuccess;
    }

    public long getPowerCost() {
        return MjAPI.MJ / 10;
    }

    public boolean canLoadFromNBT() {
        return false;
    }

    public ItemStack receiveItem(ItemStack stack) {
        return stack;
    }

    public final void terminate() {
        abortDelegateAI();
        end();

        if (parentAI != null) {
            parentAI.delegateAI = null;
            parentAI.delegateAIEnded(this);
        }
    }

    public final void abort() {
        abortDelegateAI();

        try {
            end();

            if (parentAI != null) {
                parentAI.delegateAI = null;
                parentAI.delegateAIAborted(this);
            }
        } catch (Throwable e) {
            BCLog.logger.warn("[robots] Robot delegate AI threw; aborting it", e);
            delegateAI = null;

            if (parentAI != null) {
                parentAI.delegateAI = null;
            }
        }
    }

    public final void cycle() {
        try {
            preempt(delegateAI);

            if (delegateAI != null) {
                delegateAI.cycle();
            } else {
                robot.getBattery().extractPower(1, getPowerCost());
                update();
            }
        } catch (Throwable e) {
            BCLog.logger.warn("[robots] Robot AI update threw; aborting", e);
            abort();
        }
    }

    public final void startDelegateAI(AIRobot ai) {
        abortDelegateAI();
        delegateAI = ai;
        ai.parentAI = this;
        delegateAI.start();
    }

    public final void abortDelegateAI() {
        if (delegateAI != null) {
            delegateAI.abort();
        }
    }

    public final AIRobot getActiveAI() {
        if (delegateAI != null) {
            return delegateAI.getActiveAI();
        } else {
            return this;
        }
    }

    public final AIRobot getDelegateAI() {
        return delegateAI;
    }

    public final void writeToNBT(CompoundTag nbt) {
        nbt.putString("aiName", RobotManager.getAIRobotName(getClass()));

        CompoundTag data = new CompoundTag();
        writeSelfToNBT(data);
        nbt.put("data", data);

        if (delegateAI != null && delegateAI.canLoadFromNBT()) {
            CompoundTag sub = new CompoundTag();

            delegateAI.writeToNBT(sub);
            nbt.put("delegateAI", sub);
        }
    }

    public final void loadFromNBT(CompoundTag nbt) {
        loadSelfFromNBT(nbt.getCompound("data").orElse(new net.minecraft.nbt.CompoundTag()));

        if (nbt.contains("delegateAI")) {
            CompoundTag sub = nbt.getCompound("delegateAI").orElse(new net.minecraft.nbt.CompoundTag());

            try {
                Class<?> aiRobotClass;
                if (sub.contains("class")) {

                    aiRobotClass = RobotManager.getAIRobotByLegacyClassName(sub.getString("class").orElse(""));
                } else {
                    aiRobotClass = RobotManager.getAIRobotByName(sub.getString("aiName").orElse(""));
                }
                if (aiRobotClass != null) {
                    delegateAI = (AIRobot) aiRobotClass.getConstructor(EntityRobotBase.class).newInstance(robot);
                    delegateAI.parentAI = this;

                    if (delegateAI.canLoadFromNBT()) {
                        delegateAI.loadFromNBT(sub);
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
                ai = (AIRobot) aiRobotClass.getConstructor(EntityRobotBase.class).newInstance(robot);
                ai.loadFromNBT(nbt);
            }
        } catch (Throwable e) {
            BCLog.logger.warn("[robots] Failed to instantiate robot AI from NBT", e);
        }

        return ai;
    }
}
