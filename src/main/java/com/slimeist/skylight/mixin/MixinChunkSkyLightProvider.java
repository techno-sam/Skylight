package com.slimeist.skylight.mixin;

import com.slimeist.skylight.SkylightMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.chunk.light.ChunkSkyLightProvider;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChunkSkyLightProvider.class)
public class MixinChunkSkyLightProvider {
    @Inject(at=@At(value="INVOKE", target="Lnet/minecraft/world/chunk/light/ChunkSkyLightProvider;getOpaqueShape(Lnet/minecraft/block/BlockState;JLnet/minecraft/util/math/Direction;)Lnet/minecraft/util/shape/VoxelShape;", ordinal=0), method="getPropagatedLevel", locals=LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void modifyStateForLighting(long sourceId, long targetId, int level, CallbackInfoReturnable<Integer> cir, MutableInt mutableInt, BlockState blockState, int i, int j, int k, int l, int m, int n, int o, int p, int q, Direction direction, BlockState blockState2) {
        if (blockState2.isOf(SkylightMod.SKYLIGHT_BLOCK)) {
            if (true) {//i==l && k==n && j > m) {
                SkylightMod.LOGGER.info((blockState2.isOpaque()?"opaque":"non opaque")+(blockState2.hasSidedTransparency()?" sided transparent":" not sided transparent"));
                if (mutableInt.getValue()==0) {
                    cir.setReturnValue(0);
                } else {
                    cir.setReturnValue(Math.max(1, mutableInt.getValue()));
                }
                cir.cancel();
            }
        }
    }
}
