package com.cc_seedfinding.mcfeature.structure;

import com.cc_seedfinding.mcbiome.biome.Biome;
import com.cc_seedfinding.mcbiome.biome.Biomes;
import com.cc_seedfinding.mccore.state.Dimension;
import com.cc_seedfinding.mccore.version.MCVersion;
import com.cc_seedfinding.mccore.version.VersionMap;

public class JunglePyramid extends OldStructure<JunglePyramid> {

	public static final VersionMap<Config> CONFIGS = new VersionMap<Config>()
		.add(MCVersion.v1_8, new Config(14357617))
		.add(MCVersion.v1_13, new Config(14357619));

	public JunglePyramid(MCVersion version) {
		this(CONFIGS.getAsOf(version), version);
	}

	public JunglePyramid(RegionStructure.Config config, MCVersion version) {
		super(config, version);
	}

	public static String name() {
		return "jungle_pyramid";
	}

	@Override
	public Dimension getValidDimension() {
		return Dimension.OVERWORLD;
	}

	@Override
	public boolean isValidBiome(Biome biome) {
		return biome == Biomes.JUNGLE || biome == Biomes.JUNGLE_HILLS || biome == Biomes.BAMBOO_JUNGLE
			|| biome == Biomes.BAMBOO_JUNGLE_HILLS;
	}

}
