package com.cc_seedfinding.mcfeature.structure;

import com.cc_seedfinding.mcbiome.biome.Biome;
import com.cc_seedfinding.mccore.state.Dimension;
import com.cc_seedfinding.mccore.version.MCVersion;
import com.cc_seedfinding.mccore.version.VersionMap;

public class OceanRuin extends UniformStructure<OceanRuin> {

	public static final VersionMap<Config> CONFIGS = new VersionMap<Config>()
		.add(MCVersion.v1_13, new Config(16, 8, 14357621))
		.add(MCVersion.v1_16, new Config(20, 8, 14357621));

	public OceanRuin(MCVersion version) {
		this(CONFIGS.getAsOf(version), version);
	}

	public OceanRuin(Config config, MCVersion version) {
		super(config, version);
	}

	public static String name() {
		return "ocean_ruin";
	}


	@Override
	public Dimension getValidDimension() {
		return Dimension.OVERWORLD;
	}

	@Override
	public boolean isValidBiome(Biome biome) {
		return biome.getCategory() == Biome.Category.OCEAN;
	}

}
