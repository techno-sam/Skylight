package com.slimeist.skylight.client.render.sky;

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

import com.slimeist.skylight.SkylightMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;

public class SecondaryFrameBuffer {
    public SimpleFramebuffer fb;

    public void prepare() {
        Framebuffer mainFrameBuffer = MinecraftClient.getInstance().getFramebuffer();
        int width = mainFrameBuffer.viewportWidth;
        int height = mainFrameBuffer.viewportHeight;
        prepare(width, height);
    }

    public void prepare(int width, int height) {
        if (fb == null) {
            fb = new SimpleFramebuffer(
                    width, height,
                    true,//has depth attachment
                    MinecraftClient.IS_SYSTEM_MAC
            );
            fb.checkFramebufferStatus();
            SkylightMod.LOGGER.info("Secondary Framebuffer init");
        }
        if (width != fb.viewportWidth ||
                height != fb.viewportHeight
        ) {
            fb.resize(
                    width, height, MinecraftClient.IS_SYSTEM_MAC
            );
            fb.checkFramebufferStatus();
            SkylightMod.LOGGER.info("Secondary Framebuffer resized");
        }
    }


}
