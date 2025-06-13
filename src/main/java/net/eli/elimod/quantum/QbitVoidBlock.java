package net.eli.elimod.quantum;

import com.mojang.serialization.MapCodec;

import net.eli.elimod.utils.OptDirection;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class QbitVoidBlock extends QbitSpreadBlock{

    public QbitVoidBlock(Settings settings) {
        super(settings);
        isSource = false;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
		return createCodec(QbitVoidBlock::new);
    }

    @Override
    public void reciveQbit(BlockState state, World world, BlockPos pos) {
      if(world.getBlockEntity(pos) instanceof QbitEntity entity){
        entity.clear();
      }
    }


    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        // float diff = 1f/8f;
        float diff = 3f/8f;
        return VoxelShapes.cuboid(0.5f - diff, 0.5f - diff, 0.5f - diff, 0.5f + diff, 0.5f + diff, 0.5f + diff);
    }
}
