package com.slimeist.skylight.mixin;

import com.slimeist.skylight.client.render.sky.ducks.IEMinecraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin implements IEMinecraftClient {
    @Final
    @Shadow
    @Mutable
    private Framebuffer framebuffer;

    @Mutable
    @Shadow
    @Final
    public WorldRenderer worldRenderer;

    @Mutable
    @Shadow
    @Final
    private BufferBuilderStorage bufferBuilders;

    @Override
    public void setFrameBuffer(Framebuffer buffer) {
        framebuffer = buffer;
    }

    @Override
    public void setWorldRenderer(WorldRenderer r) {
        worldRenderer = r;
    }

    @Override
    public void setBufferBuilderStorage(BufferBuilderStorage arg) {
        bufferBuilders = arg;
    }
}
