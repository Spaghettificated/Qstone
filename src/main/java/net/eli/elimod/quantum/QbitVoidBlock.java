package net.eli.elimod.quantum;

import com.mojang.serialization.MapCodec;

import net.eli.elimod.utils.OptDirection;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.util.math.BlockPos;
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
    }

}
