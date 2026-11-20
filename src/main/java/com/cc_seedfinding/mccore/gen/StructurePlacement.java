package com.cc_seedfinding.mccore.gen;

import com.cc_seedfinding.mccore.block.Tile;
import com.cc_seedfinding.mccore.util.block.BlockBox;
import com.cc_seedfinding.mccore.util.block.BlockMirror;
import com.cc_seedfinding.mccore.util.block.BlockRotation;
import com.cc_seedfinding.mccore.util.pos.BPos;

public class StructurePlacement {

	public BlockBox box;
	public BlockMirror mirror;
	public BlockRotation rotation;
	public BPos pivot;

	public StructurePlacement() {
		this.box = BlockBox.empty();
		this.mirror = BlockMirror.NONE;
		this.rotation = BlockRotation.NONE;
		this.pivot = BPos.ORIGIN;
	}

	public Tile transform(Tile tile) {
		return this.transformAndSet(tile.copy());
	}

	public Tile transformAndSet(Tile tile) {
		tile.setPos(tile.getPos().transform(mirror, rotation, pivot));
		return tile;
	}

}
