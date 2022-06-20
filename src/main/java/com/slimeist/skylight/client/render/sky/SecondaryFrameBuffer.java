package com.slimeist.skylight.client.render.sky;

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
