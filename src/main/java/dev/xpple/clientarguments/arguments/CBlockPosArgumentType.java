package dev.xpple.clientarguments.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class CBlockPosArgumentType implements ArgumentType<CPosArgument> {

	private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "~ ~ ~", "^ ^ ^", "^1 ^ ^-5", "~0.5 ~1 ~-5");
	public static final SimpleCommandExceptionType UNLOADED_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.pos.unloaded"));
	public static final SimpleCommandExceptionType OUT_OF_WORLD_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.pos.outofworld"));
	public static final SimpleCommandExceptionType OUT_OF_BOUNDS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.pos.outofbounds"));

	public static CBlockPosArgumentType blockPos() {
		return new CBlockPosArgumentType();
	}

	public static BlockPos getCLoadedBlockPos(final CommandContext<FabricClientCommandSource> context, final String name) throws CommandSyntaxException {
		ClientLevel clientWorld = context.getSource().getWorld();
		return getCLoadedBlockPos(context, clientWorld, name);
	}

	public static BlockPos getCLoadedBlockPos(final CommandContext<FabricClientCommandSource> context, final ClientLevel world, final String name) throws CommandSyntaxException {
		BlockPos blockPos = getCBlockPos(context, name);
		ChunkPos chunkPos = new ChunkPos(blockPos);
		if (!world.getChunkSource().hasChunk(chunkPos.x, chunkPos.z)) {
			throw UNLOADED_EXCEPTION.create();
		} else if (!world.isInWorldBounds(blockPos)) {
			throw OUT_OF_WORLD_EXCEPTION.create();
		} else {
			return blockPos;
		}
	}

	public static BlockPos getCBlockPos(final CommandContext<FabricClientCommandSource> context, final String name) {
		return context.getArgument(name, CPosArgument.class).toAbsoluteBlockPos(context.getSource());
	}

	public static BlockPos getCValidBlockPos(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
		BlockPos blockPos = getCBlockPos(context, name);
		if (!Level.isInSpawnableBounds(blockPos)) {
			throw OUT_OF_BOUNDS_EXCEPTION.create();
		}
		return blockPos;
	}

	@Override
	public CPosArgument parse(final StringReader stringReader) throws CommandSyntaxException {
		return stringReader.canRead() && stringReader.peek() == '^' ? CLookingPosArgument.parse(stringReader) : CDefaultPosArgument.parse(stringReader);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
		if (!(context.getSource() instanceof SharedSuggestionProvider)) {
			return Suggestions.empty();
		}
		String string = builder.getRemaining();
		Collection<SharedSuggestionProvider.TextCoordinates> collection;
		if (!string.isEmpty() && string.charAt(0) == '^') {
			collection = Collections.singleton(SharedSuggestionProvider.TextCoordinates.DEFAULT_LOCAL);
		} else {
			collection = ((SharedSuggestionProvider) context.getSource()).getRelevantCoordinates();
		}

		return SharedSuggestionProvider.suggestCoordinates(string, collection, builder, Commands.createValidator(this::parse));
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
