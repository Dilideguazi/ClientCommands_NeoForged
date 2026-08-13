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
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class CRegistryKeyArgumentType<T> implements ArgumentType<ResourceKey<T>> {

    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    private static final DynamicCommandExceptionType INVALID_FEATURE_EXCEPTION = new DynamicCommandExceptionType(id -> Component.translatable("commands.place.feature.invalid", id));
    private static final DynamicCommandExceptionType INVALID_STRUCTURE_EXCEPTION = new DynamicCommandExceptionType(id -> Component.translatable("commands.place.structure.invalid", id));
    private static final DynamicCommandExceptionType INVALID_JIGSAW_EXCEPTION = new DynamicCommandExceptionType(id -> Component.translatable("commands.place.jigsaw.invalid", id));

    final ResourceKey<? extends Registry<T>> registryRef;

    public CRegistryKeyArgumentType(ResourceKey<? extends Registry<T>> registryRef) {
        this.registryRef = registryRef;
    }

    public static <T> CRegistryKeyArgumentType<T> registryKey(ResourceKey<? extends Registry<T>> registryRef) {
        return new CRegistryKeyArgumentType<>(registryRef);
    }

    private static <T> ResourceKey<T> getKey(CommandContext<FabricClientCommandSource> context, String name, ResourceKey<Registry<T>> registryRef, DynamicCommandExceptionType invalidException) throws CommandSyntaxException {
        ResourceKey<?> registryKey = context.getArgument(name, ResourceKey.class);
        Optional<ResourceKey<T>> optional = registryKey.cast(registryRef);
        return optional.orElseThrow(() -> invalidException.create(registryKey));
    }

    private static <T> Registry<T> getRegistry(CommandContext<FabricClientCommandSource> context, ResourceKey<? extends Registry<T>> registryRef) {
        return context.getSource().registryAccess().registryOrThrow(registryRef);
    }

    private static <T> Holder.Reference<T> getRegistryEntry(CommandContext<FabricClientCommandSource> context, String name, ResourceKey<Registry<T>> registryRef, DynamicCommandExceptionType invalidException) throws CommandSyntaxException {
        ResourceKey<T> registryKey = getKey(context, name, registryRef, invalidException);
        return getRegistry(context, registryRef).getHolder(registryKey).orElseThrow(() -> invalidException.create(registryKey.location()));
    }

    public static Holder.Reference<ConfiguredFeature<?, ?>> getCConfiguredFeatureEntry(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
        return getRegistryEntry(context, name, Registries.CONFIGURED_FEATURE, INVALID_FEATURE_EXCEPTION);
    }

    public static Holder.Reference<Structure> getCStructureEntry(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
        return getRegistryEntry(context, name, Registries.STRUCTURE, INVALID_STRUCTURE_EXCEPTION);
    }

    public static Holder.Reference<StructureTemplatePool> getCStructurePoolEntry(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
        return getRegistryEntry(context, name, Registries.TEMPLATE_POOL, INVALID_JIGSAW_EXCEPTION);
    }

    @Override
    public ResourceKey<T> parse(final StringReader stringReader) throws CommandSyntaxException {
        ResourceLocation identifier = ResourceLocation.read(stringReader);
        return ResourceKey.create(this.registryRef, identifier);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        S s = context.getSource();
        if (s instanceof SharedSuggestionProvider commandSource) {
            return commandSource.suggestRegistryElements(this.registryRef, SharedSuggestionProvider.ElementSuggestionType.ELEMENTS, builder, context);
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
