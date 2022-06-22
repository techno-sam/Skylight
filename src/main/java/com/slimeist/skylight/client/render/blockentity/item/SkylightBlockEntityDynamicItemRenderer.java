package com.slimeist.skylight.client.render.blockentity.item;

import com.slimeist.skylight.SkylightMod;
import com.slimeist.skylight.common.block.entity.SkylightBlockEntity;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class SkylightBlockEntityDynamicItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private final SkylightBlockEntity renderSkylight = new SkylightBlockEntity(BlockPos.ORIGIN, SkylightMod.SKYLIGHT_BLOCK.getDefaultState());
    @Override
    public void render(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(renderSkylight, matrices, vertexConsumers, light, overlay);
    }
}
