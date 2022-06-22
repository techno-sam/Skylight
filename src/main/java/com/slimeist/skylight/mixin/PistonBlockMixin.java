package com.slimeist.skylight.mixin;

import com.slimeist.skylight.SkylightMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBlock.class)
public class PistonBlockMixin {
    @Inject(at=@At(value="INVOKE", target="Lnet/minecraft/block/BlockState;hasBlockEntity()Z"), method="isMovable", cancellable = true)
    private static void makeMovable(BlockState state, World world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir, CallbackInfoReturnable<Boolean> cir) {
        if (state.isOf(SkylightMod.SKYLIGHT_BLOCK)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
