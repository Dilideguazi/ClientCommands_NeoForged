package com.cc_seedfinding.mcbiome.layer.land;

import com.cc_seedfinding.mcbiome.biome.Biome;
import com.cc_seedfinding.mcbiome.biome.Biomes;
import com.cc_seedfinding.mcbiome.layer.IntBiomeLayer;
import com.cc_seedfinding.mcbiome.layer.composite.XCrossLayer;
import com.cc_seedfinding.mccore.version.MCVersion;

public class MushroomLayer extends XCrossLayer {

	public MushroomLayer(MCVersion version, long worldSeed, long salt, IntBiomeLayer parent) {
		super(version, worldSeed, salt, parent);
	}

	@Override
	public int sample(int sw, int se, int ne, int nw, int center) {
		return Biome.applyAll(v -> Biome.isShallowOcean(v, this.getVersion()), center, sw, se, ne, nw) &&
			this.nextInt(100) == 0 ? Biomes.MUSHROOM_FIELDS.getId() : center;
	}

}
