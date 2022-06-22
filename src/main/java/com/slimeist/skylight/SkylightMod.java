package com.slimeist.skylight;

import com.slimeist.skylight.common.block.SkylightBlock;
import com.slimeist.skylight.common.block.entity.SkylightBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkylightMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MODID = "skylight";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	//BLOCKS
	public static final Block SKYLIGHT_BLOCK = new SkylightBlock(FabricBlockSettings.of(Material.METAL, MapColor.LIGHT_BLUE_GRAY)
			.strength(0.3f)
			.sounds(BlockSoundGroup.GLASS)
			//.nonOpaque()
			.allowsSpawning(SkylightMod::never)
			.solidBlock(SkylightMod::never)
			.suffocates(SkylightMod::never)
			.blockVision(SkylightMod::never)
			//.luminance(15)
			//.emissiveLighting(SkylightMod::always)
			.requiresTool()
	);

	//BLOCK ENTITIES
	public static BlockEntityType<SkylightBlockEntity> SKYLIGHT_BLOCK_ENTITY;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		Registry.register(Registry.BLOCK, id("skylight"), SKYLIGHT_BLOCK);
		Registry.register(Registry.ITEM, id("skylight"), new BlockItem(SKYLIGHT_BLOCK, new FabricItemSettings().group(ItemGroup.DECORATIONS)));
		SKYLIGHT_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, str_id("skylight"), FabricBlockEntityTypeBuilder.create(SkylightBlockEntity::new, SKYLIGHT_BLOCK).build());
	}

	public static Identifier id(String path) {
		return new Identifier(MODID, path);
	}

	public static String str_id(String path) {
		return MODID + ":" + path;
	}


	/**
	 * A shortcut to always return {@code false} in a typed context predicate with an
	 * {@link EntityType}, used like {@code settings.allowSpawning(Blocks::never)}.
	 */
	private static Boolean never(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
		return false;
	}

	/**
	 * A shortcut to always return {@code true} in a typed context predicate with an
	 * {@link EntityType}, used like {@code settings.allowSpawning(Blocks::always)}.
	 */
	private static Boolean always(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
		return true;
	}

	/**
	 * A shortcut to always return {@code true} a context predicate, used as
	 * {@code settings.solidBlock(Blocks::always)}.
	 */
	private static boolean always(BlockState state, BlockView world, BlockPos pos) {
		return true;
	}

	/**
	 * A shortcut to always return {@code false} a context predicate, used as
	 * {@code settings.solidBlock(Blocks::never)}.
	 */
	private static boolean never(BlockState state, BlockView world, BlockPos pos) {
		return false;
	}
}
