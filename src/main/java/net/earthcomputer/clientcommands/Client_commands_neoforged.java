package net.earthcomputer.clientcommands;

import com.mojang.logging.LogUtils;
import dev.xpple.betterconfig.BetterConfig;
import dev.xpple.betterconfig.BetterConfigClient;
import dev.xpple.clientarguments.ClientArguments;
import net.earthcomputer.clientcommands.command.CreativeTabCommand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

@Mod(Client_commands_neoforged.MOD_ID)
public class Client_commands_neoforged {
    public static final String MOD_ID = "client_commands_neoforged";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Client_commands_neoforged(IEventBus modEventBus) throws IOException {
        Enumeration<URL> roots = Client_commands_neoforged.class.getClassLoader().getResources("");
        while (roots.hasMoreElements()) {
            LOGGER.info("Classpath root: " + roots.nextElement());
        }

        URL mappingsDir = Client_commands_neoforged.class.getClassLoader().getResource("mappings");
        LOGGER.info("mappings dir exists? " + mappingsDir);

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
            ClientCommands.onInitializeClient();
        }
    }
}
