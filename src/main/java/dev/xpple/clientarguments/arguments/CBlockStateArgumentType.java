package dev.xpple.clientarguments.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CBlockStateArgumentType implements ArgumentType<ClientBlockArgument> {

	private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "foo{bar=baz}");

	private final HolderLookup<Block> registryWrapper;

	protected CBlockStateArgumentType(CommandBuildContext registryAccess) {
		this.registryWrapper = registryAccess.holderLookup(Registries.BLOCK);
	}

	public static CBlockStateArgumentType blockState(CommandBuildContext registryAccess) {
		return new CBlockStateArgumentType(registryAccess);
	}

	public static ClientBlockArgument getCBlockState(final CommandContext<FabricClientCommandSource> context, final String name) {
		return context.getArgument(name, ClientBlockArgument.class);
	}

	@Override
	public ClientBlockArgument parse(final StringReader stringReader) throws CommandSyntaxException {
		var result = BlockStateParser.parseForBlock(this.registryWrapper, stringReader, true);
		return new ClientBlockArgument(result);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
		return BlockStateParser.fillSuggestions(this.registryWrapper, builder, false, true);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
