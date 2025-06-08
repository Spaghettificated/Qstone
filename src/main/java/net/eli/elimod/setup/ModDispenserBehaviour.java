package net.eli.elimod.setup;

import net.eli.elimod.things.FragileBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.EquippableDispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ModDispenserBehaviour {
    public static void registerDispenserBehaviours(){
        DispenserBlock.registerBehavior(ModBlocks.FRAGILE_BLOCK, new FallibleItemDispenserBehavior() {
			@Override
			protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
				World world = pointer.world();
				BlockPos blockPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
				FragileBlock block = (FragileBlock)ModBlocks.FRAGILE_BLOCK;
				if (world.isAir(blockPos)) {
					if (!world.isClient) {
						world.setBlockState(blockPos, block.getDefaultState(), Block.NOTIFY_ALL);
						world.emitGameEvent(null, GameEvent.BLOCK_PLACE, blockPos);
                        block.setDemise(world, blockPos);
					}

					stack.decrement(1);
					this.setSuccess(true);
				} else {
					this.setSuccess(EquippableDispenserBehavior.dispense(pointer, stack));
				}

				return stack;
			}
		});
    }
}
