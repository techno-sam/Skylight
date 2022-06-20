package com.slimeist.skylight.mixin;

import com.slimeist.skylight.client.render.blockentity.SkylightBlockEntityRenderer;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {
    private static void c(CallbackInfo ci) {
        if (SkylightBlockEntityRenderer.isRenderingPortal()) {
            ci.cancel();
        }
    }

    @Inject(at=@At("HEAD"), method="render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", cancellable = true)
    private <E extends BlockEntity> void cancelBlockEntityRender(
            E blockEntity,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        c(ci);
    }

    @Inject(at=@At("HEAD"), method="renderEntity", cancellable = true)
    private <E extends BlockEntity> void cancelBlockEntityRender(
            E entity,
            MatrixStack matrix,
            VertexConsumerProvider vertexConsumerProvider,
            int light, int overlay, CallbackInfoReturnable<Boolean> cir) {
        c(cir);
    }
}
