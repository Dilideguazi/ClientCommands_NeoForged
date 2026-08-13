package dev.xpple.clientarguments.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CRegistryEntryArgumentType<T> implements ArgumentType<Holder.Reference<T>> {

    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");

    private static final DynamicCommandExceptionType NOT_SUMMONABLE_EXCEPTION = new DynamicCommandExceptionType(id -> Component.translatable("entity.not_summonable", id));
    public static final Dynamic2CommandExceptionType NOT_FOUND_EXCEPTION = new Dynamic2CommandExceptionType((element, type) -> Component.translatable("argument.resource.not_found", element, type));
    public static final Dynamic3CommandExceptionType INVALID_TYPE_EXCEPTION = new Dynamic3CommandExceptionType((element, type, expectedType) -> Component.translatable("argument.resource.invalid_type", element, type, expectedType));

    final ResourceKey<? extends Registry<T>> registryRef;
    private final HolderLookup<T> registryWrapper;

    public CRegistryEntryArgumentType(CommandBuildContext arg, ResourceKey<? extends Registry<T>> arg2) {
        this.registryRef = arg2;
        this.registryWrapper = arg.holderLookup(arg2);
    }

    public static <T> CRegistryEntryArgumentType<T> registryEntry(CommandBuildContext registryAccess, ResourceKey<? extends Registry<T>> registryRef) {
        return new CRegistryEntryArgumentType<>(registryAccess, registryRef);
    }

    @SuppressWarnings("unchecked")
    public static <T> Holder.Reference<T> getCRegistryEntry(CommandContext<FabricClientCommandSource> context, String name, ResourceKey<Registry<T>> registryRef) throws CommandSyntaxException {
        Holder.Reference<T> reference = (Holder.Reference<T>) context.getArgument(name, Holder.Reference.class);
        ResourceKey<?> registryKey = reference.key();
        if (registryKey.isFor(registryRef)) {
            return reference;
        }
        throw INVALID_TYPE_EXCEPTION.create(registryKey.location(), registryKey.registry(), registryRef.location());
    }

    public static Holder.Reference<Attribute> getCEntityAttribute(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
        return getCRegistryEntry(context, name, Registries.ATTRIBUTE);
    }

    public static Holder.Reference<ConfiguredFeature<?, ?>> getCConfiguredFeature(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
        return getCRegistryEntry(context, name, Registries.CONFIGURED_FEATURE);
    }

    public static Holder.Reference<Structure> getCStructure(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
        return getCRegistryEntry(context, name, Registries.STRUCTURE);
    }

    public static Holder.Reference<EntityType<?>> getCEntityType(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
        return getCRegistryEntry(context, name, Registries.ENTITY_TYPE);
    }

    public static Holder.Reference<EntityType<?>> getCSummonableEntityType(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
        Holder.Reference<EntityType<?>> lv = getCRegistryEntry(context, name, Registries.ENTITY_TYPE);
        if (lv.value().canSummon()) {
            return lv;
        }
        throw NOT_SUMMONABLE_EXCEPTION.create(lv.key().location().toString());
    }

    public static Holder.Reference<MobEffect> getCStatusEffect(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
        return getCRegistryEntry(context, name, Registries.MOB_EFFECT);
    }

    public static Holder.Reference<Enchantment> getCEnchantment(CommandContext<FabricClientCommandSource> context, String name) throws CommandSyntaxException {
        return getCRegistryEntry(context, name, Registries.ENCHANTMENT);
    }

    @Override
    public Holder.Reference<T> parse(final StringReader stringReader) throws CommandSyntaxException {
        ResourceLocation identifier = ResourceLocation.read(stringReader);
        ResourceKey<T> registryKey = ResourceKey.create(this.registryRef, identifier);
        return this.registryWrapper.get(registryKey).orElseThrow(() -> NOT_FOUND_EXCEPTION.create(identifier, this.registryRef.location()));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> commandContext, final SuggestionsBuilder suggestionsBuilder) {
        return SharedSuggestionProvider.suggestResource(this.registryWrapper.listElementIds().map(ResourceKey::location), suggestionsBuilder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
