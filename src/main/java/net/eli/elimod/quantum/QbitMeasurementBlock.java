package net.eli.elimod.quantum;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.eli.elimod.utils.OptDirection;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;

public class QbitMeasurementBlock extends QbitSpreadBlock {
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final BooleanProperty EMITS = BooleanProperty.of("emits");

    public QbitMeasurementBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(POWERED, false).with(EMITS, false));
    }

	@Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        builder.add(EMITS);
        super.appendProperties(builder);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(QbitMeasurementBlock::new);
    }

    @Nullable
    public Boolean meassure(BlockState state, World world, BlockPos pos){
        if (world.getBlockEntity(pos) instanceof QbitEntity entity && entity.isPresent()){
            State Qstate = entity.getState().get();
            State state0 = State.projection(Qbit.ZERO).extended(entity.getQbitNumber(), entity.getQbit_pos()).actOn(Qstate);
            State state1 = State.projection(Qbit.ONE ).extended(entity.getQbitNumber(), entity.getQbit_pos()).actOn(Qstate);
            var prob0 = state0.normSqr();
            var prob1 = state1.normSqr();
            var r = world.random.nextDouble();
            System.out.println("Randomize: 0:" + Double.toString(prob0) + ", 1:" + Double.toString(prob1));
            System.out.println("Randomized: " + Double.toString(r));
            boolean out;
            if (r < (prob0 / (prob0+prob1))) {
                entity.collapse(Qbit.ZERO);
                out =  false;
            }
            else{
                entity.collapse(Qbit.ONE);
                out =  true;
            }
            for (BlockPos epos : entity.getEntangled()){
              if (epos != pos){
                // var estate = world.getBlockState(epos);
                // world.updateListeners(epos, estate, estate, FORCE_STATE);
                world.updateNeighbor(epos, null, null);
              }
            }
            return out;
        }
        return null;
    }

    @Override
    public void reciveQbit(BlockState state, World world, BlockPos pos) {
        super.reciveQbit(state, world, pos);
    }
    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock,
            WireOrientation wireOrientation, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, wireOrientation, notify);
        boolean bl = world.isReceivingRedstonePower(pos);
        boolean powered = state.get(POWERED);
        boolean emits = state.get(EMITS);

        BlockState newState = state;
        if(bl && !powered){
            var meassured = meassure(newState, world, pos);
            if (meassured!=null){
              newState = newState.with(EMITS, meassured);
              passQbit(newState, world, pos);
              world.playSound((Entity)null, pos, SoundEvents.BLOCK_COPPER_BULB_TURN_ON , SoundCategory.BLOCKS);
            }
        }
        if(!bl && emits){
            newState = newState.with(EMITS, false);
        }
        world.setBlockState(pos, newState.with(POWERED, bl), 3);
    } 
    @Override
    protected boolean emitsRedstonePower(BlockState state) {
      return state.get(EMITS);
    }
    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        var odir = OptDirection.from(direction);
        if (direction != Direction.DOWN && direction != Direction.UP 
            && odir != state.get(TARGET, OptDirection.NONE) && odir != state.get(SOURCE, OptDirection.NONE) 
            && state.get(EMITS))
          return 15;
        else 
          return 0;
    }

    
    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
    }

}
