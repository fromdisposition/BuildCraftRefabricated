/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.api;

@SuppressWarnings("unchecked")
public interface IVariableNode extends IExpressionNode {

    void set(IExpressionNode from);

    void setConstant(boolean isConst);

    boolean isConstant();

    public interface IVariableNodeLong extends IVariableNode, INodeLong {
        void set(long value);

        @Override
        default void set(IExpressionNode from) {
            set(((INodeLong) from).evaluate());
        }
    }

    public interface IVariableNodeDouble extends IVariableNode, INodeDouble {
        void set(double value);

        @Override
        default void set(IExpressionNode from) {
            set(((INodeDouble) from).evaluate());
        }
    }

    public interface IVariableNodeBoolean extends IVariableNode, INodeBoolean {
        void set(boolean value);

        @Override
        default void set(IExpressionNode from) {
            set(((INodeBoolean) from).evaluate());
        }
    }

    public interface IVariableNodeObject<T> extends IVariableNode, INodeObject<T> {
        void set(T value);

        default void setUnchecked(Object to) {
            if (to.getClass() != getType()) {
                throw new ClassCastException(to.getClass() + " cannot be cast to " + getType());
            }
            set((T) to);
        }

        @Override
        default void set(IExpressionNode from) {
            setUnchecked(((INodeObject<?>) from).evaluate());
        }
    }
}
