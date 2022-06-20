package com.slimeist.skylight.common.block.entity;

import com.slimeist.skylight.SkylightMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

public class SkylightBlockEntity extends BlockEntity {
    public SkylightBlockEntity(BlockPos pos, BlockState state) {
        super(SkylightMod.SKYLIGHT_BLOCK_ENTITY, pos, state);
    }
}
