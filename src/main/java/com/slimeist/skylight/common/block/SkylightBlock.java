package com.slimeist.skylight.common.block;

import com.slimeist.skylight.common.block.entity.SkylightBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class SkylightBlock extends AbstractGlassBlock implements BlockEntityProvider {
    public SkylightBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SkylightBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.NORMAL;
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }
}
