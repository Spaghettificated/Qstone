package net.eli.elimod.quantum;

import com.mojang.serialization.MapCodec;

import net.eli.elimod.utils.OptDirection;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class QbitMeasurementBlock extends QbitSpreadBlock {

    public QbitMeasurementBlock(Settings settings) {
        super(settings);
        // this.setDefaultState(this.stateManager.getDefaultState().with(TARGET, OptDirection.NONE).with(SOURCE, OptDirection.NONE));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
		return createCodec(QbitMeasurementBlock::new);
    }

    @Override
    public void reciveQbit(BlockState state, World world, BlockPos pos) {
      if (world.getBlockEntity(pos) instanceof QbitEntity entity && entity.isPresent()){
        State Qstate = entity.getState().get();
        State state0 = State.projection(Qbit.ZERO).extended(entity.getQbitNumber(), entity.getQbit_pos()).actOn(Qstate);
        State state1 = State.projection(Qbit.ONE ).extended(entity.getQbitNumber(), entity.getQbit_pos()).actOn(Qstate);
        var prob0 = state0.normSqr();
        var prob1 = state1.normSqr();
        var r = world.random.nextDouble();
        System.out.println("Randomize: 0:" + Double.toString(prob0) + ", 1:" + Double.toString(prob1));
        System.out.println("Randomized: " + Double.toString(r));
        if (r < (prob0 / (prob0+prob1))) {
          entity.collapse(Qbit.ZERO);
        }
        else{
          entity.collapse(Qbit.ONE);
        }
        

      }
      super.reciveQbit(state, world, pos);
    }
}
