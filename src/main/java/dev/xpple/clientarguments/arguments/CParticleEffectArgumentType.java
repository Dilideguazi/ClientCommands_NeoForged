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
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CParticleEffectArgumentType implements ArgumentType<ParticleOptions> {

	private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "particle with options");
	public static final DynamicCommandExceptionType UNKNOWN_PARTICLE_EXCEPTION = new DynamicCommandExceptionType(id -> Component.translatable("particle.notFound", id));

	public static CParticleEffectArgumentType particleEffect() {
		return new CParticleEffectArgumentType();
	}

	public static ParticleOptions getCParticle(final CommandContext<FabricClientCommandSource> context, final String name) {
		return context.getArgument(name, ParticleOptions.class);
	}

	@Override
	public ParticleOptions parse(final StringReader stringReader) throws CommandSyntaxException {
		return readParameters(stringReader);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggestResource(ForgeRegistries.PARTICLE_TYPES.getKeys(), builder);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public static ParticleOptions readParameters(StringReader reader) throws CommandSyntaxException {
		ResourceLocation identifier = ResourceLocation.read(reader);
		ParticleType<?> particleType = ForgeRegistries.PARTICLE_TYPES.getValue(identifier);
		if (particleType == null){
			throw UNKNOWN_PARTICLE_EXCEPTION.create(identifier);
		}
		return readParameters(reader, particleType);
	}

	private static <T extends ParticleOptions> T readParameters(StringReader reader, ParticleType<T> type) throws CommandSyntaxException {
		return type.getDeserializer().fromCommand(type, reader);
	}
}
