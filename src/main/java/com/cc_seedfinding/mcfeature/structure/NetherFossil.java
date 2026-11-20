package com.cc_seedfinding.mcfeature.structure;

import com.cc_seedfinding.mcbiome.biome.Biome;
import com.cc_seedfinding.mcbiome.biome.Biomes;
import com.cc_seedfinding.mccore.block.Block;
import com.cc_seedfinding.mccore.block.Blocks;
import com.cc_seedfinding.mccore.rand.ChunkRand;
import com.cc_seedfinding.mccore.state.Dimension;
import com.cc_seedfinding.mccore.version.MCVersion;
import com.cc_seedfinding.mccore.version.VersionMap;
import com.cc_seedfinding.mcterrain.TerrainGenerator;

public class NetherFossil extends UniformStructure<NetherFossil> {

	public static final VersionMap<Config> CONFIGS = new VersionMap<Config>()
		.add(MCVersion.v1_16, new Config(2, 1, 14357921));

	public NetherFossil(MCVersion version) {
		this(CONFIGS.getAsOf(version), version);
	}

	public NetherFossil(Config config, MCVersion version) {
		super(config, version);
	}

	public static String name() {
		return "nether_fossil";
	}

	@Override
	public Dimension getValidDimension() {
		return Dimension.NETHER;
	}

	@Override
	public boolean isValidBiome(Biome biome) {
		return biome == Biomes.SOUL_SAND_VALLEY;
	}

	@Override
	public boolean isValidTerrain(TerrainGenerator generator, int chunkX, int chunkZ) {
		if(generator == null) return true;
		ChunkRand rand = new ChunkRand();
		rand.setCarverSeed(generator.getWorldSeed(), chunkX, chunkZ, this.getVersion());
		int x = (chunkX << 4) + rand.nextInt(16);
		int z = (chunkZ << 4) + rand.nextInt(16);
		int seaLevel = generator.getSeaLevel();
		int y = seaLevel + rand.nextInt(generator.getWorldHeight() - 2 - seaLevel);
		Block[] column = generator.getColumnAt(x, z);
		for(; y > seaLevel; --y) {
			Block block = column[y];
			Block blockDown = column[y - 1];
			if(block == Blocks.AIR && blockDown == Blocks.NETHERRACK) {
				break;
			}
		}
		return y > seaLevel;
	}
}
