package dev.xpple.clientarguments.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ColumnPos;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CColumnPosArgumentType implements ArgumentType<CPosArgument> {

	private static final Collection<String> EXAMPLES = Arrays.asList("0 0", "~ ~", "~1 ~-2", "^ ^", "^-1 ^0");
	public static final SimpleCommandExceptionType INCOMPLETE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.pos2d.incomplete"));

	public static CColumnPosArgumentType columnPos() {
		return new CColumnPosArgumentType();
	}

	public static ColumnPos getCColumnPos(final CommandContext<FabricClientCommandSource> context, final String name) {
		BlockPos blockPos = (context.getArgument(name, CPosArgument.class)).toAbsoluteBlockPos(context.getSource());
		return new ColumnPos(blockPos.getX(), blockPos.getZ());
	}

	@Override
	public CPosArgument parse(final StringReader stringReader) throws CommandSyntaxException {
		int cursor = stringReader.getCursor();
		if (stringReader.canRead()) {
			WorldCoordinate coordinateArgument = WorldCoordinate.parseInt(stringReader);
			if (stringReader.canRead() && stringReader.peek() == ' ') {
				stringReader.skip();
				WorldCoordinate coordinateArgument2 = WorldCoordinate.parseInt(stringReader);
				return new CDefaultPosArgument(coordinateArgument, new WorldCoordinate(true, 0.0D), coordinateArgument2);
			}
			stringReader.setCursor(cursor);
		}
		throw INCOMPLETE_EXCEPTION.createWithContext(stringReader);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
		if (!(context.getSource() instanceof SharedSuggestionProvider)) {
			return Suggestions.empty();
		} else {
			String string = builder.getRemaining();
			if (!string.isEmpty() && string.charAt(0) == '^') {
				Set<SharedSuggestionProvider.TextCoordinates> singleton = Collections.singleton(SharedSuggestionProvider.TextCoordinates.DEFAULT_LOCAL);
				return SharedSuggestionProvider.suggest2DCoordinates(string, singleton, builder, Commands.createValidator(this::parse));
			}
			Collection<SharedSuggestionProvider.TextCoordinates> blockPositionSuggestions = ((SharedSuggestionProvider) context.getSource()).getRelevantCoordinates();
			return SharedSuggestionProvider.suggest2DCoordinates(string, blockPositionSuggestions, builder, Commands.createValidator(this::parse));
		}
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
