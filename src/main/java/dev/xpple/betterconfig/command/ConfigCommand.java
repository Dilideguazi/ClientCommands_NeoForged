/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  net.minecraft.command.CommandRegistryAccess
 *  net.minecraft.server.command.CommandSourceStack
 *  net.minecraft.text.Text
 */
package dev.xpple.betterconfig.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xpple.betterconfig.impl.ModConfigImpl;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class ConfigCommand
extends AbstractConfigCommand<CommandSourceStack> {
    private ConfigCommand() {
        super("config");
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(new ConfigCommand().create(registryAccess).requires(source -> source.hasPermission(4)));
    }

    @Override
    protected int comment(CommandSourceStack source, String config, String comment) {
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.comment", "Comment for %s:", config), false);
        source.sendSuccess(() -> Component.nullToEmpty(comment), false);
        return 1;
    }

    @Override
    protected int get(CommandSourceStack source, ModConfigImpl modConfig, String config) {
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.get", "%s is currently set to %s.", config, modConfig.asString(config)), false);
        return 1;
    }

    @Override
    protected int reset(CommandSourceStack source, ModConfigImpl modConfig, String config) {
        modConfig.reset(config);
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.reset", "%s has been reset to %s.", config, modConfig.asString(config)), true);
        return 1;
    }

    @Override
    protected int set(CommandSourceStack source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.set(config, value);
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.set", "%s has been set to %s.", config, modConfig.asString(config)), true);
        return 1;
    }

    @Override
    protected int add(CommandSourceStack source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.add(config, value);
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.add", "%s has been added to %s.", modConfig.asString(value), config), true);
        return 1;
    }

    @Override
    protected int put(CommandSourceStack source, ModConfigImpl modConfig, String config, Object key, Object value) throws CommandSyntaxException {
        modConfig.put(config, key, value);
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.put", "The mapping %s=%s has been added to %s.", modConfig.asString(key), modConfig.asString(value), config), true);
        return 1;
    }

    @Override
    protected int remove(CommandSourceStack source, ModConfigImpl modConfig, String config, Object value) throws CommandSyntaxException {
        modConfig.remove(config, value);
        source.sendSuccess(() -> Component.translatableWithFallback("betterconfig.commands.config.remove", "%s has been removed from %s.", modConfig.asString(value), config), true);
        return 1;
    }
}

