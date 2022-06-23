package com.slimeist.skylight.mixin;

import com.slimeist.skylight.SkylightMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DaylightDetectorBlock;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.chunk.light.ChunkSkyLightProvider;
import net.minecraft.world.chunk.light.LightStorage;
import net.minecraft.world.chunk.light.SkyLightStorage;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChunkSkyLightProvider.class)
public abstract class MixinChunkSkyLightProvider extends ChunkLightProvider {

    private MixinChunkSkyLightProvider(ChunkProvider chunkProvider, LightType type, LightStorage lightStorage) {
        super(chunkProvider, type, lightStorage);
    }

    @Inject(at=@At("HEAD"), method="getPropagatedLevel", cancellable = true)
    public void getPropagatedLevel(long sourceId, long targetId, int level, CallbackInfoReturnable<Integer> cir) {
        if (false) {
            ChunkSkyLightProvider this_ = (ChunkSkyLightProvider) (Object) this;
            BlockState sourceState = this.getStateForLighting(sourceId, null);
            MutableInt mutableInt = new MutableInt();
            BlockState targetState = this.getStateForLighting(targetId, mutableInt);
            if (sourceState.isOf(SkylightMod.SKYLIGHT_BLOCK)) {
                if (mutableInt.getValue() == 0) {
                    cir.setReturnValue(0);
                } else {
                    cir.setReturnValue(Math.max(1, mutableInt.getValue()));
                }
                cir.cancel();
            }
        }
    }
}
