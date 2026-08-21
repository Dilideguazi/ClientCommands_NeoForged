package com.cc_seedfinding.mcbiome.layer.land;

import com.cc_seedfinding.mcbiome.biome.Biome;
import com.cc_seedfinding.mcbiome.biome.Biomes;
import com.cc_seedfinding.mcbiome.layer.IntBiomeLayer;
import com.cc_seedfinding.mcbiome.layer.composite.CrossLayer;
import com.cc_seedfinding.mccore.version.MCVersion;

public class IslandLayer extends CrossLayer {

	public IslandLayer(MCVersion version, long worldSeed, long salt, IntBiomeLayer parent) {
		super(version, worldSeed, salt, parent);
	}

	@Override
	public int sample(int n, int e, int s, int w, int center) {
		return Biome.applyAll(v -> Biome.isShallowOcean(v, this.getVersion()), center, n, e, s, w)
			&& this.nextInt(2) == 0 ? Biomes.PLAINS.getId() : center;
	}

}
