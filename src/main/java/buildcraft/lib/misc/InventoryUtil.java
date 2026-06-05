package buildcraft.lib.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import buildcraft.lib.attachments.Attachments;
import buildcraft.lib.transfer.item.ItemHandlerView;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.ResourceHandlerUtil;
import buildcraft.lib.transfer.item.ItemResource;

import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.api.transport.pipe.IPipeHolder;

public class InventoryUtil {

    public static void addAll(ResourceHandler<ItemResource> handler, List<ItemStack> list) {
        for (int i = 0; i < handler.size(); i++) {
            ItemResource res = handler.getResource(i);
            if (!res.isEmpty()) {
                list.add(res.toStack(handler.getAmountAsInt(i)));
            }
        }
    }

    public static void addToBestAcceptor(Level level, BlockPos pos, @Nullable Direction ignore, @Nonnull ItemStack stack) {
        if (stack.isEmpty()) return;
        stack = addToRandomInjectable(level, pos, ignore, stack);
        stack = addToRandomInventory(level, pos, stack);
        if (!stack.isEmpty()) {
            drop(level, pos, stack);
        }
    }

    @Nonnull
    public static ItemStack addToRandomInjectable(Level level, BlockPos pos, @Nullable Direction ignore, @Nonnull ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        List<Direction> toTry = new ArrayList<>(6);
        Collections.addAll(toTry, Direction.values());
        Collections.shuffle(toTry);

        for (Direction face : toTry) {
            if (face == ignore) continue;
            if (stack.isEmpty()) return ItemStack.EMPTY;

            BlockPos adjPos = pos.relative(face);
            BlockEntity tile = level.getBlockEntity(adjPos);
            if (tile == null) continue;

            IInjectable injectable = getInjectable(tile, face.getOpposite());
            if (injectable == null) continue;

            stack = injectable.injectItem(stack, true, face.getOpposite(), null, 0);
            if (stack.isEmpty()) return ItemStack.EMPTY;
        }
        return stack;
    }

    @Nullable
    private static IInjectable getInjectable(BlockEntity tile, Direction face) {
        if (tile instanceof IPipeHolder holder) {
            var pipe = holder.getPipe();
            if (pipe != null) {
                PipeFlow flow = pipe.getFlow();
                if (flow != null) {
                    Object result = flow.getCapability(PipeApi.CAP_INJECTABLE, face);
                    if (result instanceof IInjectable injectable) {
                        return injectable;
                    }
                }
            }
        }
        return null;
    }

    @Nonnull
    public static ItemStack addToRandomInventory(Level level, BlockPos pos, @Nonnull ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        List<Direction> toTry = new ArrayList<>(6);
        Collections.addAll(toTry, Direction.values());
        Collections.shuffle(toTry);

        ItemResource resource = ItemResource.of(stack);
        int remaining = stack.getCount();

        for (Direction face : toTry) {
            if (remaining <= 0) return ItemStack.EMPTY;

            BlockPos adjPos = pos.relative(face);

            ResourceHandler<ItemResource> handler = buildcraft.lib.attachments.AttachmentQueries.getBlock(
                    level, Attachments.Item.BLOCK, adjPos, face.getOpposite());
            if (handler == null) continue;

            int inserted = ResourceHandlerUtil.insertStacking(handler, resource, remaining, null);
            remaining -= inserted;
        }

        if (remaining <= 0) return ItemStack.EMPTY;
        return stack.copyWithCount(remaining);
    }

    public static void drop(Level level, BlockPos pos, @Nonnull ItemStack stack) {
        if (!stack.isEmpty()) {
            Block.popResource(level, pos, stack);
        }
    }
}
