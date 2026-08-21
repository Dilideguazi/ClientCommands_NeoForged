package com.cc_seedfinding.mcfeature.structure;


import com.cc_seedfinding.mcbiome.biome.Biome;
import com.cc_seedfinding.mcbiome.biome.Biomes;
import com.cc_seedfinding.mccore.rand.ChunkRand;
import com.cc_seedfinding.mccore.state.Dimension;
import com.cc_seedfinding.mccore.version.MCVersion;
import com.cc_seedfinding.mccore.version.VersionMap;
import com.cc_seedfinding.mcfeature.loot.ILoot;
import com.cc_seedfinding.mcfeature.structure.generator.Generator;
import com.cc_seedfinding.mcfeature.structure.generator.structure.ShipwreckGenerator;

public class Shipwreck extends UniformStructure<Shipwreck> implements ILoot {
	public static final VersionMap<Config> CONFIGS = new VersionMap<Config>()
		.add(MCVersion.v1_13, new Config(15, 8, 165745295))
		.add(MCVersion.v1_13_1, new Config(16, 8, 165745295))
		.add(MCVersion.v1_16, new Config(24, 4, 165745295));


	public Shipwreck(MCVersion version) {
		this(CONFIGS.getAsOf(version), version);
	}

	public Shipwreck(Config config, MCVersion version) {
		super(config, version);
	}

	public static String name() {
		return "shipwreck";
	}

	@Override
	public boolean canStart(Data<Shipwreck> data, long structureSeed, ChunkRand rand) {
		return super.canStart(data, structureSeed, rand);
	}

	@Override
	public Dimension getValidDimension() {
		return Dimension.OVERWORLD;
	}

	@Override
	public boolean isValidBiome(Biome biome) {
		return biome.getCategory() == Biome.Category.OCEAN || biome == Biomes.BEACH || biome == Biomes.SNOWY_BEACH;
	}

	@Override
	public int getDecorationSalt() {
		return this.getVersion().isNewerOrEqualTo(MCVersion.v1_16) ? 40006 : 30005;
	}

	@Override
	public boolean isCorrectGenerator(Generator generator) {
		return generator instanceof ShipwreckGenerator;
	}

	@Override
	public SpecificCalls getSpecificCalls() {
		return (generator, rand) -> {
			if(isCorrectGenerator(generator)) {
				if(((ShipwreckGenerator)generator).isBeached()) {
					rand.nextInt(3);
				}
			}
		};
	}
}
