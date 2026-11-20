package com.cc_seedfinding.mcfeature.decorator.ore.nether;

import com.cc_seedfinding.mcbiome.biome.Biomes;
import com.cc_seedfinding.mccore.block.Blocks;
import com.cc_seedfinding.mccore.version.MCVersion;
import com.cc_seedfinding.mccore.version.VersionMap;
import com.cc_seedfinding.mcfeature.decorator.ore.HeightProvider;
import com.cc_seedfinding.mcfeature.decorator.ore.ScatterOreDecorator;

public class LargeDebrisOre extends ScatterOreDecorator<ScatterOreDecorator.Config, ScatterOreDecorator.Data<LargeDebrisOre>> {

	public static final VersionMap<Config> CONFIGS = new VersionMap<Config>()
		.add(MCVersion.v1_16, new Config(15, 7, 3, 1, HeightProvider.depthAverage(16, 8), Blocks.ANCIENT_DEBRIS, BASE_STONE_NETHER)
			.add(12, 7, Biomes.CRIMSON_FOREST)
			.add(13, 7, Biomes.WARPED_FOREST))
		.add(MCVersion.v1_17, new Config(15, 7, 3, 1, HeightProvider.triangleRange(8, 24), Blocks.ANCIENT_DEBRIS, BASE_STONE_NETHER)
			.add(12, 7, Biomes.CRIMSON_FOREST)
			.add(13, 7, Biomes.WARPED_FOREST));

	public LargeDebrisOre(MCVersion version) {
		super(CONFIGS.getAsOf(version), version);
	}

	@Override
	public String getName() {
		return name();
	}

	public static String name() {
		return "large_debris_ore";
	}

}
