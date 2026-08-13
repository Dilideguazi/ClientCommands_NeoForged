/*
 * Decompiled with CFR 0.152.
 */
package dev.xpple.betterconfig.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.xpple.betterconfig.api.Config;
import dev.xpple.betterconfig.command.suggestion.EnumSuggestionProvider;
import dev.xpple.betterconfig.impl.BetterConfigImpl;
import dev.xpple.betterconfig.impl.ModConfigImpl;
import dev.xpple.betterconfig.util.CheckedBiFunction;
import dev.xpple.betterconfig.util.CheckedFunction;
import dev.xpple.betterconfig.util.Pair;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AbstractConfigCommand<S extends SharedSuggestionProvider> {
    private static final DynamicCommandExceptionType INVALID_ENUM_EXCEPTION = new DynamicCommandExceptionType(value -> Component.translatable("argument.enum.invalid", value));
    private final String rootLiteral;

    protected AbstractConfigCommand(String rootLiteral) {
        this.rootLiteral = rootLiteral;
    }

    protected LiteralArgumentBuilder<S> create(CommandBuildContext registryAccess) {
        LiteralArgumentBuilder root = LiteralArgumentBuilder.literal(this.rootLiteral);
        for (ModConfigImpl modConfig : BetterConfigImpl.getModConfigs().values()) {
            HashMap<String, LiteralArgumentBuilder> literals = new HashMap<>();
            for (String config2 : modConfig.getConfigs().keySet()) {
                Predicate<SharedSuggestionProvider> condition = modConfig.getConditions().get(config2);
                LiteralArgumentBuilder configLiteral = LiteralArgumentBuilder.<SharedSuggestionProvider>literal(config2).requires(condition);
                literals.put(config2, configLiteral);
                configLiteral.then(LiteralArgumentBuilder.literal("get").executes(ctx -> this.get((S) ctx.getSource(), modConfig, config2)));
                configLiteral.then(LiteralArgumentBuilder.literal("reset").executes(ctx -> this.reset((S) ctx.getSource(), modConfig, config2)));
            }
            modConfig.getComments().forEach((config, comment) -> (literals.get(config)).then(LiteralArgumentBuilder.literal("comment").executes(ctx -> this.comment((S)(ctx.getSource()), config, comment))));
            modConfig.getSetters().keySet().forEach(config -> {
                Config annotation = modConfig.getAnnotations().get(config);
                Config.Setter setter = annotation.setter();
                Class<?> type = setter.type() == Config.EMPTY.class ? modConfig.getType(config) : setter.type();
                Function<CommandBuildContext, ArgumentType<?>> argumentFunction = (Function<CommandBuildContext, ArgumentType<?>>) modConfig.getArgument(type);
                Pair<SuggestionProvider<? extends SharedSuggestionProvider>, CheckedBiFunction<CommandContext<? extends SharedSuggestionProvider>, String, ?, CommandSyntaxException>> suggestorPair = modConfig.getSuggestor(type);
                if (argumentFunction != null) {
                    RequiredArgumentBuilder subCommand = RequiredArgumentBuilder.argument("value", argumentFunction.apply(registryAccess));
                    subCommand.executes(ctx -> this.set((S)(ctx.getSource()), modConfig, config, ctx.getArgument("value", type)));
                    (literals.get(config)).then(LiteralArgumentBuilder.literal("set").then(subCommand));
                } else if (suggestorPair != null) {
                    RequiredArgumentBuilder subCommand = RequiredArgumentBuilder.argument("value", (ArgumentType)StringArgumentType.greedyString());
                    subCommand.suggests(suggestorPair.left()).executes(ctx -> {
                        try {
                            return this.set((S)(ctx.getSource()), modConfig, config, suggestorPair.right().apply(ctx, "value"));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    (literals.get(config)).then(LiteralArgumentBuilder.literal("set").then(subCommand));
                } else if (type.isEnum()) {
                    RequiredArgumentBuilder subCommand = RequiredArgumentBuilder.argument("value", (ArgumentType)StringArgumentType.string()).suggests(new EnumSuggestionProvider(type));
                    subCommand.executes(ctx -> {
                        String value = StringArgumentType.getString(ctx, "value");
                        return this.set((S)(ctx.getSource()), modConfig, config, Arrays.stream(type.getEnumConstants()).filter(c -> ((Enum)c).name().equals(value)).findAny().orElseThrow(() -> INVALID_ENUM_EXCEPTION.create((Object)value)));
                    });
                    (literals.get(config)).then(LiteralArgumentBuilder.literal("set").then(subCommand));
                }
            });
            modConfig.getAdders().keySet().forEach(config -> {
                Config annotation = modConfig.getAnnotations().get(config);
                Config.Adder adder = annotation.adder();
                Class type = adder.type() == Config.EMPTY.class ? (Class)modConfig.getParameterTypes(config)[0] : adder.type();
                Function<CommandBuildContext, ArgumentType<?>> argumentFunction = (Function<CommandBuildContext, ArgumentType<?>>) modConfig.getArgument(type);
                Pair<SuggestionProvider<? extends SharedSuggestionProvider>, CheckedBiFunction<CommandContext<? extends SharedSuggestionProvider>, String, ?, CommandSyntaxException>> suggestorPair = modConfig.getSuggestor(type);
                if (argumentFunction != null) {
                    RequiredArgumentBuilder subCommand = RequiredArgumentBuilder.argument("value", argumentFunction.apply(registryAccess));
                    subCommand.executes(ctx -> this.add((S)(ctx.getSource()), modConfig, config, ctx.getArgument("value", type)));
                    (literals.get(config)).then(LiteralArgumentBuilder.literal("add").then(subCommand));
                } else if (suggestorPair != null) {
                    RequiredArgumentBuilder subCommand = RequiredArgumentBuilder.argument("value", (ArgumentType)StringArgumentType.greedyString());
                    subCommand.suggests(suggestorPair.left()).executes(ctx -> {
                        try {
                            return this.add((S)(ctx.getSource()), modConfig, config, suggestorPair.right().apply(ctx, "value"));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    (literals.get(config)).then(LiteralArgumentBuilder.literal("add").then(subCommand));
                } else if (type.isEnum()) {
                    RequiredArgumentBuilder subCommand = RequiredArgumentBuilder.argument("value", (ArgumentType)StringArgumentType.string()).suggests(new EnumSuggestionProvider(type));
                    subCommand.executes(ctx -> {
                        String value = StringArgumentType.getString(ctx, "value");
                        return this.add((S)(ctx.getSource()), modConfig, config, Arrays.stream(type.getEnumConstants()).filter(c -> ((Enum)c).name().equals(value)).findAny().orElseThrow(() -> INVALID_ENUM_EXCEPTION.create((Object)value)));
                    });
                    (literals.get(config)).then(LiteralArgumentBuilder.literal("add").then(subCommand));
                }
            });
            modConfig.getPutters().keySet().forEach(config -> {
                CheckedFunction<CommandContext<S>, ?, CommandSyntaxException> getKey;
                RequiredArgumentBuilder subCommand;
                Config annotation = modConfig.getAnnotations().get(config);
                Config.Putter putter = annotation.putter();
                Type[] types = modConfig.getParameterTypes(config);
                Class<?> keyType = putter.keyType() == Config.EMPTY.class ? (Class)types[0] : putter.keyType();
                Function<CommandBuildContext, ArgumentType<?>> keyArgumentFunction = (Function<CommandBuildContext, ArgumentType<?>>) modConfig.getArgument(keyType);
                Pair<SuggestionProvider<? extends SharedSuggestionProvider>, CheckedBiFunction<CommandContext<? extends SharedSuggestionProvider>, String, ?, CommandSyntaxException>> keySuggestorPair = modConfig.getSuggestor(keyType);
                if (keyArgumentFunction != null) {
                    subCommand = RequiredArgumentBuilder.argument("key", keyArgumentFunction.apply(registryAccess));
                    getKey = ctx -> ctx.getArgument("key", keyType);
                } else if (keySuggestorPair != null) {
                    subCommand = RequiredArgumentBuilder.argument("key", (ArgumentType)StringArgumentType.string());
                    subCommand.suggests(keySuggestorPair.left());
                    getKey = ctx -> {
                        try {
                            return ((CheckedBiFunction)keySuggestorPair.right()).apply(ctx, "key");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    };
                } else if (keyType.isEnum()) {
                    subCommand = RequiredArgumentBuilder.argument("key", (ArgumentType)StringArgumentType.string()).suggests(new EnumSuggestionProvider(keyType));
                    getKey = ctx -> {
                        String value = StringArgumentType.getString(ctx, "key");
                        return Arrays.stream(keyType.getEnumConstants()).filter(c -> ((Enum)c).name().equals(value)).findAny().orElseThrow(() -> INVALID_ENUM_EXCEPTION.create((Object)value));
                    };
                } else {
                    return;
                }
                Class valueType = putter.valueType() == Config.EMPTY.class ? (Class)types[1] : putter.valueType();
                Function<CommandBuildContext, ArgumentType<?>> valueArgumentFunction = (Function<CommandBuildContext, ArgumentType<?>>) modConfig.getArgument(valueType);
                Pair<SuggestionProvider<? extends SharedSuggestionProvider>, CheckedBiFunction<CommandContext<? extends SharedSuggestionProvider>, String, ?, CommandSyntaxException>> valueSuggestorPair = modConfig.getSuggestor(valueType);
                if (valueArgumentFunction != null) {
                    RequiredArgumentBuilder subSubCommand = RequiredArgumentBuilder.argument("value", valueArgumentFunction.apply(registryAccess));
                    subSubCommand.executes(ctx -> {
                        try {
                            return this.put((S)(ctx.getSource()), modConfig, config, getKey.apply(ctx), ctx.getArgument("value", valueType));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    (literals.get(config)).then(LiteralArgumentBuilder.literal("put").then(subCommand.then(subSubCommand)));
                } else if (valueSuggestorPair != null) {
                    RequiredArgumentBuilder subSubCommand = RequiredArgumentBuilder.argument("value", (ArgumentType)StringArgumentType.greedyString());
                    subSubCommand.suggests(valueSuggestorPair.left()).executes(ctx -> {
                        try {
                            return this.put((S)(ctx.getSource()), modConfig, config, getKey.apply(ctx), valueSuggestorPair.right().apply(ctx, "value"));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    (literals.get(config)).then(LiteralArgumentBuilder.literal("put").then(subCommand.then(subSubCommand)));
                } else if (valueType.isEnum()) {
                    RequiredArgumentBuilder subSubCommand = RequiredArgumentBuilder.argument("value", (ArgumentType)StringArgumentType.string()).suggests(new EnumSuggestionProvider(valueType));
                    subCommand.executes(ctx -> {
                        String value = StringArgumentType.getString(ctx, "value");
                        try {
                            return this.put((S)(ctx.getSource()), modConfig, config, getKey.apply(ctx), Arrays.stream(valueType.getEnumConstants()).filter(c -> ((Enum)c).name().equals(value)).findAny().orElseThrow(() -> INVALID_ENUM_EXCEPTION.create((Object)value)));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    (literals.get(config)).then(LiteralArgumentBuilder.literal("put").then(subCommand.then(subSubCommand)));
                }
            });
            modConfig.getRemovers().keySet().forEach(config -> {
                Config annotation = modConfig.getAnnotations().get(config);
                Config.Remover remover = annotation.remover();
                Class type = remover.type() == Config.EMPTY.class ? (Class)modConfig.getParameterTypes(config)[0] : remover.type();
                Function<CommandBuildContext, ArgumentType<?>> argumentFunction = (Function<CommandBuildContext, ArgumentType<?>>) modConfig.getArgument(type);
                Pair<SuggestionProvider<? extends SharedSuggestionProvider>, CheckedBiFunction<CommandContext<? extends SharedSuggestionProvider>, String, ?, CommandSyntaxException>> suggestorPair = modConfig.getSuggestor(type);
                if (argumentFunction != null) {
                    RequiredArgumentBuilder subCommand = RequiredArgumentBuilder.argument("value", argumentFunction.apply(registryAccess));
                    subCommand.executes(ctx -> this.remove((S)(ctx.getSource()), modConfig, config, ctx.getArgument("value", type)));
                    (literals.get(config)).then(LiteralArgumentBuilder.literal("remove").then(subCommand));
                } else if (suggestorPair != null) {
                    RequiredArgumentBuilder subCommand = RequiredArgumentBuilder.argument("value", (ArgumentType)StringArgumentType.greedyString());
                    subCommand.suggests(suggestorPair.left()).executes(ctx -> this.remove((S)(ctx.getSource()), modConfig, config, suggestorPair.right().apply(ctx, "value")));
                    (literals.get(config)).then(LiteralArgumentBuilder.literal("remove").then(subCommand));
                } else if (type.isEnum()) {
                    RequiredArgumentBuilder subCommand = RequiredArgumentBuilder.argument("value", (ArgumentType)StringArgumentType.string()).suggests(new EnumSuggestionProvider(type));
                    subCommand.executes(ctx -> {
                        String value = StringArgumentType.getString(ctx, "value");
                        return this.remove((S)(ctx.getSource()), modConfig, config, Arrays.stream(type.getEnumConstants()).filter(c -> ((Enum)c).name().equals(value)).findAny().orElseThrow(() -> INVALID_ENUM_EXCEPTION.create((Object)value)));
                    });
                    literals.get(config).then(LiteralArgumentBuilder.literal("remove").then(subCommand));
                }
            });
            literals.values().forEach(literal -> root.then(LiteralArgumentBuilder.literal(modConfig.getModId()).then(literal)));
        }
        return root;
    }

    protected abstract int comment(S var1, String var2, String var3);

    protected abstract int get(S var1, ModConfigImpl var2, String var3);

    protected abstract int reset(S var1, ModConfigImpl var2, String var3);

    protected abstract int set(S var1, ModConfigImpl var2, String var3, Object var4) throws CommandSyntaxException;

    protected abstract int add(S var1, ModConfigImpl var2, String var3, Object var4) throws CommandSyntaxException;

    protected abstract int put(S var1, ModConfigImpl var2, String var3, Object var4, Object var5) throws CommandSyntaxException;

    protected abstract int remove(S var1, ModConfigImpl var2, String var3, Object var4) throws CommandSyntaxException;
}

