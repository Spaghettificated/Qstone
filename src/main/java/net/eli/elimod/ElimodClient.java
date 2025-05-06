package net.eli.elimod;

import net.eli.elimod.quantum.Qbit;
import net.eli.elimod.quantum.QbitEntityRenderer;
import net.eli.elimod.setup.ModBlockEntities;
import net.eli.elimod.setup.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

@Environment(EnvType.CLIENT)
public class ElimodClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.QBIT_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.QBIT_BLOCK, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.QBIT_WIRE, RenderLayer.getTranslucent());
        // for (var block : ModBlocks.QBIT_GATE_BLOCKS) {
        //     BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getIn());
        // }
        BlockEntityRendererFactories.register(ModBlockEntities.QBIT_ENTITY, QbitEntityRenderer::new);
    }

    
}
