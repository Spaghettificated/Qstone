package net.eli.elimod.quantum;

import com.mojang.serialization.MapCodec;

import net.eli.elimod.utils.OptDirection;
import net.minecraft.block.BlockWithEntity;

public class QbitMeasurementBlock extends QbitBlock {

    public QbitMeasurementBlock(Settings settings) {
        super(settings);
        // this.setDefaultState(this.stateManager.getDefaultState().with(TARGET, OptDirection.NONE).with(SOURCE, OptDirection.NONE));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
		return createCodec(QbitMeasurementBlock::new);
    }
}
