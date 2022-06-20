package com.slimeist.skylight.mixin;

import com.slimeist.skylight.client.render.blockentity.SkylightBlockEntityRenderer;
import com.slimeist.skylight.client.render.sky.ducks.IEGameRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer implements IEGameRenderer {
    @Shadow
    @Final
    @Mutable
    private LightmapTextureManager lightmapTextureManager;

    @Shadow
    private boolean renderHand;

    @Shadow
    @Final
    @Mutable
    private Camera camera;

    @Override
    public void setLightmapTextureManager(LightmapTextureManager manager) {
        lightmapTextureManager = manager;
    }

    @Override
    public boolean getDoRenderHand() {
        return renderHand;
    }

    @Override
    public void setCamera(Camera camera_) {
        camera = camera_;
    }

    @Inject(at=@At("HEAD"), method="shouldRenderBlockOutline", cancellable = true)
    public void cancelOutline(CallbackInfoReturnable<Boolean> cir) {
        if (SkylightBlockEntityRenderer.isRenderingPortal()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(at=@At("HEAD"), method="updateTargetedEntity", cancellable = true)
    public void cancelTargetUpdate(float tickDelta, CallbackInfo ci) {
        if (SkylightBlockEntityRenderer.isRenderingPortal()) {
            ci.cancel();
        }
    }
}
