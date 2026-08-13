/*
 * Decompiled with CFR 0.152.
 */
package dev.xpple.betterconfig;

import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.betterconfig.command.client.ConfigCommandClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(value= Dist.CLIENT)
public class BetterConfigClient {
    public static void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(BetterConfigClient::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        ConfigCommandClient.register(dispatcher, registryAccess);
    }
}

