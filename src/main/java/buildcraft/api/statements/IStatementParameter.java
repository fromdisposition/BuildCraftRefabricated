/* Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.statements;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import buildcraft.api.statements.StatementManager.IParamReaderBuf;
import buildcraft.api.statements.StatementManager.IParameterReader;

public interface IStatementParameter extends IGuiSlot {

    @Nonnull
    ItemStack getItemStack();

    default DrawType getDrawType() {
        return DrawType.SPRITE_STACK;
    }

    IStatementParameter onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
        StatementMouseClick mouse);

    void writeToNbt(CompoundTag nbt);

    default void writeToBuf(FriendlyByteBuf buffer) {
        CompoundTag nbt = new CompoundTag();
        writeToNbt(nbt);
        buffer.writeNbt(nbt);
    }

    IStatementParameter rotateLeft();

    IStatementParameter[] getPossible(IStatementContainer source);

    default boolean isPossibleOrdered() {
        return false;
    }

    public enum DrawType {

        SPRITE_ONLY,

        STACK_ONLY,

        STACK_ONLY_OR_QUESTION_MARK,

        SPRITE_STACK,

        SPRITE_STACK_OR_QUESTION_MARK,

        STACK_SPRITE,

        STACK_OR_QUESTION_MARK_THEN_SPRITE
    }
}
