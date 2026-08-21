package com.cc_seedfinding.mcfeature.loot.function;

import com.cc_seedfinding.mcfeature.loot.LootContext;
import com.cc_seedfinding.mcfeature.loot.item.ItemStack;
import com.cc_seedfinding.mcfeature.loot.roll.BinomialRoll;
import com.cc_seedfinding.mcfeature.loot.roll.ConstantRoll;
import com.cc_seedfinding.mcfeature.loot.roll.LootRoll;
import com.cc_seedfinding.mcfeature.loot.roll.UniformRoll;

public class SetCountFunction implements LootFunction {
	private final LootRoll roll;

	public SetCountFunction(LootRoll roll) {
		this.roll = roll;
	}

	public static SetCountFunction constant(int value) {
		return new SetCountFunction(new ConstantRoll(value));
	}

	public static SetCountFunction uniform(float min, float max) {
		return new SetCountFunction(new UniformRoll(min, max));
	}

	public static SetCountFunction binomial(int trials, float probability) {
		return new SetCountFunction(new BinomialRoll(trials, probability));
	}

	@Override
	public ItemStack process(ItemStack baseStack, LootContext context) {
		baseStack.setCount(this.roll.getCount(context));
		return baseStack;
	}

}
