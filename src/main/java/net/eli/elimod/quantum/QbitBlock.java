package net.eli.elimod.quantum;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import net.eli.elimod.utils.OptDirection;;


public abstract class QbitBlock extends BlockWithEntity {
	public static final EnumProperty<OptDirection> SOURCE = EnumProperty.of("qbit_source", OptDirection.class);
	public static final EnumProperty<OptDirection> TARGET = EnumProperty.of("qbit_target", OptDirection.class);
	protected boolean isSource = true;
	protected boolean isTarget = true;
    public boolean isSource() { return isSource; }
	public boolean isTarget() { return isTarget; }


	public QbitBlock(Settings settings) {
		super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(TARGET, OptDirection.NONE).with(SOURCE, OptDirection.NONE));
	}

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SOURCE);
        builder.add(TARGET);
        super.appendProperties(builder);
    }


	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new QbitEntity(pos, state);
	}

	public void setState(Optional<State> qbitOpt, BlockState state, World world, BlockPos pos, @Nullable Block sourceBlock){
		if (!(world.getBlockEntity(pos) instanceof QbitEntity thisQbitEntity)) {
            return;
        }
		if(!thisQbitEntity.getState().equals(qbitOpt)){
			thisQbitEntity.setStateOption(qbitOpt);
			thisQbitEntity.markDirty();
			world.updateListeners(pos, state, state, 0);
			for (Direction dir : Direction.values()) {
				BlockState neighbour = world.getBlockState(pos.offset(dir));
				if(neighbour.getProperties().contains(Properties.FACING)){
					var neighbour_facing = neighbour.get(Properties.FACING);
					if(neighbour_facing != null && neighbour_facing == dir.getOpposite()){
						world.updateNeighbor(pos.offset(dir), this, null);
					}
				}
			}
			// world.updateNeighbors(pos, sourceBlock);
			for (var player : world.getPlayers()) {
				player.sendMessage(Text.literal( "update qbit to" + thisQbitEntity.getState()), true);
			}
		}
	}

	// public boolean reciveQbit(){
		
	public OptDirection findTarget(BlockState state, WorldView world, BlockPos pos){
		for (Direction dir : Direction.values()) {
			var targetDir = world.getBlockState(pos.offset(dir)).get(SOURCE, OptDirection.NONE);
			if( targetDir == OptDirection.from(dir.getOpposite()) ){
				return OptDirection.from(dir);
			}
		}
		return OptDirection.NONE;
	}
	// }
	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView,
			BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
			var new_state = state;
			boolean state_changed = false;
			// System.out.println("qbit in " + world.getBlockState(pos).getBlock().getName() + " at " + pos.toString() + " is updating");
			if (this.isSource()) {
				var neighborSourceDir = neighborState.get(SOURCE, OptDirection.NONE);
				var targetDir = state.get(TARGET, OptDirection.NONE);

				// System.out.println("\tupdating target, starting target is: " + targetDir.toString());

				if( targetDir.isSome() && targetDir.getDirection() == direction){ // jak target zniknął, to go usuń
					// System.out.println("\t\tcuz target was lost");
					if ( neighborSourceDir != OptDirection.from(direction.getOpposite())){
						// new_state = new_state.with(TARGET, OptDirection.NONE); // maybe look for other possible directions
						new_state = new_state.with(TARGET, findTarget(state, world, pos));
						state_changed = true;
					}
				}

				
				if (neighborSourceDir == OptDirection.from(direction.getOpposite())){ // jak pojawił się nowy target to go zmień
					// System.out.println("\t\tcuz we have new target at " + direction.asString());
					new_state = new_state.with(TARGET, OptDirection.from(direction));
					state_changed = true;
				}


			}
			// if (this.isTarget()) {
			// 	var sourceDir = state.get(SOURCE, OptDirection.NONE);
			// }
		// TODO Auto-generated method stub
		world.getBlockEntity(pos).markDirty();
		
		// if (state_changed){
		// 	state.initShapeCache();
		// }
		return super.getStateForNeighborUpdate(new_state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}
	
	public boolean passQbit(BlockState state, World world, BlockPos pos){
		var targetDir = state.get(TARGET, OptDirection.NONE);
		if (targetDir.isSome()){
			var neighbourPos = pos.offset(targetDir.getDirection());
			var neighbour = world.getBlockState(neighbourPos);
			if (neighbour.get(SOURCE, OptDirection.NONE).getDirection().getOpposite() == targetDir.getDirection()
				&& world.getBlockEntity(neighbourPos) instanceof QbitEntity neighbourEntity
				&& world.getBlockEntity(pos) instanceof QbitEntity thisEntity
				&& thisEntity.isPresent()) {
					boolean success;
					if(world.getBlockEntity(pos) instanceof QbitEntity entity)
						System.out.println("passing: " + entity.getState().get().toString() + " " + Integer.toString(entity.getQbitNumber()) + " | " + Integer.toString(entity.getQbit_pos()));

					if (isTarget())
						success = neighbourEntity.takeFrom(thisEntity, world);
					else
						success = neighbourEntity.clone(thisEntity);
					if (success){
						world.updateNeighbor(neighbourPos, this, null);
						world.updateListeners(pos, state, state, 0);
						world.updateListeners(neighbourPos, neighbour, neighbour, 0);

						if(neighbour.getBlock() instanceof QbitBlock neighbourBlock)
							neighbourBlock.reciveQbit(neighbour, world, neighbourPos);
	
					}

					return success;
			}
		}

		
		return false;
	}
	public void reciveQbit(BlockState state, World world, BlockPos pos) {

		if(world.getBlockEntity(pos) instanceof QbitEntity entity && entity.isPresent()){
			System.out.println("reciving: " + entity.getState().get().toString() + " " + Integer.toString(entity.getQbitNumber()) + " | " + Integer.toString(entity.getQbit_pos()));

			for (BlockPos epos : entity.getEntangled()){
				// world.updateListeners(pos, state, state, 0);

				if(world.getBlockEntity(epos) instanceof QbitEntity otherEntity && entity.isPresent()){
					otherEntity.markDirty();
				}

				if (epos != pos){
					world.updateNeighbor(epos, null, null);
					world.updateListeners(epos, world.getBlockState(epos), world.getBlockState(epos), 0);
				}
			}
		}
	}


}
