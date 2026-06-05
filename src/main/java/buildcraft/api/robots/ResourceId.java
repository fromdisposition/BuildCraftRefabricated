/* Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.robots;

import net.minecraft.nbt.CompoundTag;

import buildcraft.api.core.BCLog;

public abstract class ResourceId {

    protected ResourceId() {}

    public void writeToNBT(CompoundTag nbt) {
        nbt.putString("resourceName", RobotManager.getResourceIdName(getClass()));
    }

    protected void readFromNBT(CompoundTag nbt) {}

    public static ResourceId load(CompoundTag nbt) {
        try {
            Class<?> cls;
            if (nbt.contains("class")) {

                cls = RobotManager.getResourceIdByLegacyClassName(nbt.getString("class").orElse(""));
            } else {
                cls = RobotManager.getResourceIdByName(nbt.getString("resourceName").orElse(""));
            }

            ResourceId id = (ResourceId) cls.getDeclaredConstructor().newInstance();
            id.readFromNBT(nbt);

            return id;
        } catch (Throwable e) {
            BCLog.logger.warn("[robots] Failed to load a ResourceId from NBT", e);
        }

        return null;
    }
}
