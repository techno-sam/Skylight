package com.slimeist.skylight.client.render.sky.context_management;

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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A world rendering task.
 */
public class WorldRenderInfo {

    /**
     * The dimension that it's going to render
     */
    public final ClientWorld world;

    /**
     * Camera position
     */
    public final Vec3d cameraPos;

    public final boolean overwriteCameraTransformation;

    /**
     * If overwriteCameraTransformation is true,
     * the world rendering camera transformation will be replaced by this.
     * If overwriteCameraTransformation is false,
     * this will be applied to the original camera transformation, and this can be null
     */
    @Nullable
    public final Matrix4f cameraTransformation;

    /**
     * Used for visibility prediction optimization
     */
    @Nullable
    public final UUID description;

    /**
     * Render distance.
     * It cannot render the chunks that are not synced to client.
     */
    public final int renderDistance;

    public final boolean doRenderHand;

    private static final Stack<WorldRenderInfo> renderInfoStack = new Stack<>();

    public WorldRenderInfo(
            ClientWorld world, Vec3d cameraPos,
            @Nullable Matrix4f cameraTransformation,
            boolean overwriteCameraTransformation,
            @Nullable UUID description,
            int renderDistance
    ) {
        this(
                world, cameraPos, cameraTransformation, overwriteCameraTransformation,
                description, renderDistance, false
        );
    }

    public WorldRenderInfo(
            ClientWorld world, Vec3d cameraPos,
            @Nullable Matrix4f cameraTransformation,
            boolean overwriteCameraTransformation,
            @Nullable UUID description,
            int renderDistance,
            boolean doRenderHand
    ) {
        this.world = world;
        this.cameraPos = cameraPos;
        this.cameraTransformation = cameraTransformation;
        this.description = description;
        this.renderDistance = renderDistance;
        this.overwriteCameraTransformation = overwriteCameraTransformation;
        this.doRenderHand = doRenderHand;
    }

    public static void pushRenderInfo(WorldRenderInfo worldRenderInfo) {
        renderInfoStack.push(worldRenderInfo);
    }

    public static void popRenderInfo() {
        renderInfoStack.pop();
    }

    /*public static void adjustCameraPos(Camera camera) {
        if (!renderInfoStack.isEmpty()) {
            WorldRenderInfo currWorldRenderInfo = renderInfoStack.peek();
            ((IECamera) camera).portal_setPos(currWorldRenderInfo.cameraPos);
        }
    }*/

    public static void applyAdditionalTransformations(MatrixStack matrixStack) {
        for (WorldRenderInfo worldRenderInfo : renderInfoStack) {
            if (worldRenderInfo.overwriteCameraTransformation) {
                matrixStack.peek().getPositionMatrix().loadIdentity();
                matrixStack.peek().getNormalMatrix().loadIdentity();
            }

            Matrix4f matrix = worldRenderInfo.cameraTransformation;
            if (matrix != null) {
                matrixStack.peek().getPositionMatrix().multiply(matrix);

                Matrix3f normalMatrixMult = new Matrix3f(matrix);
                // make its determinant 1 so it won't scale the normal vector
                normalMatrixMult.multiply(
                        (float) Math.pow(1.0 / Math.abs(normalMatrixMult.determinant()), 1.0 / 3)
                );
                matrixStack.peek().getNormalMatrix().multiply(normalMatrixMult);
            }
        }
    }

    /**
     * it's different from {@link PortalRendering#isRendering()}
     * when rendering cross portal third person view, this is true
     * but {@link PortalRendering#isRendering()} is false
     */
    public static boolean isRendering() {
        return !renderInfoStack.empty();
    }

    public static int getRenderingLayer() {
        return renderInfoStack.size();
    }

    // for example rendering portal B inside portal A will always have the same rendering description
    public static List<UUID> getRenderingDescription() {
        return renderInfoStack.stream()
                .map(renderInfo -> renderInfo.description).collect(Collectors.toList());
    }

    public static int getRenderDistance() {
        if (renderInfoStack.isEmpty()) {
            return MinecraftClient.getInstance().options.viewDistance;
        }

        return renderInfoStack.peek().renderDistance;
    }
}
