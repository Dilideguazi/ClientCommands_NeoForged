package com.cc_seedfinding.mcfeature.loot.entry;

import com.cc_seedfinding.mcfeature.loot.LootContext;
import com.cc_seedfinding.mcfeature.loot.item.ItemStack;

import java.util.function.Consumer;

public class EmptyEntry extends LootEntry {

	public EmptyEntry(int weight) {
		super(weight);
	}

	@Override
	public void generate(LootContext context, Consumer<ItemStack> stackConsumer) {

	}
}
