package com.slimeist.skylight.mixin;

import com.slimeist.skylight.client.render.blockentity.SkylightBlockEntityRenderer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class MixinParticleManager {
    private static void c(CallbackInfo ci) {
        if (SkylightBlockEntityRenderer.isRenderingPortal()) {
            ci.cancel();
        }
    }

    @Inject(at=@At("HEAD"), method="renderParticles", cancellable = true)
    public void cancelParticleRender(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, LightmapTextureManager lightmapTextureManager, Camera camera, float tickDelta, CallbackInfo ci) {
        c(ci);
    }
}
