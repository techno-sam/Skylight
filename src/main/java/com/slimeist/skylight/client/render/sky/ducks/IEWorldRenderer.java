package com.slimeist.skylight.client.render.sky.ducks;

/*
   Copyright 2020 qouteall

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

Modified by Slimeist to only render the sky
 */

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

    void ip_setCloudsDirty(boolean arg);
    
    //void portal_fullyDispose();
    
    //void portal_setChunkInfoList(ObjectArrayList<WorldRenderer.ChunkInfo> arg);
    
    //ObjectArrayList<WorldRenderer.ChunkInfo> portal_getChunkInfoList();
}
