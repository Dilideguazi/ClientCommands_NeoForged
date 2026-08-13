package dev.xpple.clientarguments.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class CRegistryEntryPredicateArgumentType<T> implements ArgumentType<CRegistryEntryPredicateArgumentType.EntryPredicate<T>> {

    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");

    private static final Dynamic2CommandExceptionType NOT_FOUND_EXCEPTION = new Dynamic2CommandExceptionType((tag, type) -> Component.translatable("argument.resource_tag.not_found", tag, type));
    private static final Dynamic3CommandExceptionType WRONG_TYPE_EXCEPTION = new Dynamic3CommandExceptionType((tag, type, expectedType) -> Component.translatable("argument.resource_tag.invalid_type", tag, type, expectedType));

    private final HolderLookup<T> registryWrapper;
    final ResourceKey<? extends Registry<T>> registryRef;

    public CRegistryEntryPredicateArgumentType(CommandBuildContext registryAccess, ResourceKey<? extends Registry<T>> registryRef) {
        this.registryRef = registryRef;
        this.registryWrapper = registryAccess.holderLookup(registryRef);
    }

    public static <T> CRegistryEntryPredicateArgumentType<T> registryEntryPredicate(CommandBuildContext registryRef, ResourceKey<? extends Registry<T>> registryAccess) {
        return new CRegistryEntryPredicateArgumentType<>(registryRef, registryAccess);
    }

    public static <T> EntryPredicate<T> getRegistryEntryPredicate(final CommandContext<FabricClientCommandSource> context, final String name, ResourceKey<Registry<T>> registryRef) throws CommandSyntaxException {
        EntryPredicate<?> entryPredicate = context.getArgument(name, EntryPredicate.class);
        Optional<EntryPredicate<T>> optional = entryPredicate.tryCast(registryRef);
        return optional.orElseThrow(() -> entryPredicate.getEntry().map(entry -> {
            ResourceKey<?> registryKey = entry.key();
            return ResourceArgument.ERROR_INVALID_RESOURCE_TYPE.create(registryKey.location(), registryKey.registry(), registryRef.location());
        }, entryList -> {
            TagKey<?> tagKey = entryList.key();
            return WRONG_TYPE_EXCEPTION.create(tagKey.location(), tagKey.registry(), registryRef.location());
        }));
    }

    @Override
    public EntryPredicate<T> parse(final StringReader stringReader) throws CommandSyntaxException {
        if (stringReader.canRead() && stringReader.peek() == '#') {
            int cursor = stringReader.getCursor();

            try {
                stringReader.skip();
                ResourceLocation identifier = ResourceLocation.read(stringReader);
                TagKey<T> tagKey = TagKey.create(this.registryRef, identifier);
                HolderSet.Named<T> named = this.registryWrapper.get(tagKey).orElseThrow(() -> NOT_FOUND_EXCEPTION.create(identifier, this.registryRef.location()));
                return new TagBased<>(named);
            } catch (CommandSyntaxException e) {
                stringReader.setCursor(cursor);
                throw e;
            }
        } else {
            ResourceLocation identifier = ResourceLocation.read(stringReader);
            ResourceKey<T> registryKey = ResourceKey.create(this.registryRef, identifier);
            Holder.Reference<T> reference = this.registryWrapper.get(registryKey).orElseThrow(() -> ResourceArgument.ERROR_UNKNOWN_RESOURCE.create(identifier, this.registryRef.location()));
            return new EntryBased<>(reference);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        SharedSuggestionProvider.suggestResource(this.registryWrapper.listTagIds().map(TagKey::location), builder, "#");
        return SharedSuggestionProvider.suggestResource(this.registryWrapper.listElementIds().map(ResourceKey::location), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public interface EntryPredicate<T> extends Predicate<Holder<T>> {
        Either<Holder.Reference<T>, HolderSet.Named<T>> getEntry();

        <E> Optional<EntryPredicate<E>> tryCast(ResourceKey<? extends Registry<E>> registryRef);

        String asString();
    }

    private record TagBased<T>(HolderSet.Named<T> tag) implements EntryPredicate<T> {

        @Override
        public Either<Holder.Reference<T>, HolderSet.Named<T>> getEntry() {
            return Either.right(this.tag);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <E> Optional<EntryPredicate<E>> tryCast(ResourceKey<? extends Registry<E>> registryRef) {
            return this.tag.key().isFor(registryRef) ? Optional.of((EntryPredicate<E>) this) : Optional.empty();
        }

        @Override
        public boolean test(Holder<T> registryEntry) {
            return this.tag.contains(registryEntry);
        }

        @Override
        public String asString() {
            return "#" + this.tag.key().location();
        }
    }

    private record EntryBased<T>(Holder.Reference<T> value) implements EntryPredicate<T> {

        @Override
        public Either<Holder.Reference<T>, HolderSet.Named<T>> getEntry() {
            return Either.left(this.value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <E> Optional<EntryPredicate<E>> tryCast(ResourceKey<? extends Registry<E>> registryRef) {
            return this.value.key().isFor(registryRef) ? Optional.of((EntryPredicate<E>) this) : Optional.empty();
        }

        @Override
        public boolean test(Holder<T> registryEntry) {
            return registryEntry.equals(this.value);
        }

        @Override
        public String asString() {
            return this.value.key().location().toString();
        }
    }
}
