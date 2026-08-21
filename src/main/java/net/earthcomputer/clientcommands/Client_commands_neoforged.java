package net.earthcomputer.clientcommands;

import com.mojang.logging.LogUtils;
import dev.xpple.betterconfig.BetterConfig;
import dev.xpple.betterconfig.BetterConfigClient;
import dev.xpple.clientarguments.ClientArguments;
import dev.xpple.simplewaypoints.SimpleWaypoints;
import net.earthcomputer.clientcommands.command.CreativeTabCommand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import org.slf4j.Logger;

@Mod(Client_commands_neoforged.MOD_ID)
public class Client_commands_neoforged {
    public static final String MOD_ID = "client_commands_neoforged";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Client_commands_neoforged(IEventBus modEventBus) {
        CreativeTabCommand.registerCreativeTabs();
        CreativeTabCommand.CREATIVE_MODE_TABS.register(modEventBus);

        LOGGER.info(MOD_ID + " Mod initialized successfully.");
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.DEDICATED_SERVER)
    public static class ServerModEvents {
        @SubscribeEvent
        public static void onServerSetup(FMLDedicatedServerSetupEvent event) {
            BetterConfig.onInitializeServer();
        }
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            ClientArguments.onInitializeClient();
            BetterConfigClient.onInitializeClient();
            SimpleWaypoints.onInitializeClient();
            ClientCommands.onInitializeClient();
        }
    }
}
