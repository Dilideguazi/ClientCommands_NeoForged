package dev.xpple.clientarguments.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class CItemPredicateArgumentType implements ArgumentType<Predicate<ItemStack>> {
	private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "#stick", "#stick{foo=bar}");

	private final HolderLookup<Item> registryWrapper;

	public CItemPredicateArgumentType(CommandBuildContext registryAccess) {
		this.registryWrapper = registryAccess.holderLookup(Registries.ITEM);
	}

	public static CItemPredicateArgumentType itemPredicate(CommandBuildContext registryAccess) {
		return new CItemPredicateArgumentType(registryAccess);
	}

	@Override
	public Predicate<ItemStack> parse(final StringReader stringReader) throws CommandSyntaxException {
		return ItemParser.parseForTesting(registryWrapper, stringReader).map(
				itemResult -> new ItemPredicate(itemResult.item().value(), itemResult.nbt()),
				tagResult -> new TagPredicate(tagResult.tag(), tagResult.nbt())
		);
	}

	@SuppressWarnings("unchecked")
	public static Predicate<ItemStack> getCItemPredicate(CommandContext<FabricClientCommandSource> context, String name) {
		return context.getArgument(name, Predicate.class);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
		return ItemParser.fillSuggestions(registryWrapper, builder, true);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	private static class ItemPredicate implements Predicate<ItemStack> {
		private final Item item;
		@Nullable
		private final CompoundTag nbt;

		public ItemPredicate(Item item, @Nullable CompoundTag nbt) {
			this.item = item;
			this.nbt = nbt;
		}

		@Override
		public boolean test(ItemStack itemStack) {
			return itemStack.is(this.item) && NbtUtils.compareNbt(this.nbt, itemStack.getTag(), true);
		}
	}

	private static class TagPredicate implements Predicate<ItemStack> {
		private final HolderSet<Item> tag;
		@Nullable
		private final CompoundTag compound;

		public TagPredicate(HolderSet<Item> tag, @Nullable CompoundTag nbt) {
			this.tag = tag;
			this.compound = nbt;
		}

		@Override
		public boolean test(ItemStack itemStack) {
			return this.tag.contains(itemStack.getItemHolder()) && NbtUtils.compareNbt(this.compound, itemStack.getTag(), true);
		}
	}
}
