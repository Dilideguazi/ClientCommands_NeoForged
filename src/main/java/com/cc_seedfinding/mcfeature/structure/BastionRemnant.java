package com.cc_seedfinding.mcfeature.structure;

import com.cc_seedfinding.mcbiome.biome.Biome;
import com.cc_seedfinding.mcbiome.biome.Biomes;
import com.cc_seedfinding.mccore.rand.ChunkRand;
import com.cc_seedfinding.mccore.state.Dimension;
import com.cc_seedfinding.mccore.util.pos.CPos;
import com.cc_seedfinding.mccore.version.MCVersion;
import com.cc_seedfinding.mccore.version.VersionMap;

public class BastionRemnant extends UniformStructure<BastionRemnant> {

	public static final VersionMap<Config> CONFIGS = new VersionMap<Config>()
		.add(MCVersion.v1_16, new Config(30, 4, 30084232))
		.add(MCVersion.v1_16_1, new Config(27, 4, 30084232));

	public BastionRemnant(MCVersion version) {
		this(CONFIGS.getAsOf(version), version);
	}

	public BastionRemnant(Config config, MCVersion version) {
		super(config, version);
	}

	public static String name() {
		return "bastion_remnant";
	}

	@Override
	public boolean canStart(Data<BastionRemnant> data, long structureSeed, ChunkRand rand) {
		if(!super.canStart(data, structureSeed, rand)) return false;
		return rand.nextInt(5) >= 2;
	}

	@Override
	public CPos getInRegion(long structureSeed, int regionX, int regionZ, ChunkRand rand) {
		CPos bastion = super.getInRegion(structureSeed, regionX, regionZ, rand);
		return rand.nextInt(5) >= 2 ? bastion : null;
	}

	@Override
	public Dimension getValidDimension() {
		return Dimension.NETHER;
	}

	@Override
	public boolean isValidBiome(Biome biome) {
		return biome == Biomes.NETHER_WASTES || biome == Biomes.SOUL_SAND_VALLEY || biome == Biomes.WARPED_FOREST
			|| biome == Biomes.CRIMSON_FOREST;
	}

}
