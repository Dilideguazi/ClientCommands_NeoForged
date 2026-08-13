package dev.xpple.clientarguments.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CScoreboardCriterionArgumentType implements ArgumentType<ObjectiveCriteria> {

	private static final Collection<String> EXAMPLES = Arrays.asList("trigger", "playerKillCount", "food");
	public static final DynamicCommandExceptionType INVALID_CRITERION_EXCEPTION = new DynamicCommandExceptionType(name -> Component.translatable("argument.criteria.invalid", name));

	public static CScoreboardCriterionArgumentType scoreboardCriterion() {
		return new CScoreboardCriterionArgumentType();
	}

	public static ObjectiveCriteria getCCriterion(final CommandContext<FabricClientCommandSource> context, final String name) {
		return context.getArgument(name, ObjectiveCriteria.class);
	}

	@Override
	public ObjectiveCriteria parse(final StringReader stringReader) throws CommandSyntaxException {
		int cursor = stringReader.getCursor();

		while(stringReader.canRead() && stringReader.peek() != ' ') {
			stringReader.skip();
		}

		String string = stringReader.getString().substring(cursor, stringReader.getCursor());
		return ObjectiveCriteria.byName(string).orElseThrow(() -> {
			stringReader.setCursor(cursor);
			return INVALID_CRITERION_EXCEPTION.create(string);
		});
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggest(ObjectiveCriteria.getCustomCriteriaNames(), builder);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
