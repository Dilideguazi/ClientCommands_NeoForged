package com.cc_seedfinding.mcfeature.structure.generator.piece.stronghold;

import com.cc_seedfinding.mcseed.rand.JRand;

public class Start extends SpiralStaircase {

	public PieceWeight pieceWeight;
	public PortalRoom portalRoom;

	public Start(JRand rand, int x, int z) {
		super(0, rand, x, z);
	}

}
