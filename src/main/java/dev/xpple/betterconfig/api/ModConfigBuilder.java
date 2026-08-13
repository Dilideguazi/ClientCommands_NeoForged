package dev.xpple.betterconfig.api;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.xpple.betterconfig.impl.BetterConfigImpl;
import dev.xpple.betterconfig.impl.BetterConfigInternals;
import dev.xpple.betterconfig.impl.ModConfigImpl;
import dev.xpple.betterconfig.util.CheckedBiFunction;
import dev.xpple.betterconfig.util.Pair;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "rawtypes", "unchecked"})
public class ModConfigBuilder {
    final String modId;
    final Class<?> configsClass;
    final GsonBuilder builder = (new GsonBuilder()).serializeNulls().enableComplexMapKeySerialization();
    final Map<Class<?>, Function<CommandBuildContext, ? extends ArgumentType<?>>> arguments = new HashMap<>();
    final Map<Class<?>, Pair<SuggestionProvider<? extends SharedSuggestionProvider>, CheckedBiFunction<CommandContext<? extends SharedSuggestionProvider>, String, ?, CommandSyntaxException>>> suggestors = new HashMap();

    public ModConfigBuilder(String modId, Class<?> configsClass) {
        this.modId = modId;
        this.configsClass = configsClass;
    }

    public <T> ModConfigBuilder registerType(Class<T> type, TypeAdapter<T> adapter, Supplier<ArgumentType<T>> argumentTypeSupplier) {
        return this.registerType(type, adapter, (Function)((registryAccess) -> (ArgumentType)argumentTypeSupplier.get()));
    }

    public <T> ModConfigBuilder registerType(Class<T> type, TypeAdapter<T> adapter, Function<CommandBuildContext, ArgumentType<T>> argumentTypeFunction) {
        this.builder.registerTypeAdapter(type, adapter);
        this.arguments.put(type, argumentTypeFunction);
        return this;
    }

    public <T> ModConfigBuilder registerTypeHierarchy(Class<T> type, TypeAdapter<T> adapter, Supplier<ArgumentType<T>> argumentTypeSupplier) {
        return this.registerTypeHierarchy(type, adapter, (Function)((registryAccess) -> (ArgumentType)argumentTypeSupplier.get()));
    }

    public <T> ModConfigBuilder registerTypeHierarchy(Class<T> type, TypeAdapter<T> adapter, Function<CommandBuildContext, ArgumentType<T>> argumentTypeFunction) {
        this.builder.registerTypeHierarchyAdapter(type, adapter);
        this.arguments.put(type, argumentTypeFunction);
        return this;
    }

    public <T> ModConfigBuilder registerType(Class<T> type, TypeAdapter<T> adapter, SuggestionProvider<? extends SharedSuggestionProvider> suggestionProvider, CheckedBiFunction<CommandContext<? extends SharedSuggestionProvider>, String, T, CommandSyntaxException> argumentParser) {
        this.builder.registerTypeAdapter(type, adapter);
        this.suggestors.put(type, new Pair(suggestionProvider, argumentParser));
        return this;
    }

    public <T> ModConfigBuilder registerTypeHierarchy(Class<T> type, TypeAdapter<T> adapter, SuggestionProvider<? extends SharedSuggestionProvider> suggestionProvider, CheckedBiFunction<CommandContext<? extends SharedSuggestionProvider>, String, T, CommandSyntaxException> argumentParser) {
        this.builder.registerTypeHierarchyAdapter(type, adapter);
        this.suggestors.put(type, new Pair(suggestionProvider, argumentParser));
        return this;
    }

    public void build() {
        ModConfigImpl modConfig = new ModConfigImpl(this.modId, this.configsClass, this.builder.create(), this.arguments, this.suggestors);
        if (BetterConfigImpl.getModConfigs().putIfAbsent(this.modId, modConfig) == null) {
            BetterConfigInternals.init(modConfig);
        } else {
            throw new IllegalArgumentException(this.modId);
        }
    }
}
