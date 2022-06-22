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

import com.mojang.blaze3d.systems.RenderSystem;
import com.slimeist.skylight.SkylightMod;
import com.slimeist.skylight.client.render.sky.context_management.WorldRenderInfo;
import com.slimeist.skylight.client.render.sky.ducks.IEGameRenderer;
import com.slimeist.skylight.client.render.sky.ducks.IEMinecraftClient;
import com.slimeist.skylight.client.render.sky.ducks.IEWorldRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class MyGameRenderer {
    public static MinecraftClient client = MinecraftClient.getInstance();

    // portal rendering and outer world rendering uses different buffer builder storages
    // theoretically every layer of portal rendering should have its own buffer builder storage
    private static BufferBuilderStorage secondaryBufferBuilderStorage = new BufferBuilderStorage();

    // the vanilla visibility sections discovery code is multi-threaded
    // when the player teleports through a portal, on the first frame it will not work normally
    // so use IP's non-multi-threaded algorithm at the first frame
    public static int vanillaTerrainSetupOverride = 0;

    public static void renderWorldNew(
            WorldRenderInfo worldRenderInfo,
            Consumer<Runnable> invokeWrapper
    ) {
        WorldRenderInfo.pushRenderInfo(worldRenderInfo);

        switchAndRenderTheWorld(
                worldRenderInfo.world,
                worldRenderInfo.cameraPos,
                worldRenderInfo.cameraPos,
                invokeWrapper,
                worldRenderInfo.renderDistance,
                worldRenderInfo.doRenderHand,
                client.getTickDelta()
        );

        WorldRenderInfo.popRenderInfo();
    }

    private static void switchAndRenderTheWorld(
            ClientWorld newWorld,
            Vec3d thisTickCameraPos,
            Vec3d lastTickCameraPos,
            Consumer<Runnable> invokeWrapper,
            int renderDistance,
            boolean doRenderHand,
            float tickDelta
    ) {
        resetGlStates();

        Entity cameraEntity = client.cameraEntity;

        Vec3d oldEyePos = McHelper.getEyePos(cameraEntity);
        Vec3d oldLastTickEyePos = McHelper.getLastTickEyePos(cameraEntity);

        ClientWorld oldEntityWorld = ((ClientWorld) cameraEntity.world);

        //switch the camera entity pos
        McHelper.setEyePos(cameraEntity, thisTickCameraPos, lastTickCameraPos);
        cameraEntity.world = newWorld;

        WorldRenderer worldRenderer = client.worldRenderer;

        IEGameRenderer ieGameRenderer = (IEGameRenderer) client.gameRenderer;
        Camera newCamera = new Camera();

        //store old state
        WorldRenderer oldWorldRenderer = client.worldRenderer;
        LightmapTextureManager oldLightmap = client.gameRenderer.getLightmapTextureManager();
        boolean oldNoClip = client.player.noClip;
        boolean oldDoRenderHand = ieGameRenderer.getDoRenderHand();
        Camera oldCamera = client.gameRenderer.getCamera();
        ShaderEffect oldTransparencyShader = ((IEWorldRenderer) worldRenderer).portal_getTransparencyShader();
        BufferBuilderStorage oldBufferBuilder = ((IEWorldRenderer) worldRenderer).ip_getBufferBuilderStorage();
        BufferBuilderStorage oldClientBufferBuilder = client.getBufferBuilders();
        Frustum oldFrustum = ((IEWorldRenderer) worldRenderer).portal_getFrustum();

        // the projection matrix contains view bobbing.
        // the view bobbing is related with scale
        Matrix4f oldProjectionMatrix = RenderSystem.getProjectionMatrix();

        //ObjectArrayList<WorldRenderer.ChunkInfo> newChunkInfoList = VisibleSectionDiscovery.takeList();
        //((IEWorldRenderer) oldWorldRenderer).portal_setChunkInfoList(newChunkInfoList);

        //Object irisPipeline = IrisInterface.invoker.getPipeline(worldRenderer);

        //switch
        ((IEMinecraftClient) client).setWorldRenderer(worldRenderer);
        client.world = newWorld;
        ieGameRenderer.setLightmapTextureManager(client.gameRenderer.getLightmapTextureManager());

        client.getBlockEntityRenderDispatcher().world = newWorld;
        client.player.noClip = true;
        client.gameRenderer.setRenderHand(doRenderHand);

        ieGameRenderer.setCamera(newCamera);

        boolean useSecondaryEntityVertexConsumer = true;
        if (useSecondaryEntityVertexConsumer) {
            ((IEWorldRenderer) worldRenderer).ip_setBufferBuilderStorage(secondaryBufferBuilderStorage);
            ((IEMinecraftClient) client).setBufferBuilderStorage(secondaryBufferBuilderStorage);
        }

        ((IEWorldRenderer) worldRenderer).portal_setTransparencyShader(null);

        //((IEWorldRenderer) worldRenderer).ip_setCloudsDirty(true);

        //invoke rendering
        try {
            invokeWrapper.accept(() -> {
                client.getProfiler().push("render_portal_content");
                client.gameRenderer.renderWorld(
                        tickDelta,
                        Util.getMeasuringTimeNano(),
                        new MatrixStack()
                );
                client.getProfiler().pop();
            });
        }
        catch (Throwable e) {
            SkylightMod.LOGGER.error(e.getLocalizedMessage());
        }

        //recover

        ((IEMinecraftClient) client).setWorldRenderer(oldWorldRenderer);
        client.world = oldEntityWorld;
        ieGameRenderer.setLightmapTextureManager(oldLightmap);
        client.getBlockEntityRenderDispatcher().world = oldEntityWorld;
        client.player.noClip = oldNoClip;
        client.gameRenderer.setRenderHand(oldDoRenderHand);

        ieGameRenderer.setCamera(oldCamera);

        ((IEWorldRenderer) worldRenderer).portal_setTransparencyShader(oldTransparencyShader);


        //((IEWorldRenderer) oldWorldRenderer).portal_setChunkInfoList(oldChunkInfoList);

        ((IEWorldRenderer) worldRenderer).ip_setBufferBuilderStorage(oldBufferBuilder);
        ((IEMinecraftClient) client).setBufferBuilderStorage(oldClientBufferBuilder);

        ((IEWorldRenderer) worldRenderer).portal_setFrustum(oldFrustum);

        RenderSystem.setProjectionMatrix(oldProjectionMatrix);

        client.getEntityRenderDispatcher()
                .configure(
                        client.world,
                        oldCamera,
                        client.targetedEntity
                );

        //restore the camera entity pos
        cameraEntity.world = oldEntityWorld;
        McHelper.setEyePos(cameraEntity, oldEyePos, oldLastTickEyePos);

        resetGlStates();
    }

    public static void resetGlStates() {
        // not working with sodium
//        for (int i = 0; i < 16; i++) {
//            GlStateManager.glActiveTexture(GL20C.GL_TEXTURE0 + i);
//            GlStateManager._bindTexture(0);
//        }
//
//        GlStateManager.glActiveTexture(GL20C.GL_TEXTURE0);

//        GlStateManager.disableAlphaTest();
//        GlStateManager._enableCull();
//        GlStateManager._disableBlend();
//        net.minecraft.client.render.DiffuseLighting.disableGuiDepthLighting();
//        MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().disable();
//        client.gameRenderer.getOverlayTexture().teardownOverlayColor();
    }

    public static void resetFogState() {
        Camera camera = client.gameRenderer.getCamera();
        float g = client.gameRenderer.getViewDistance();

        Vec3d cameraPos = camera.getPos();
        double d = cameraPos.getX();
        double e = cameraPos.getY();
        double f = cameraPos.getZ();

        boolean bl2 = client.world.getDimensionEffects().useThickFog(MathHelper.floor(d), MathHelper.floor(e)) ||
                client.inGameHud.getBossBarHud().shouldThickenFog();

        BackgroundRenderer.applyFog(camera, BackgroundRenderer.FogType.FOG_TERRAIN, Math.max(g - 16.0F, 32.0F), bl2);
        BackgroundRenderer.setFogBlack();
    }

    public static void updateFogColor(float tickDelta) {
        BackgroundRenderer.render(
                client.gameRenderer.getCamera(),
                tickDelta,
                client.world,
                client.options.getViewDistance(),
                client.gameRenderer.getSkyDarkness(tickDelta)
        );
    }

    public static void resetDiffuseLighting(MatrixStack matrixStack) {
        if (client.world.getDimensionEffects().shouldBrightenLighting()) {
            DiffuseLighting.enableForLevel(matrixStack.peek().getPositionMatrix());
        } else {
            DiffuseLighting.disableForLevel(matrixStack.peek().getPositionMatrix());
        }
    }
}
