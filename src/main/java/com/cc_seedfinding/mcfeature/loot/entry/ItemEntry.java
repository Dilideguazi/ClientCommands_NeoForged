package com.cc_seedfinding.mcfeature.loot.entry;

import com.cc_seedfinding.mcfeature.loot.LootContext;
import com.cc_seedfinding.mcfeature.loot.function.LootFunction;
import com.cc_seedfinding.mcfeature.loot.item.Item;
import com.cc_seedfinding.mcfeature.loot.item.ItemStack;

import java.util.function.Consumer;

public class ItemEntry extends LootEntry {

	public final Item item;

	public ItemEntry(Item item) {
		this(item, 1);
	}

	public ItemEntry(Item item, int weight) {
		super(weight);
		this.item = item;
	}

	public void generate(LootContext context, Consumer<ItemStack> stackConsumer) {
		stackConsumer = LootFunction.stack(stackConsumer, this.combinedLootFunction, context);
		stackConsumer.accept(new ItemStack(new Item(this.item.getName())));
	}
}
