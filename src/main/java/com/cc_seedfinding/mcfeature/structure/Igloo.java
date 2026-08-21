package com.cc_seedfinding.mcfeature.structure;

import com.cc_seedfinding.mcbiome.biome.Biome;
import com.cc_seedfinding.mcbiome.biome.Biomes;
import com.cc_seedfinding.mccore.rand.ChunkRand;
import com.cc_seedfinding.mccore.state.Dimension;
import com.cc_seedfinding.mccore.util.block.BlockRotation;
import com.cc_seedfinding.mccore.util.pos.CPos;
import com.cc_seedfinding.mccore.version.MCVersion;
import com.cc_seedfinding.mccore.version.VersionMap;

public class Igloo extends OldStructure<Igloo> {

	public static final VersionMap<Config> CONFIGS = new VersionMap<Config>()
		.add(MCVersion.v1_9, new Config(14357617))
		.add(MCVersion.v1_13, new Config(14357618));

	public Igloo(MCVersion version) {
		this(CONFIGS.getAsOf(version), version);
	}

	public Igloo(RegionStructure.Config config, MCVersion version) {
		super(config, version);
	}

	public static String name() {
		return "igloo";
	}

	public boolean hasBasement(long structureSeed, CPos cPos, ChunkRand rand) {

		if(getVersion().isNewerOrEqualTo(MCVersion.v1_9) && getVersion().isOlderThan(MCVersion.v1_14)) {
			rand.setPopulationSeed(structureSeed, cPos.getX(), cPos.getZ(), this.getVersion());
			// TODO figure how many calls here (ffs)
			BlockRotation rotation = BlockRotation.getRandom(rand);
			return rand.nextDouble() < 0.5D;
		}
		if(getVersion().isNewerOrEqualTo(MCVersion.v1_14)) {
			rand.setCarverSeed(structureSeed, cPos.getX(), cPos.getZ(), this.getVersion());
			BlockRotation rotation = BlockRotation.getRandom(rand);
			return rand.nextDouble() < 0.5D;
		}
		return false;
	}

	@Override
	public Dimension getValidDimension() {
		return Dimension.OVERWORLD;
	}

	@Override
	public boolean isValidBiome(Biome biome) {
		return biome == Biomes.SNOWY_TAIGA || biome == Biomes.SNOWY_TUNDRA;
	}

}
