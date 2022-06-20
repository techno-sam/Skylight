package com.slimeist.skylight.mixin;

import com.slimeist.skylight.client.render.blockentity.SkylightBlockEntityRenderer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
    private void c(CallbackInfo ci) {
        if (SkylightBlockEntityRenderer.isRenderingPortal()) {
            ci.cancel();
        }
    }

    @Inject(at=@At("HEAD"), method="shouldRender", cancellable = true)
    public <E extends Entity> void cancelEntityRender(E entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        c(cir);
    }
}
