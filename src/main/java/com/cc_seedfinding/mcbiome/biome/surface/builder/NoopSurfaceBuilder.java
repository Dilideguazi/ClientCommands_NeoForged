package com.cc_seedfinding.mcbiome.biome.surface.builder;

import com.cc_seedfinding.mcbiome.biome.Biome;
import com.cc_seedfinding.mcbiome.biome.surface.SurfaceConfig;
import com.cc_seedfinding.mcbiome.source.BiomeSource;
import com.cc_seedfinding.mccore.block.Block;
import com.cc_seedfinding.mccore.rand.ChunkRand;

public class NoopSurfaceBuilder extends SurfaceBuilder {
	public NoopSurfaceBuilder(SurfaceConfig surfaceConfig) {
		super(surfaceConfig);
	}
	@Override
	public Block[] applyToColumn(BiomeSource source, ChunkRand rand, Block[] column, Biome biome, int x, int z, int maxY, int minY, double noise, int seaLevel, Block defaultBlock, Block defaultFluid) {
		return null;
	}
}
