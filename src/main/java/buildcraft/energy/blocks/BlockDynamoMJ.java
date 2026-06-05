package buildcraft.energy.blocks;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.lib.engine.TileEngineBase_BC8;

@SuppressWarnings("this-escape")
public class BlockDynamoMJ extends Block implements EntityBlock, ICustomRotationHandler {

    public BlockDynamoMJ(Properties properties) {
        super(properties.noOcclusion());
        registerDefaultState(defaultBlockState().setValue(BuildCraftProperties.BLOCK_FACING_6, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BuildCraftProperties.BLOCK_FACING_6);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(BuildCraftProperties.BLOCK_FACING_6, ctx.getClickedFace());
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rot) {
        return state.setValue(BuildCraftProperties.BLOCK_FACING_6, rot.rotate(state.getValue(BuildCraftProperties.BLOCK_FACING_6)));
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        Direction facing = state.getValue(BuildCraftProperties.BLOCK_FACING_6);
        return switch (facing) {
            case DOWN -> Block.box(0, 12, 0, 16, 16, 16);
            case UP -> Block.box(0, 0, 0, 16, 4, 16);
            case NORTH -> Block.box(0, 0, 12, 16, 16, 16);
            case SOUTH -> Block.box(0, 0, 0, 16, 16, 4);
            case WEST -> Block.box(12, 0, 0, 16, 16, 16);
            case EAST -> Block.box(0, 0, 0, 4, 16, 16);
        };
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileDynamoMJ(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileDynamoMJ dynamo) {
                dynamo.onPlacedBy(placer, stack);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide()) {
            return (lvl, pos, st, be) -> {
                if (be instanceof TileDynamoMJ dynamo) {
                    TileDynamoMJ.serverTick(lvl, pos, st, dynamo);
                }
            };
        } else {
            return (lvl, pos, st, be) -> {
                if (be instanceof TileDynamoMJ dynamo) {
                    dynamo.clientTick();
                }
            };
        }
    }

    @Override
    public InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (world.isClientSide()) return InteractionResult.SUCCESS;
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof TileDynamoMJ dynamo) {
            if (dynamo.attemptRotation()) {
                world.setBlock(pos, state.setValue(BuildCraftProperties.BLOCK_FACING_6, dynamo.getOrientation()), 3);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof IItemPipe pipe) {
            InteractionResult placed = EnginePipeInteraction.tryPlacePipe(
                    pipe, stack, level, player, hand, hitResult, PipeApi.flowPower, PipeApi.flowRf);
            return placed != null ? placed : openGui(state, level, pos, player);
        }

        if (player.isShiftKeyDown()) {
            return openGui(state, level, pos, player);
        }

        if (stack.getItem() instanceof IToolWrench) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileEngineBase_BC8 engine && engine.hasAlternateReceiver()) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide()) {
                level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.4f, 1.3f);
            }
            player.swing(hand);
            return InteractionResult.CONSUME;
        }

        return openGui(state, level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hitResult) {
        return openGui(state, level, pos, player);
    }

    private InteractionResult openGui(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileDynamoMJ dynamo && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(dynamo);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn,
            @Nullable Orientation orientation, boolean isMoving) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileDynamoMJ dynamo) {
            dynamo.onNeighborUpdate();
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileDynamoMJ dynamo) {
            buildcraft.lib.misc.BlockDropsUtil.dropItems(level, pos, dynamo.upgrades);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
