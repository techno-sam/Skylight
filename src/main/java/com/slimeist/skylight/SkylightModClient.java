package com.slimeist.skylight;

import com.slimeist.skylight.client.render.blockentity.SkylightBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;

import static com.slimeist.skylight.SkylightMod.SKYLIGHT_BLOCK;
import static com.slimeist.skylight.SkylightMod.SKYLIGHT_BLOCK_ENTITY;

public class SkylightModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(SKYLIGHT_BLOCK_ENTITY, SkylightBlockEntityRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(SKYLIGHT_BLOCK, RenderLayer.getTranslucent());
        MinecraftClient.getInstance().execute(() -> {
            SkylightBlockEntityRenderer.init();
        });
    }
}
