package com.cc_seedfinding.mcbiome.layer.water;

import com.cc_seedfinding.mcbiome.biome.Biome;
import com.cc_seedfinding.mcbiome.biome.Biomes;
import com.cc_seedfinding.mcbiome.layer.IntBiomeLayer;
import com.cc_seedfinding.mcbiome.layer.composite.CrossLayer;
import com.cc_seedfinding.mccore.version.MCVersion;

public class NoiseToRiverLayer extends CrossLayer {

	public NoiseToRiverLayer(MCVersion version, long worldSeed, long salt, IntBiomeLayer parent) {
		super(version, worldSeed, salt, parent);
	}

	@Override
	public int sample(int n, int e, int s, int w, int center) {
		if(this.getVersion().isOlderOrEqualTo(MCVersion.v1_6_4)) {
			return center != 0 && Biome.applyAll(v -> center == v, w, n, e, s) ? -1 : Biomes.RIVER.getId();
		}
		int validCenter = isValidForRiver(center);
		return Biome.applyAll(v -> validCenter == isValidForRiver(v), w, n, e, s) ? -1 : Biomes.RIVER.getId();
	}

	private static int isValidForRiver(int value) {
		return value >= 2 ? 2 + (value & 1) : value;
	}

}
