package com.cc_seedfinding.mcfeature.structure;

import com.cc_seedfinding.mcbiome.biome.Biome;
import com.cc_seedfinding.mcbiome.biome.Biomes;
import com.cc_seedfinding.mccore.rand.ChunkRand;
import com.cc_seedfinding.mccore.state.Dimension;
import com.cc_seedfinding.mccore.util.block.BlockRotation;
import com.cc_seedfinding.mccore.util.pos.CPos;
import com.cc_seedfinding.mccore.version.MCVersion;
import com.cc_seedfinding.mccore.version.VersionMap;

public class Village extends OldStructure<Village> {

	public static final VersionMap<Config> CONFIGS = new VersionMap<Config>()
		.add(MCVersion.v1_8, new Config(10387312));

	public Village(MCVersion version) {
		this(CONFIGS.getAsOf(version), version);
	}

	public Village(RegionStructure.Config config, MCVersion version) {
		super(config, version);
	}

	public static String name() {
		return "village";
	}


	public boolean isZombieVillage(long structureSeed, CPos cPos, ChunkRand rand) {
		rand.setCarverSeed(structureSeed, cPos.getX(), cPos.getZ(), this.getVersion());
		// burn a call (nextInt)
		rand.advance(1);
		if(getVersion().isNewerOrEqualTo(MCVersion.v1_10) && getVersion().isOlderThan(MCVersion.v1_14)) {
			int size = 0; // this might change via settings;
			rand.getInt(2 + size, 4 + size * 2);
			rand.getInt(0 + size, 1 + size);
			rand.getInt(0 + size, 2 + size);
			rand.getInt(2 + size, 5 + size * 3);
			rand.getInt(0 + size, 2 + size);
			rand.getInt(1 + size, 4 + size);
			rand.getInt(2 + size, 4 + size * 2);
			rand.getInt(0, 1 + size);
			rand.getInt(0 + size, 3 + size * 2);
			BlockRotation rotation = BlockRotation.getRandom(rand);
			return rand.nextInt(50) == 0;
		}
		if(getVersion().isNewerOrEqualTo(MCVersion.v1_14) && this.biome != null) {
			// we trust for now cubiomes but we will later move that to villageGenerator
			if(Biomes.PLAINS.equals(this.biome)) {
				return rand.nextInt(204) >= 200;
			} else if(Biomes.DESERT.equals(this.biome)) {
				return rand.nextInt(250) >= 245;
			} else if(Biomes.SAVANNA.equals(this.biome)) {
				return rand.nextInt(459) >= 450;
			} else if(Biomes.TAIGA.equals(this.biome)) {
				return rand.nextInt(100) >= 98;
			} else if(Biomes.SNOWY_TUNDRA.equals(this.biome)) {
				return rand.nextInt(306) >= 300;
			}
		}
		return false;

	}

	@Override
	public Dimension getValidDimension() {
		return Dimension.OVERWORLD;
	}

	@Override
	public boolean isValidBiome(Biome biome) {
		if(biome == Biomes.PLAINS || biome == Biomes.DESERT || biome == Biomes.SAVANNA) return true;
		if(biome == Biomes.TAIGA && this.getVersion().isNewerOrEqualTo(MCVersion.v1_10)) return true;
		return biome == Biomes.SNOWY_TUNDRA && this.getVersion().isNewerOrEqualTo(MCVersion.v1_14);
	}

}
