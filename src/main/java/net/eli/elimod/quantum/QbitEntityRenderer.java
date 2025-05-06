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
    private static final ItemStack stack = new ItemStack(Items.RED_TERRACOTTA);
 
    public QbitEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(QbitEntity entity, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {

        var qbitOpt = entity.getQbit();
        if(qbitOpt.isPresent()){
            var qbit = qbitOpt.get();
            double y = State.dot(qbit, Gate.Z.actOn(qbit)).re;
            Vec3d bloch = new Vec3d(State.dot(qbit, Gate.X.actOn(qbit)).re, 
                                    State.dot(qbit, Gate.Z.actOn(qbit)).re,
                                    State.dot(qbit, Gate.Y.actOn(qbit)).re);
    
            matrices.push();
    
            // double offset = Math.sin((entity.getWorld().getTime() + tickDelta) / 8.0) / 4.0;
            Vec3d center = new Vec3d(0.5, 0.5, 0.5);
            // Vec3d size = new Vec3d(0.1, 0.1, 0.1);
            double size = 0.7;
            // Vec3d offset = new Vec3d(0., 0.75, 0.);
            // Move the item
            // matrices.translate(center);
            matrices.translate(center.add(bloch.multiply(0.5 - size/4. - 0.001)));
            matrices.scale((float)size, (float)size, (float)size);
     
            // Rotate the item
            // matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((entity.getWorld().getTime() + tickDelta) * 4));
    
            int lightAbove = WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up());
            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ItemDisplayContext.FIXED, lightAbove, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), 0);
     
            // Mandatory call after GL calls
            matrices.pop();
        }

    }
 
    
}
