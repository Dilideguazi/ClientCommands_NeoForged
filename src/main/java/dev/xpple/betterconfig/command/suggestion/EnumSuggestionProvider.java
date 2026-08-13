/*
 * Decompiled with CFR 0.152.
 */
package dev.xpple.betterconfig.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class EnumSuggestionProvider<T extends Enum<T>>
implements SuggestionProvider<SharedSuggestionProvider> {
    private final Class<T> enumClass;

    public EnumSuggestionProvider(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    public CompletableFuture<Suggestions> getSuggestions(CommandContext<SharedSuggestionProvider> context, SuggestionsBuilder builder) {
        //noinspection rawtypes
        return SharedSuggestionProvider.suggest(Arrays.stream((Enum[])this.enumClass.getEnumConstants()).map(Enum::name), builder);
    }
}

