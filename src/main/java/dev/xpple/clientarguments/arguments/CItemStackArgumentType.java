package dev.xpple.clientarguments.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CItemStackArgumentType implements ArgumentType<ItemInput> {

	private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "stick{foo=bar}");

	private final HolderLookup<Item> registryWrapper;

	public CItemStackArgumentType(CommandBuildContext registryAccess) {
		this.registryWrapper = registryAccess.holderLookup(Registries.ITEM);
	}

	public static CItemStackArgumentType itemStack(CommandBuildContext registryAccess) {
		return new CItemStackArgumentType(registryAccess);
	}

	@Override
	public ItemInput parse(final StringReader stringReader) throws CommandSyntaxException {
		var result = ItemParser.parseForItem(this.registryWrapper, stringReader);
		return new ItemInput(result.item(), result.nbt());
	}

	public static <S> ItemInput getCItemStackArgument(CommandContext<S> context, String name) {
		return context.getArgument(name, ItemInput.class);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
		return ItemParser.fillSuggestions(this.registryWrapper, builder, false);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
