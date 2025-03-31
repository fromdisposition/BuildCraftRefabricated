package buildcraft.energy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.api.blocks.ICustomRotationHandler;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.energy.tile.TileDynamoMJ;

public class BlockDynamoMJ extends BlockBCTile_Neptune implements ICustomRotationHandler {

    public BlockDynamoMJ(Material material, String id) {
        super(material, id);
    }

    @Override
    public TileBC_Neptune createTileEntity(World world, IBlockState state) {
        return new TileDynamoMJ();
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileDynamoMJ) {
            TileDynamoMJ engine = (TileDynamoMJ) tile;
            if (side == engine.getCurrentDirection().getOpposite()) {
                return BlockFaceShape.SOLID;
            } else {
                return BlockFaceShape.UNDEFINED;
            }
        }
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    @Deprecated
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileDynamoMJ) {
            TileDynamoMJ engine = (TileDynamoMJ) tile;
            return side == engine.getCurrentDirection().getOpposite();
        }
        return false;
    }

    @Override
    @Deprecated
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, block, fromPos);
        if (world.isRemote) return;
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileDynamoMJ) {
            TileDynamoMJ engine = (TileDynamoMJ) tile;
            engine.rotateIfInvalid();
        }
    }

    // ICustomRotationHandler

    @Override
    public EnumActionResult attemptRotation(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileDynamoMJ) {
            TileDynamoMJ engine = (TileDynamoMJ) tile;
            return engine.attemptRotation();
        }
        return EnumActionResult.FAIL;
    }
}
