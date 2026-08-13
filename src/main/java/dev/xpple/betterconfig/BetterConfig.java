/*
 * Decompiled with CFR 0.152.
 */
package dev.xpple.betterconfig;

import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.betterconfig.command.ConfigCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public class BetterConfig {
//    public static final String MOD_ID = "betterconfig";
    public static final Path MOD_PATH = FMLPaths.CONFIGDIR.get();
    public static final Logger LOGGER = LogManager.getLogger("betterconfig");

    public static void onInitializeServer() {
        CommandRegistrationCallback.EVENT.register(BetterConfig::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        ConfigCommand.register(dispatcher, registryAccess);
    }
}

