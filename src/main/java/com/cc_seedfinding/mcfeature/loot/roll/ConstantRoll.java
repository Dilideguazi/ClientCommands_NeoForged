package com.cc_seedfinding.mcfeature.loot.roll;

import com.cc_seedfinding.mcfeature.loot.LootContext;

public class ConstantRoll extends LootRoll {

	public final int value;

	public ConstantRoll(int value) {
		this.value = value;
	}

	@Override
	public int getCount(LootContext context) {
		return this.value;
	}

}
