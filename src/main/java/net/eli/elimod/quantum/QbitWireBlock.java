package net.eli.elimod.quantum;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;

public class QbitWireBlock extends QbitSpreadBlock {

	public QbitWireBlock(Settings settings) {
        super(settings);
    }

    @Override
	protected MapCodec<? extends QbitBlock> getCodec() {
		return createCodec(QbitWireBlock::new);
	}
    

	// @Override
    // protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
    //     if (!(world.getBlockEntity(pos) instanceof QbitEntity blockEntity)) {
    //         return super.onUse(state, world, pos, player, hit);
    //     }

    //     blockEntity.marker_id++;
    //     blockEntity.updated = true;
    //     return ActionResult.SUCCESS;
    // }
    



    // @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        var source = sourceQbit(state, world, pos);
        if (source.isPresent()) {
            setState(source.get().getQbit(), state, world, pos, sourceBlock);
        }
        else{
            setState(Optional.empty(), state, world, pos, sourceBlock);
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        // float diff = 1f/8f;
        float diff = 3f/8f;
        return VoxelShapes.cuboid(0.5f - diff, 0.5f - diff, 0.5f - diff, 0.5f + diff, 0.5f + diff, 0.5f + diff);
    }
}
