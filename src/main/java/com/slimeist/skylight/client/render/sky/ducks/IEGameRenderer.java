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

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;

public interface IEGameRenderer {
    void setLightmapTextureManager(LightmapTextureManager manager);
    
    boolean getDoRenderHand();
    
    void setCamera(Camera camera);
    
    //void setIsRenderingPanorama(boolean cond);
    
    //void portal_bobView(MatrixStack matrixStack, float tickDelta);
}
