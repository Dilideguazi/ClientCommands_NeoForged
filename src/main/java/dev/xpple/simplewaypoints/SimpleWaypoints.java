package dev.xpple.simplewaypoints;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import dev.xpple.betterconfig.api.ModConfigBuilder;
import dev.xpple.simplewaypoints.commands.BuildInfoCommand;
import dev.xpple.simplewaypoints.commands.ImportCommand;
import dev.xpple.simplewaypoints.commands.WaypointCommand;
import dev.xpple.simplewaypoints.config.Configs;
import dev.xpple.simplewaypoints.config.WaypointColor;
import dev.xpple.simplewaypoints.config.WaypointColorAdapter;
import dev.xpple.simplewaypoints.config.WaypointColorArgument;
import dev.xpple.simplewaypoints.impl.WaypointRenderingHelper;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SimpleWaypoints {
    public static final String MOD_ID = "simplewaypoints";

    public static final Path MOD_CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(MOD_ID);

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void onInitializeClient() {
        try {
            Files.createDirectories(MOD_CONFIG_PATH);
        } catch (IOException e) {
            LOGGER.error("Failed to create config dir", e);
        }

        new ModConfigBuilder<Component, CommandBuildContext>(MOD_ID, Configs.class)
            .registerType(WaypointColor.class, new WaypointColorAdapter(), WaypointColorArgument::waypointColor)
            .build();

        WaypointRenderingHelper.registerEvents();

        ClientCommandRegistrationCallback.EVENT.register(SimpleWaypoints::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context) {
        BuildInfoCommand.register(dispatcher);
        ImportCommand.register(dispatcher);
        WaypointCommand.register(dispatcher);
    }
}
