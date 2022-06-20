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
import com.slimeist.skylight.mixin.GameRendererAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.gl.Uniform;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class SkylightBlockEntityRenderer implements BlockEntityRenderer<SkylightBlockEntity> {

    private static ItemStack stack = new ItemStack(Items.JUKEBOX, 1);

    private static boolean renderingPortal = false;

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

    protected void renderContent(SkylightBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        //Render item
        matrices.push();
        // Calculate the current offset in the y value
        double offset = Math.sin((blockEntity.getWorld().getTime() + tickDelta) / 8.0) / 4.0;
        // Move the item
        matrices.translate(0.5, 1.25 + offset, 0.5);

        // Rotate the item
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((blockEntity.getWorld().getTime() + tickDelta) * 4));

        int lightAbove = WorldRenderer.getLightmapCoordinates(blockEntity.getWorld(), blockEntity.getPos().up());
        MinecraftClient client = MinecraftClient.getInstance();
        client.getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, lightAbove, overlay, matrices, vertexConsumers, 0);

        matrices.pop();
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

    @Override
    public void render(SkylightBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (isRenderingPortal()) {
            return;
        }
        matrices.push();
        renderingPortal = true;
        //Some vars
        MinecraftClient client = MinecraftClient.getInstance();

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

        //renderContent(blockEntity, tickDelta, matrices, vertexConsumers, light, overlay);
        Vec3d myOriginPos = new Vec3d(blockEntity.getPos().getX(), blockEntity.getPos().getY(), blockEntity.getPos().getZ()).add(0, 2, 0);
        //myPos = client.cameraEntity.getCameraPosVec(tickDelta);
        Vec3d camPos = originalCamera.getPos();
        Vec3d localPos = camPos.subtract(myOriginPos);
        Vec3d myPos = localPos.add(myOriginPos.add(0, 1.5+0.125, 0));
        invokeWorldRendering(new WorldRenderInfo(client.world, myPos, matrices.peek().getPositionMatrix(), false, null, 2));//client.options.getViewDistance()));
        GlStateManager._enableDepthTest();

        //cleanup frame buffer
        ((IEMinecraftClient) client).setFrameBuffer(oldFrameBuffer);
        oldFrameBuffer.beginWrite(true);

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
        renderSides(blockEntity, skyMatrices.peek().getPositionMatrix(), bufferBuilder, new Vec3f(1.0f, 0.0f, 0.75f));
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
    private void renderSides(SkylightBlockEntity entity, Matrix4f matrix, VertexConsumer vertexConsumer, Vec3f fogColor) {
        float bottom = this.getBottomYOffset();
        float top = this.getTopYOffset();
        this.renderSide(entity, matrix, vertexConsumer, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, Direction.SOUTH, fogColor);
        this.renderSide(entity, matrix, vertexConsumer, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, Direction.NORTH, fogColor);
        this.renderSide(entity, matrix, vertexConsumer, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, Direction.EAST, fogColor);
        this.renderSide(entity, matrix, vertexConsumer, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, Direction.WEST, fogColor);
        this.renderSide(entity, matrix, vertexConsumer, 0.0f, 1.0f, bottom, bottom, 0.0f, 0.0f, 1.0f, 1.0f, Direction.DOWN, fogColor);
        this.renderSide(entity, matrix, vertexConsumer, 0.0f, 1.0f, top, top, 1.0f, 1.0f, 0.0f, 0.0f, Direction.UP, fogColor);
    }

    private void renderSide(SkylightBlockEntity entity, Matrix4f model, VertexConsumer vertices, float x1, float x2, float y1, float y2, float z1, float z2, float z3, float z4, Direction side, Vec3f fogColor) {
        vertices.vertex(model, x1, y1, z1).color(fogColor.getX(), fogColor.getY(), fogColor.getZ(), 1.0f).next();
        vertices.vertex(model, x2, y1, z2).color(fogColor.getX(), fogColor.getY(), fogColor.getZ(), 1.0f).next();
        vertices.vertex(model, x2, y2, z3).color(fogColor.getX(), fogColor.getY(), fogColor.getZ(), 1.0f).next();
        vertices.vertex(model, x1, y2, z4).color(fogColor.getX(), fogColor.getY(), fogColor.getZ(), 1.0f).next();
    }

    protected float getTopYOffset() {
        return 1;
    }

    protected float getBottomYOffset() {
        return 0;
    }

    protected RenderLayer getLayer() {
        return RenderLayer.getEndPortal();
    }

    private void renderSkyBroken() {
        /*if (false) {//sky rendering
            GameRenderer gameRenderer = client.gameRenderer;
            Shader currentShader = RenderSystem.getShader();
            Camera camera = client.gameRenderer.getCamera();

            MatrixStack skyMatrices = new MatrixStack();

            //GameRenderer setup
            double fov = ((GameRendererAccessor) gameRenderer).callGetFov(camera, tickDelta, true);
            skyMatrices.peek().getPositionMatrix().multiply(gameRenderer.getBasicProjectionMatrix(fov));
            ((GameRendererAccessor) gameRenderer).callBobViewWhenHurt(skyMatrices, tickDelta);
            if (client.options.bobView) {
                ((GameRendererAccessor) gameRenderer).callBobView(skyMatrices, tickDelta);
            }
            Matrix4f posMatrix = skyMatrices.peek().getPositionMatrix();
            //posMatrix.multiply(matrices.peek().getPositionMatrix());
            gameRenderer.loadProjectionMatrix(posMatrix);
            skyMatrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
            skyMatrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0f));
            Matrix3f normalMatrix = skyMatrices.peek().getNormalMatrix().copy();
            if (normalMatrix.invert()) {
                RenderSystem.setInverseViewRotationMatrix(normalMatrix);
            }

            //WorldRenderer

            Vec3d vec3d = camera.getPos();
            double d = vec3d.getX();
            double e = vec3d.getY();
            double f = vec3d.getZ();
            //skyMatrices.scale(0.5f, 0.5f, 0.5f);
            Matrix4f matrix4f = skyMatrices.peek().getPositionMatrix();

            //Fog
            BackgroundRenderer.render(camera, tickDelta, client.world, client.options.getViewDistance(), gameRenderer.getSkyDarkness(tickDelta));
            BackgroundRenderer.setFogBlack();

            //sky rendering
            boolean thickFog = client.world.getDimensionEffects().useThickFog(MathHelper.floor(camera.getPos().getX()), MathHelper.floor(camera.getPos().getY())) || client.inGameHud.getBossBarHud().shouldThickenFog();
            RenderSystem.setShader(GameRenderer::getPositionShader);
            MinecraftClient.getInstance().worldRenderer.renderSky(skyMatrices, matrix4f, tickDelta, camera, thickFog, () -> BackgroundRenderer.applyFog(camera, BackgroundRenderer.FogType.FOG_SKY, client.gameRenderer.getViewDistance(), thickFog));
            //fog
            BackgroundRenderer.applyFog(camera, BackgroundRenderer.FogType.FOG_TERRAIN, Math.max(gameRenderer.getViewDistance(), 32.0f), thickFog);
            //clouds
            client.worldRenderer.renderClouds(skyMatrices, matrix4f, tickDelta, d, e, f);

            RenderSystem.setShader(() -> currentShader);
        }*/
    }
}
