package com.cc_seedfinding.mcfeature.structure;

import com.cc_seedfinding.mcbiome.biome.Biome;
import com.cc_seedfinding.mcbiome.biome.Biomes;
import com.cc_seedfinding.mcbiome.source.BiomeSource;
import com.cc_seedfinding.mccore.state.Dimension;
import com.cc_seedfinding.mccore.version.MCVersion;
import com.cc_seedfinding.mccore.version.VersionMap;

public class Mansion extends TriangularStructure<Mansion> {

	public static final VersionMap<Config> CONFIGS = new VersionMap<Config>()
		.add(MCVersion.v1_11, new Config(80, 20, 10387319));

	public Mansion(MCVersion version) {
		this(CONFIGS.getAsOf(version), version);
	}

	public Mansion(Config config, MCVersion version) {
		super(config, version);
	}

	public static String name() {
		return "mansion";
	}

	@Override
	public boolean canSpawn(int chunkX, int chunkZ, BiomeSource source) {
		if(!super.canSpawn(chunkX, chunkZ, source)) return false;
		return source.iterateUniqueBiomes((chunkX << 4) + 9, (chunkZ << 4) + 9, 32, this::isValidBiome);
	}

	@Override
	public Dimension getValidDimension() {
		return Dimension.OVERWORLD;
	}

	@Override
	public boolean isValidBiome(Biome biome) {
		return biome == Biomes.DARK_FOREST || biome == Biomes.DARK_FOREST_HILLS;
	}

}
