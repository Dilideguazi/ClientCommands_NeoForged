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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CDimensionArgumentType implements ArgumentType<ResourceLocation> {

	private static final Collection<String> EXAMPLES = Arrays.stream(DimensionArgument.values()).map(DimensionArgument::getName).collect(Collectors.toSet());
	private static final DynamicCommandExceptionType INVALID_DIMENSION_EXCEPTION = new DynamicCommandExceptionType(id -> Component.translatable("argument.dimension.invalid", id));

	public static CDimensionArgumentType dimension() {
		return new CDimensionArgumentType();
	}

	public static DimensionArgument getCDimensionArgument(final CommandContext<FabricClientCommandSource> context, final String name) throws CommandSyntaxException {
		ResourceLocation identifier = context.getArgument(name, ResourceLocation.class);
		ResourceKey<Level> registryKey = ResourceKey.create(Registries.DIMENSION, identifier);
		return Arrays.stream(DimensionArgument.values()).filter(dimension -> dimension.registryKey.equals(registryKey)).findAny().orElseThrow(() -> INVALID_DIMENSION_EXCEPTION.create(identifier));
	}

	@Override
	public ResourceLocation parse(final StringReader stringReader) throws CommandSyntaxException {
		return ResourceLocation.read(stringReader);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggestResource(Arrays.stream(DimensionArgument.values()).map(dimension -> dimension.registryKey.location()), builder);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public enum DimensionArgument {
		OVERWORLD("overworld", Level.OVERWORLD),
		NETHER("the_nether", Level.NETHER),
		END("the_end", Level.END);

		private final String name;
		private final ResourceKey<Level> registryKey;

		DimensionArgument(String name, ResourceKey<Level> registryKey) {
			this.name = name;
			this.registryKey = registryKey;
		}

		public String getName() {
			return this.name;
		}

		public ResourceKey<Level> getRegistryKey() {
			return this.registryKey;
		}
	}
}
