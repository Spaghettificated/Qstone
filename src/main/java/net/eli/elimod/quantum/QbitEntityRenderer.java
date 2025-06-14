package net.eli.elimod.quantum;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.jcraft.jorbis.Block;

import net.eli.elimod.utils.OptDirection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

@Environment(EnvType.CLIENT)
public class QbitEntityRenderer implements BlockEntityRenderer<QbitEntity> {
    // A jukebox itemstack
    // private static final ItemStack[] markerItems = {
    //     new ItemStack(Items.RED_TERRACOTTA),
    //     new ItemStack(Items.GREEN_TERRACOTTA),
    //     new ItemStack(Items.BLUE_TERRACOTTA),
    //     new ItemStack(Items.YELLOW_TERRACOTTA),
    //     new ItemStack(Items.PURPLE_TERRACOTTA),
    // };
    private static final ItemStack[] markerItems = {
        new ItemStack(Items.RED_CONCRETE),
        new ItemStack(Items.GREEN_CONCRETE),
        new ItemStack(Items.BLUE_CONCRETE),
        new ItemStack(Items.CYAN_CONCRETE),
        new ItemStack(Items.MAGENTA_CONCRETE),
        new ItemStack(Items.YELLOW_CONCRETE),
        new ItemStack(Items.BLACK_CONCRETE),
    };
    private static final ItemStack entangleMarker = new ItemStack(Items.MAGENTA_STAINED_GLASS);
    public static final double marker_size = 0.4;
    public static final double marigin_size = 0.03;
    public int marker_id =0;
 
    public QbitEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(QbitEntity entity, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {
        // if(!entity.isVisible){
        //     return;
        // }
        // entity.updateBlochVecs();
        Vec3d[] blochVecs = entity.getBlochVecs();


        

        // int j = entity.marker_id;
        int j = marker_id;
        Vec3d center = new Vec3d(0.5, 0.5, 0.5);
        matrices.push();
        matrices.translate(center);

        var matrix = matrices.peek();

        var target = entity.getCachedState().get(QbitBlock.TARGET, OptDirection.NONE);
        if (target.isSome()){
            var targetBuffer = vertexConsumers.getBuffer(RenderLayer.getLines());
            targetBuffer.vertex(matrix, new Vector3f(0,0,0)).color(255,0,0,255).normal(0, 1, 0);
            targetBuffer.vertex(matrix, target.getDirection().getUnitVector().mul(0.5f) ).color(255,0,0,255).normal(0, 1, 0);
        }

        var source = entity.getCachedState().get(QbitBlock.SOURCE, OptDirection.NONE);
        if (source.isSome()){
            var sourceBuffer = vertexConsumers.getBuffer(RenderLayer.getLines());
            sourceBuffer.vertex(matrix, new Vector3f(0,0,0)).color(0,255,0,255).normal(0, 1, 0);
            sourceBuffer.vertex(matrix, source.getDirection().getUnitVector().mul(0.5f) ).color(0,255,0,255).normal(0, 1, 0);
        }
        
        
        // if(entity.getState().isPresent())  System.out.println("rendering: " + entity.getState().get().toString() + " " + Integer.toString(entity.getQbitNumber()) + " | " + Integer.toString(entity.getQbit_pos()));
        
        // if(entity.getQbitNumber() > 1 && entity.getQbit_pos() == 0){
        if(entity.getQbitNumber() > 1){
            var entanglineBuffer = vertexConsumers.getBuffer(RenderLayer.getLines());
            for (var pos : entity.getEntangled().clone()) {
                var thisPos = entity.getPos();
                var v = new Vector3f(pos.getX() - thisPos.getX(), pos.getY() - thisPos.getY(), pos.getZ() - thisPos.getZ());
                entanglineBuffer.vertex(matrix, v)
                                        .color(255,0,255,255)
                                        .normal(0, 1, 0);
            }

            matrices.push();
            var s = 0.55f;
            matrices.scale(s,s,s);
            int lala = WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos());
            MinecraftClient.getInstance().getItemRenderer().renderItem(entangleMarker, ItemDisplayContext.FIXED, lala, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), 0);
            matrices.pop();
        }

        for (int i = 0 +j; i < blochVecs.length +j; i++) {
            Vec3d bloch = blochVecs[i -j];
            // bloch = bloch.normalize().multiply( Math.sqrt(bloch.length()));
            // bloch = bloch.multiply(10);
            // bloch = bloch.normalize();
            ItemStack markerItem = markerItems[i % markerItems.length];

            // var lineBuffer = vertexConsumers.getBuffer(RenderLayer.getLines());
            // lineBuffer.vertex(matrix, new Vector3f(0,0,0)).color(1,0,1,255).normal(0, 1, 0);
            // lineBuffer.vertex(matrix, bloch.multiply(0.5- marker_size/4).toVector3f() ).color(1,0,1,255).normal(0, 1, 0);

            
            
            matrices.push();

            // Move the item
            matrices.translate(bloch.multiply(0.5 - marker_size/4. - marigin_size));
            matrices.scale((float)marker_size, (float)marker_size, (float)marker_size);
            // matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(30*i));
            // matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(20*i));
     
            // Rotate the item
            // matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((entity.getWorld().getTime() + tickDelta) * 4));
    
            int lightAbove = WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos());
            MinecraftClient.getInstance().getItemRenderer().renderItem(markerItem, ItemDisplayContext.FIXED, lightAbove, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), 0);
     
            // Mandatory call after GL calls
            matrices.pop();
            
        }     
        
        matrices.pop();
    }
 
    
}
