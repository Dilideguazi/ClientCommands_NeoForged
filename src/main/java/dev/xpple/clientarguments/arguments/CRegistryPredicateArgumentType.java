package dev.xpple.clientarguments.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class CRegistryPredicateArgumentType<T> implements ArgumentType<CRegistryPredicateArgumentType.RegistryPredicate<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
    final ResourceKey<? extends Registry<T>> registryRef;

    public CRegistryPredicateArgumentType(ResourceKey<? extends Registry<T>> registryRef) {
        this.registryRef = registryRef;
    }

    public static <T> CRegistryPredicateArgumentType<T> registryPredicate(ResourceKey<? extends Registry<T>> registryRef) {
        return new CRegistryPredicateArgumentType<>(registryRef);
    }

    public static <T> RegistryPredicate<T> getCPredicate(final CommandContext<FabricClientCommandSource> context, final String name, final ResourceKey<Registry<T>> registryRef, final DynamicCommandExceptionType invalidException) throws CommandSyntaxException {
        RegistryPredicate<?> registryPredicate = context.getArgument(name, RegistryPredicate.class);
        Optional<RegistryPredicate<T>> optional = registryPredicate.tryCast(registryRef);
        return optional.orElseThrow(() -> invalidException.create(registryPredicate));
    }

    @Override
    public RegistryPredicate<T> parse(final StringReader stringReader) throws CommandSyntaxException {
        if (stringReader.canRead() && stringReader.peek() == '#') {
            int i = stringReader.getCursor();

            try {
                stringReader.skip();
                ResourceLocation identifier = ResourceLocation.read(stringReader);
                return new TagBased<>(TagKey.create(this.registryRef, identifier));
            } catch (CommandSyntaxException var4) {
                stringReader.setCursor(i);
                throw var4;
            }
        } else {
            ResourceLocation identifier2 = ResourceLocation.read(stringReader);
            return new RegistryKeyBased<>(ResourceKey.create(this.registryRef, identifier2));
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        S s = context.getSource();
        if (s instanceof SharedSuggestionProvider commandSource) {
            return commandSource.suggestRegistryElements(this.registryRef, SharedSuggestionProvider.ElementSuggestionType.ALL, builder, context);
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public interface RegistryPredicate<T> extends Predicate<Holder<T>> {
        Either<ResourceKey<T>, TagKey<T>> getKey();

        <E> Optional<RegistryPredicate<E>> tryCast(ResourceKey<? extends Registry<E>> registryRef);

        String asString();
    }

    record TagBased<T>(TagKey<T> key) implements RegistryPredicate<T> {

        public Either<ResourceKey<T>, TagKey<T>> getKey() {
            return Either.right(this.key);
        }

        public <E> Optional<RegistryPredicate<E>> tryCast(ResourceKey<? extends Registry<E>> registryRef) {
            return this.key.cast(registryRef).map(TagBased::new);
        }

        public boolean test(Holder<T> registryEntry) {
            return registryEntry.is(this.key);
        }

        public String asString() {
            return "#" + this.key.location();
        }
    }

    record RegistryKeyBased<T>(ResourceKey<T> key) implements RegistryPredicate<T> {

        public Either<ResourceKey<T>, TagKey<T>> getKey() {
            return Either.left(this.key);
        }

        public <E> Optional<RegistryPredicate<E>> tryCast(ResourceKey<? extends Registry<E>> registryRef) {
            return this.key.cast(registryRef).map(RegistryKeyBased::new);
        }

        public boolean test(Holder<T> registryEntry) {
            return registryEntry.is(this.key);
        }

        public String asString() {
            return this.key.location().toString();
        }
    }
}
