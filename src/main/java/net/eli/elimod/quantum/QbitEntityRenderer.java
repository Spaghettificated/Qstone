package net.eli.elimod.quantum;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class QbitEntityRenderer implements BlockEntityRenderer<QbitEntity> {
    // A jukebox itemstack
    private static final ItemStack[] markerItems = {
        new ItemStack(Items.RED_TERRACOTTA),
        new ItemStack(Items.GREEN_TERRACOTTA),
        new ItemStack(Items.BLUE_TERRACOTTA),
        new ItemStack(Items.YELLOW_TERRACOTTA),
        new ItemStack(Items.PURPLE_TERRACOTTA),
    };
 
    public QbitEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(QbitEntity entity, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {
        if(!entity.isVisible){
            return;
        }
        // entity.updateBlochVecs();
        Vec3d[] blochVecs = entity.getBlochVecs();

        for (int i = 0; i < blochVecs.length; i++) {
            Vec3d bloch = blochVecs[i];
            ItemStack markerItem = markerItems[i % markerItems.length];
            
            matrices.push();

            // Move the item
            Vec3d center = new Vec3d(0.5, 0.5, 0.5);
            double size = 0.7;
            matrices.translate(center.add(bloch.multiply(0.5 - size/4. - 0.001)));
            matrices.scale((float)size, (float)size, (float)size);
     
            // Rotate the item
            // matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((entity.getWorld().getTime() + tickDelta) * 4));
    
            int lightAbove = WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up());
            MinecraftClient.getInstance().getItemRenderer().renderItem(markerItem, ItemDisplayContext.FIXED, lightAbove, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), 0);
     
            // Mandatory call after GL calls
            matrices.pop();
        }     
    }
 
    
}
