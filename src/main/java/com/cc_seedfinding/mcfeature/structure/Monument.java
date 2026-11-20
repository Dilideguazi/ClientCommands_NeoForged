package com.cc_seedfinding.mcfeature.structure;

import com.cc_seedfinding.mcbiome.biome.Biome;
import com.cc_seedfinding.mcbiome.biome.Biomes;
import com.cc_seedfinding.mcbiome.source.BiomeSource;
import com.cc_seedfinding.mccore.state.Dimension;
import com.cc_seedfinding.mccore.version.MCVersion;
import com.cc_seedfinding.mccore.version.VersionMap;

public class Monument extends TriangularStructure<Monument> {

	public static final VersionMap<Config> CONFIGS = new VersionMap<Config>()
		.add(MCVersion.v1_8, new Config(32, 5, 10387313));

	public Monument(MCVersion version) {
		this(CONFIGS.getAsOf(version), version);
	}

	public Monument(Config config, MCVersion version) {
		super(config, version);
	}

	public static String name() {
		return "monument";
	}

	@Override
	public boolean canSpawn(int chunkX, int chunkZ, BiomeSource source) {
		if(!super.canSpawn(chunkX, chunkZ, source)) return false;

		if(!source.iterateUniqueBiomes((chunkX << 4) + 9, (chunkZ << 4) + 9, 16, this::isValidBiome)) {
			return false;
		}

		return source.iterateUniqueBiomes((chunkX << 4) + 9, (chunkZ << 4) + 9, 29, this::isOceanOrRiver);
	}

	public boolean isOceanOrRiver(Biome biome) {
		return biome.getCategory() == Biome.Category.OCEAN || biome.getCategory() == Biome.Category.RIVER;
	}

	@Override
	public Dimension getValidDimension() {
		return Dimension.OVERWORLD;
	}

	@Override
	public boolean isValidBiome(Biome biome) {
		return biome == Biomes.DEEP_COLD_OCEAN || biome == Biomes.DEEP_FROZEN_OCEAN || biome == Biomes.DEEP_LUKEWARM_OCEAN
			|| biome == Biomes.DEEP_OCEAN || biome == Biomes.DEEP_WARM_OCEAN;
	}

}
