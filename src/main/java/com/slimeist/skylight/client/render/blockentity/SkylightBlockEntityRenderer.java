package com.slimeist.skylight.client.render.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.slimeist.skylight.SkylightMod;
import com.slimeist.skylight.client.render.sky.MyGameRenderer;
import com.slimeist.skylight.client.render.sky.SecondaryFrameBuffer;
import com.slimeist.skylight.client.render.sky.context_management.WorldRenderInfo;
import com.slimeist.skylight.client.render.sky.ducks.IEMinecraftClient;
import com.slimeist.skylight.client.render.sky.q_misc_util.SignalBiArged;
import com.slimeist.skylight.common.block.entity.SkylightBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.Uniform;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class SkylightBlockEntityRenderer implements BlockEntityRenderer<SkylightBlockEntity> {

    public static boolean isRenderingPortal() {
        return secondaryFrameBuffer.fb != null && MinecraftClient.getInstance().getFramebuffer().fbo == secondaryFrameBuffer.fb.fbo;//renderingPortal;
    }

    static SecondaryFrameBuffer secondaryFrameBuffer = new SecondaryFrameBuffer();

    static Camera originalCamera;

    public SkylightBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    public static final SignalBiArged<ResourceManager, Consumer<Shader>> loadShaderSignal =
            new SignalBiArged<>();

    private static ResourceFactory getResourceFactory(ResourceManager resourceManager) {
        ResourceFactory resourceFactory = new ResourceFactory() {
            @Override
            public Resource getResource(Identifier id) throws IOException {
                Identifier corrected = new Identifier(SkylightMod.MODID, id.getPath());
                return resourceManager.getResource(corrected);
            }
        };
        return resourceFactory;
    }

    public static void init() {
        originalCamera = MinecraftClient.getInstance().gameRenderer.getCamera();
        loadShaderSignal.connect((resourceManager, resultConsumer) -> {
            try {
                DrawFbInAreaShader shader = new DrawFbInAreaShader(
                        getResourceFactory(resourceManager),
                        "portal_draw_fb_in_area",
                        VertexFormats.POSITION_COLOR
                );
                resultConsumer.accept(shader);
                drawFbInAreaShader = shader;
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static class DrawFbInAreaShader extends Shader {

        public final Uniform uniformW;
        public final Uniform uniformH;

        public DrawFbInAreaShader(
                ResourceFactory factory, String name, VertexFormat format
        ) throws IOException {
            super(factory, name, format);

            uniformW = getUniform("w");
            uniformH = getUniform("h");
        }

        void loadWidthHeight(int w, int h) {
            uniformW.set((float) w);
            uniformH.set((float) h);
        }
    }

    public static DrawFbInAreaShader drawFbInAreaShader;

    protected void invokeWorldRendering(WorldRenderInfo worldRenderInfo) {
        MyGameRenderer.renderWorldNew(worldRenderInfo, Runnable::run);
    }

    //private static long lastRenderedTime = -1;
    private static float lastRenderedDelta = -1;

    @Override
    public void render(SkylightBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (isRenderingPortal()) {
            return;
        }
        matrices.push();
        boolean renderingPortal = true;
        //Some vars
        MinecraftClient client = MinecraftClient.getInstance();

        if (lastRenderedDelta==-1 || lastRenderedDelta!=tickDelta) {//(lastRenderedTime==-1 || lastRenderedTime!=blockEntity.getWorld().getTime()) {
            //lastRenderedTime = client.world.getTime();
            lastRenderedDelta = tickDelta;
            //Prepare framebuffer
            secondaryFrameBuffer.prepare();
            GlStateManager._enableDepthTest();
            Framebuffer oldFrameBuffer = client.getFramebuffer();
            ((IEMinecraftClient) client).setFrameBuffer(secondaryFrameBuffer.fb);
            secondaryFrameBuffer.fb.beginWrite(true);

            GlStateManager._clearColor(1, 0, 1, 1);
            GlStateManager._clearDepth(1);
            GlStateManager._clear(
                    GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT,
                    MinecraftClient.IS_SYSTEM_MAC
            );
            GL11.glDisable(GL11.GL_STENCIL_TEST);

            Vec3d myOriginPos = new Vec3d(blockEntity.getPos().getX(), blockEntity.getPos().getY(), blockEntity.getPos().getZ()).add(0, 2, 0);
            //myPos = client.cameraEntity.getCameraPosVec(tickDelta);
            Vec3d camPos = originalCamera.getPos();
            Vec3d localPos = camPos.subtract(myOriginPos);
            Vec3d myPos = localPos.add(myOriginPos.add(0, 1.5 + 0.125, 0));
            long oldTimeOfDay = client.world.getTimeOfDay();
            boolean oldDO_DAYLIGHT_CYCLE = client.world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).get();
            //client.world.setTimeOfDay(12000L+oldTimeOfDay);
            invokeWorldRendering(new WorldRenderInfo(client.world, myPos, matrices.peek().getPositionMatrix(), false, null, client.options.getViewDistance()));
            client.world.setTimeOfDay(oldTimeOfDay);
            client.world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(oldDO_DAYLIGHT_CYCLE, null);

            MyGameRenderer.updateFogColor(tickDelta);
            MyGameRenderer.resetFogState();
//            MyGameRenderer.resetDiffuseLighting(matrices); //Causes lighting issues

            GlStateManager._enableDepthTest();

            //cleanup frame buffer
            ((IEMinecraftClient) client).setFrameBuffer(oldFrameBuffer);
            oldFrameBuffer.beginWrite(true);
        }

        //Render second framebuffer into main framebuffer
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._enableDepthTest();
        GlStateManager._depthMask(true);
        GlStateManager._viewport(0, 0, secondaryFrameBuffer.fb.textureWidth, secondaryFrameBuffer.fb.textureHeight);

        DrawFbInAreaShader shader = drawFbInAreaShader;
        shader.addSampler("DiffuseSampler", secondaryFrameBuffer.fb.getColorAttachment());
        shader.loadWidthHeight(secondaryFrameBuffer.fb.textureWidth, secondaryFrameBuffer.fb.textureHeight);

        if (shader.modelViewMat != null) {
            shader.modelViewMat.set(matrices.peek().getPositionMatrix());
        }

        if (shader.projectionMat != null) {
            shader.projectionMat.set(RenderSystem.getProjectionMatrix());
        }

        shader.bind();

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        /*ViewAreaRenderer.buildPortalViewAreaTrianglesBuffer(
                Vec3.ZERO,//fog
                portal,
                bufferBuilder,
                CHelper.getCurrentCameraPos(),
                RenderStates.tickDelta
        );*/
        MatrixStack skyMatrices = new MatrixStack();
        skyMatrices.push();
        renderSides(blockEntity, skyMatrices.peek().getPositionMatrix(), bufferBuilder, Vec3d.ZERO);
        skyMatrices.pop();
        bufferBuilder.end();
        //BufferUploader._endInternal
        BufferRenderer.postDraw(bufferBuilder);
        // wrong name. unbind
        shader.unbind();

        //Restore viewport
        GlStateManager._viewport(
                0,
                0,
                client.getWindow().getWidth(),
                client.getWindow().getHeight()
        );


        //end portal rendering
        //Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        //this.renderSides(blockEntity, matrix4f, vertexConsumers.getBuffer(this.getLayer()));
        renderingPortal = false;
        matrices.pop();
    }

    @Override
    public boolean isInRenderDistance(SkylightBlockEntity blockEntity, Vec3d pos) {
        return true;
    }

    //End portal rendering
    private void renderSides(SkylightBlockEntity entity, Matrix4f matrix, VertexConsumer vertexConsumer, Vec3d fogColor) {
        float bottom = this.getBottomYOffset();
        float top = this.getTopYOffset();
        this.renderSide(entity, matrix, vertexConsumer, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, Direction.SOUTH, fogColor);
        this.renderSide(entity, matrix, vertexConsumer, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, Direction.NORTH, fogColor);
        this.renderSide(entity, matrix, vertexConsumer, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, Direction.EAST, fogColor);
        this.renderSide(entity, matrix, vertexConsumer, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, Direction.WEST, fogColor);
        this.renderSide(entity, matrix, vertexConsumer, 0.0f, 1.0f, bottom, bottom, 0.0f, 0.0f, 1.0f, 1.0f, Direction.DOWN, fogColor);
        this.renderSide(entity, matrix, vertexConsumer, 0.0f, 1.0f, top, top, 1.0f, 1.0f, 0.0f, 0.0f, Direction.UP, fogColor);
    }

    private void renderSide(SkylightBlockEntity entity, Matrix4f model, VertexConsumer vertices, float x1, float x2, float y1, float y2, float z1, float z2, float z3, float z4, Direction side, Vec3d fogColor) {
        vertices.vertex(model, x1, y1, z1).color((int) fogColor.getX()*255, (int) fogColor.getY()*255, (int) fogColor.getZ()*255, 255).next();
        vertices.vertex(model, x2, y1, z2).color((int) fogColor.getX()*255, (int) fogColor.getY()*255, (int) fogColor.getZ()*255, 255).next();
        vertices.vertex(model, x2, y2, z3).color((int) fogColor.getX()*255, (int) fogColor.getY()*255, (int) fogColor.getZ()*255, 255).next();
        vertices.vertex(model, x1, y2, z4).color((int) fogColor.getX()*255, (int) fogColor.getY()*255, (int) fogColor.getZ()*255, 255).next();
    }

    protected float getTopYOffset() {
        return 1;
    }

    protected float getBottomYOffset() {
        return 0;
    }
}
