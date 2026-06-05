/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.core.statements;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;

public class StatementParameterItemStackExact implements IStatementParameter {
    protected ItemStack stack = ItemStack.EMPTY;

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return stack;
    }

    @Override
    public StatementParameterItemStackExact onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        if (!stack.isEmpty()) {
            if (areItemsEqual(this.stack, stack)) {
                if (mouse.getButton() == 0) {
                    this.stack.setCount(Math.min(64, this.stack.getCount() + (mouse.isShift() ? 16 : 1)));
                } else {
                    this.stack.setCount(this.stack.getCount() - (mouse.isShift() ? 16 : 1));
                    if (this.stack.getCount() <= 0) {
                        this.stack = ItemStack.EMPTY;
                    }
                }
            } else {
                this.stack = stack.copy();
            }
        } else {
            if (!this.stack.isEmpty()) {
                if (mouse.getButton() == 0) {
                    this.stack.setCount(Math.min(64, this.stack.getCount() + (mouse.isShift() ? 16 : 1)));
                } else {
                    this.stack.setCount(this.stack.getCount() - (mouse.isShift() ? 16 : 1));
                    if (this.stack.getCount() <= 0) {
                        this.stack = ItemStack.EMPTY;
                    }
                }
            }
        }
        return this;
    }

    @Override
    public void writeToNbt(CompoundTag compound) {
        if (!stack.isEmpty()) {
            ItemStack.CODEC.encodeStart(buildcraft.lib.misc.NBTUtilBC.registryAwareOps(), stack)
                    .resultOrPartial()
                    .ifPresent(payload -> compound.put("stack", payload));
        }
    }

    public static StatementParameterItemStackExact readFromNbt(CompoundTag nbt) {
        StatementParameterItemStackExact param = new StatementParameterItemStackExact();
        Tag stackPayload = nbt.get("stack");
        if (stackPayload != null) {
            param.stack = ItemStack.CODEC.parse(buildcraft.lib.misc.NBTUtilBC.registryAwareOps(), stackPayload)
                    .resultOrPartial()
                    .orElse(ItemStack.EMPTY);
        }
        return param;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof StatementParameterItemStackExact param) {
            return areItemsEqual(stack, param.stack);
        }
        return false;
    }

    private static boolean areItemsEqual(ItemStack stack1, ItemStack stack2) {
        if (!stack1.isEmpty()) {
            return !stack2.isEmpty() && ItemStack.isSameItemSameComponents(stack1, stack2);
        } else {
            return stack2.isEmpty();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack);
    }

    @Override
    public String getDescription() {
        if (!stack.isEmpty()) {
            return stack.getHoverName().getString();
        } else {
            return "";
        }
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:stackExact";
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public ISprite getSprite() {
        return null;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source) {
        return null;
    }
}
