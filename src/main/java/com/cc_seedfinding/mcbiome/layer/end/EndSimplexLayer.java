package com.cc_seedfinding.mcbiome.layer.end;

import com.cc_seedfinding.mcbiome.layer.BoolBiomeLayer;
import com.cc_seedfinding.mccore.version.MCVersion;
import com.cc_seedfinding.mcnoise.simplex.SimplexNoiseSampler;
import com.cc_seedfinding.mcseed.lcg.LCG;
import com.cc_seedfinding.mcseed.rand.JRand;

public class EndSimplexLayer extends BoolBiomeLayer {

	public static final LCG SIMPLEX_SKIP = LCG.JAVA.combine(17292);
	protected final SimplexNoiseSampler simplex;

	public EndSimplexLayer(MCVersion version, long worldSeed) {
		super(version);
		JRand rand = new JRand(worldSeed);
		rand.advance(SIMPLEX_SKIP);
		this.simplex = new SimplexNoiseSampler(rand);
	}

	@Override
	public boolean sample(int x, int y, int z) {
		return this.simplex.sample2D(x, z) < (double)-0.9F;
	}

}
