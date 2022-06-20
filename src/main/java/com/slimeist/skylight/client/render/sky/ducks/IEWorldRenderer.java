package com.slimeist.skylight.client.render.sky.ducks;

import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.entity.EntityRenderDispatcher;

public interface IEWorldRenderer {
    //EntityRenderDispatcher ip_getEntityRenderDispatcher();
    
    /*ViewArea ip_getBuiltChunkStorage();
    
    ChunkRenderDispatcher getChunkBuilder();
    
    void ip_myRenderEntity(
        Entity entity,
        double cameraX,
        double cameraY,
        double cameraZ,
        float tickDelta,
        PoseStack matrixStack,
        MultiBufferSource vertexConsumerProvider
    );*/
    
    ShaderEffect portal_getTransparencyShader();
    
    void portal_setTransparencyShader(ShaderEffect arg);
    
    BufferBuilderStorage ip_getBufferBuilderStorage();
    
    void ip_setBufferBuilderStorage(BufferBuilderStorage arg);
    
    Frustum portal_getFrustum();
    
    void portal_setFrustum(Frustum arg);
    
    //void portal_fullyDispose();
    
    //void portal_setChunkInfoList(ObjectArrayList<WorldRenderer.ChunkInfo> arg);
    
    //ObjectArrayList<WorldRenderer.ChunkInfo> portal_getChunkInfoList();
}
