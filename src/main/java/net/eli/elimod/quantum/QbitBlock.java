package net.eli.elimod.quantum;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;


public abstract class QbitBlock extends BlockWithEntity {
    public QbitBlock(Settings settings) {
		super(settings);
	}


	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new QbitEntity(pos, state);
	}

	public void setState(Optional<Qbit> qbitOpt, BlockState state, World world, BlockPos pos, @Nullable Block sourceBlock){
		if (!(world.getBlockEntity(pos) instanceof QbitEntity thisQbitEntity)) {
            return;
        }
		if(!thisQbitEntity.getQbit().equals(qbitOpt)){
			thisQbitEntity.setQbitOption(qbitOpt);
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
				player.sendMessage(Text.literal( "update qbit to" + thisQbitEntity.getQbit()), true);
			}
		}


	}


}
