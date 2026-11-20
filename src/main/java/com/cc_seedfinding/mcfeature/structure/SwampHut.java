package com.cc_seedfinding.mcfeature.structure;

import com.cc_seedfinding.mcbiome.biome.Biome;
import com.cc_seedfinding.mcbiome.biome.Biomes;
import com.cc_seedfinding.mccore.state.Dimension;
import com.cc_seedfinding.mccore.version.MCVersion;
import com.cc_seedfinding.mccore.version.VersionMap;

public class SwampHut extends OldStructure<SwampHut> {

	public static final VersionMap<Config> CONFIGS = new VersionMap<Config>()
		.add(MCVersion.v1_8, new Config(14357617))
		.add(MCVersion.v1_13, new Config(14357620));

	public SwampHut(MCVersion version) {
		this(CONFIGS.getAsOf(version), version);
	}

	public SwampHut(RegionStructure.Config config, MCVersion version) {
		super(config, version);
	}

	public static String name() {
		return "swamp_hut";
	}

	@Override
	public Dimension getValidDimension() {
		return Dimension.OVERWORLD;
	}

	@Override
	public boolean isValidBiome(Biome biome) {
		return biome == Biomes.SWAMP;
	}

}
