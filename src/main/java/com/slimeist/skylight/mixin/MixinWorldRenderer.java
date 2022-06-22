package com.slimeist.skylight.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.slimeist.skylight.client.render.blockentity.SkylightBlockEntityRenderer;
import com.slimeist.skylight.client.render.sky.ducks.IEWorldRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer implements IEWorldRenderer {
    @Shadow
    private ShaderEffect transparencyShader;


    @Mutable
    @Shadow
    @Final
    private BufferBuilderStorage bufferBuilders;

    @Shadow
    private Frustum frustum;

    @Shadow
    @Nullable
    private Framebuffer translucentFramebuffer;

    @Shadow
    private boolean cloudsDirty;

    @Override
    public ShaderEffect portal_getTransparencyShader() {
        return transparencyShader;
    }

    @Override
    public void portal_setTransparencyShader(ShaderEffect arg) {
        transparencyShader = arg;
    }

    @Override
    public BufferBuilderStorage ip_getBufferBuilderStorage() {
        return bufferBuilders;
    }

    @Override
    public void ip_setBufferBuilderStorage(BufferBuilderStorage arg) {
        bufferBuilders = arg;
    }

    @Override
    public Frustum portal_getFrustum() {
        return frustum;
    }

    @Override
    public void portal_setFrustum(Frustum arg) {
        frustum = arg;
    }

    @Override
    public void ip_setCloudsDirty(boolean arg) {
        cloudsDirty = arg;
    }

    private static void c(CallbackInfo ci) {
        if (SkylightBlockEntityRenderer.isRenderingPortal()) {
            ci.cancel();
        }
    }

    @Inject(at=@At("HEAD"), method="setupTerrain", cancellable = true)
    public void cancelTerrainSetup(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
        c(ci);
    }

    @Inject(at=@At("HEAD"), method="updateChunks", cancellable = true)
    public void cancelUpdateChunks(Camera camera, CallbackInfo ci) {
        c(ci);
    }

    @Inject(at=@At("HEAD"), method="renderLayer", cancellable = true)
    public void cancelRenderLayer(RenderLayer renderLayer, MatrixStack matrices, double d, double e, double f, Matrix4f positionMatrix, CallbackInfo ci) {
        c(ci);
    }

    @Inject(at=@At("HEAD"), method="renderWorldBorder", cancellable = true)
    public void cancelRenderWorldBorder(Camera camera, CallbackInfo ci) {
        c(ci);
    }

    @Redirect(method="render", at=@At(value="FIELD", target="Lnet/minecraft/client/render/WorldRenderer;translucentFramebuffer:Lnet/minecraft/client/gl/Framebuffer;"))
    private Framebuffer redirectTranslucentFramebuffer(WorldRenderer instance) {
        if (SkylightBlockEntityRenderer.isRenderingPortal()) {
            return null;
        } else {
            return translucentFramebuffer;
        }
    }

    @Inject(at=@At("HEAD"), method="renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FDDD)V", cancellable = true)
    public void messWithClouds(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, double d, double e, double f, CallbackInfo ci) {
        if (SkylightBlockEntityRenderer.isRenderingPortal()) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}
