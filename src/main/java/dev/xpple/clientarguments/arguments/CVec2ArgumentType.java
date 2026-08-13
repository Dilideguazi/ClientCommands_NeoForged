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
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CVec2ArgumentType implements ArgumentType<CPosArgument> {

	private static final Collection<String> EXAMPLES = Arrays.asList("0 0", "~ ~", "0.1 -0.5", "~1 ~-2");
	public static final SimpleCommandExceptionType INCOMPLETE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.pos2d.incomplete"));
	private final boolean centerIntegers;

	public CVec2ArgumentType(boolean centerIntegers) {
		this.centerIntegers = centerIntegers;
	}

	public static CVec2ArgumentType vec2() {
		return vec2(true);
	}

	public static CVec2ArgumentType vec2(boolean centerIntegers) {
		return new CVec2ArgumentType(centerIntegers);
	}

	public static Vec2 getCVec2(CommandContext<FabricClientCommandSource> context, String name) {
		Vec3 vec3d = context.getArgument(name, CPosArgument.class).toAbsolutePos(context.getSource());
		return new Vec2((float)vec3d.x, (float)vec3d.z);
	}

	@Override
	public CPosArgument parse(final StringReader stringReader) throws CommandSyntaxException {
		int cursor = stringReader.getCursor();
		if (!stringReader.canRead()) {
			throw INCOMPLETE_EXCEPTION.createWithContext(stringReader);
		} else {
			WorldCoordinate coordinateArgument = WorldCoordinate.parseDouble(stringReader, this.centerIntegers);
			if (stringReader.canRead() && stringReader.peek() == ' ') {
				stringReader.skip();
				WorldCoordinate coordinateArgument2 = WorldCoordinate.parseDouble(stringReader, this.centerIntegers);
				return new CDefaultPosArgument(coordinateArgument, new WorldCoordinate(true, 0.0D), coordinateArgument2);
			} else {
				stringReader.setCursor(cursor);
				throw INCOMPLETE_EXCEPTION.createWithContext(stringReader);
			}
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
		if (!(context.getSource() instanceof SharedSuggestionProvider)) {
			return Suggestions.empty();
		} else {
			String string = builder.getRemaining();
			if (!string.isEmpty() && string.charAt(0) == '^') {
				final Set<SharedSuggestionProvider.TextCoordinates> singleton = Collections.singleton(SharedSuggestionProvider.TextCoordinates.DEFAULT_LOCAL);
				return SharedSuggestionProvider.suggest2DCoordinates(string, singleton, builder, Commands.createValidator(this::parse));
			} else {
				final Collection<SharedSuggestionProvider.TextCoordinates> positionSuggestions = ((SharedSuggestionProvider) context.getSource()).getAbsoluteCoordinates();
				return SharedSuggestionProvider.suggest2DCoordinates(string, positionSuggestions, builder, Commands.createValidator(this::parse));
			}
		}
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
